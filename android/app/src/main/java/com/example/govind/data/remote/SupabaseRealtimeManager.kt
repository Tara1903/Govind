package com.example.govind.data.remote

import android.util.Log
import com.example.govind.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

data class RealtimeChangeEvent(
    val table: String,
    val eventType: String, // INSERT, UPDATE, DELETE
    val record: JsonObject?,
    val oldRecord: JsonObject?
)

@Singleton
class SupabaseRealtimeManager @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var webSocket: WebSocket? = null
    private val isConnected = AtomicBoolean(false)
    private val refCounter = AtomicInteger(1)
    private val activeChannels = mutableSetOf<String>()

    val eventFlow = MutableSharedFlow<RealtimeChangeEvent>(extraBufferCapacity = 64)

    init {
        connect()
        startHeartbeat()
    }

    @Synchronized
    fun connect() {
        if (isConnected.get()) return

        val baseUrl = BuildConfig.SUPABASE_URL
            .replace("https://", "wss://")
            .replace("http://", "ws://")
        val anonKey = BuildConfig.SUPABASE_ANON_KEY
        val wsUrl = "$baseUrl/realtime/v1/websocket?apikey=$anonKey&vsn=1.0.0"

        val request = Request.Builder().url(wsUrl).build()

        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                isConnected.set(true)
                Log.d("SupabaseRealtime", "WebSocket Connected")
                // Re-subscribe to all active channels
                synchronized(activeChannels) {
                    activeChannels.forEach { channel ->
                        sendJoin(channel)
                    }
                }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                handleMessage(text)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                isConnected.set(false)
                Log.d("SupabaseRealtime", "WebSocket Closed: $reason")
                scheduleReconnect()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                isConnected.set(false)
                Log.w("SupabaseRealtime", "WebSocket Failure: ${t.message}")
                scheduleReconnect()
            }
        })
    }

    private fun scheduleReconnect() {
        scope.launch {
            delay(5000)
            if (!isConnected.get()) {
                connect()
            }
        }
    }

    private fun startHeartbeat() {
        scope.launch {
            while (isActive) {
                delay(25000)
                if (isConnected.get()) {
                    val ref = refCounter.incrementAndGet().toString()
                    val hb = """{"topic":"phoenix","event":"heartbeat","payload":{},"ref":"$ref"}"""
                    webSocket?.send(hb)
                }
            }
        }
    }

    private fun sendJoin(topic: String) {
        val ref = refCounter.incrementAndGet().toString()
        val joinMsg = """{"topic":"$topic","event":"phx_join","payload":{"config":{"broadcast":{"self":true},"postgres_changes":[{"event":"*","schema":"public"}]}},"ref":"$ref"}"""
        webSocket?.send(joinMsg)
    }

    fun subscribeTable(tableName: String): Flow<RealtimeChangeEvent> {
        val topic = "realtime:public:$tableName"
        synchronized(activeChannels) {
            if (activeChannels.add(topic) && isConnected.get()) {
                sendJoin(topic)
            }
        }

        return flow {
            eventFlow.filter { it.table.equals(tableName, ignoreCase = true) }.collect {
                emit(it)
            }
        }
    }

    private fun handleMessage(text: String) {
        try {
            val json = Json.parseToJsonElement(text).jsonObject
            val event = json["event"]?.jsonPrimitive?.content
            val topic = json["topic"]?.jsonPrimitive?.content ?: ""

            if (event == "postgres_changes") {
                val payload = json["payload"]?.jsonObject
                val data = payload?.get("data")?.jsonObject
                val table = data?.get("table")?.jsonPrimitive?.content ?: ""
                val type = data?.get("type")?.jsonPrimitive?.content ?: ""
                val record = data?.get("record") as? JsonObject
                val oldRecord = data?.get("old_record") as? JsonObject

                if (table.isNotBlank()) {
                    scope.launch {
                        eventFlow.emit(
                            RealtimeChangeEvent(
                                table = table,
                                eventType = type,
                                record = record,
                                oldRecord = oldRecord
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("SupabaseRealtime", "Parse error: ${e.message}")
        }
    }
}
