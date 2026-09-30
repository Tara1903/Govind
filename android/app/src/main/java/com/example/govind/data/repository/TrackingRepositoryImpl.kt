package com.example.govind.data.repository

import com.example.govind.data.model.DeliveryLocation
import com.example.govind.data.remote.SupabaseApi
import com.example.govind.domain.repository.TrackingRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrackingRepositoryImpl @Inject constructor(
    private val api: SupabaseApi,
    private val realtimeManager: com.example.govind.data.remote.SupabaseRealtimeManager
) : TrackingRepository {
    
    override fun listenToOrderTracking(orderId: String): Flow<DeliveryLocation?> = flow {
        // 1. Initial fetch
        try {
            val initial = api.getDeliveryLocation("eq.$orderId").firstOrNull()
            emit(initial)
        } catch (_: Exception) {}

        // 2. Realtime WebSocket subscription (no polling)
        realtimeManager.subscribeTable("delivery_locations").collect { event ->
            try {
                val recordOrderId = event.record?.get("order_id")?.toString()?.replace("\"", "")
                if (recordOrderId == null || recordOrderId.equals(orderId, ignoreCase = true)) {
                    val updated = api.getDeliveryLocation("eq.$orderId").firstOrNull()
                    emit(updated)
                }
            } catch (_: Exception) {}
        }
    }
}
