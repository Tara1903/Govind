package com.example.govind.ui.features.starpay

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.govind.data.starpay.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

data class StarPayUiState(
    val status: StarPayOrderStatus = StarPayOrderStatus.CREATED,
    val isLoading: Boolean = true,
    val orderId: String = "",
    val orderRef: String = "",
    val amount: Double = 0.0,
    val reservedAmount: Double = 0.0,
    val currency: String = "INR",
    val description: String = "",
    val paymentToken: String = "",
    val upiTxnRef: String = "",
    val upiUrl: String = "",
    val upiId: String = "",
    val qrBase64: String = "",
    val expiresAtIso: String = "",
    val remainingSeconds: Long = 1800, // 30 minutes default
    val error: String? = null,
    val utrInput: String = "",
    val notesInput: String = "",
    val isSubmittingUtr: Boolean = false,
    val utrError: String? = null,
    val showManualUtrSheet: Boolean = false,
    val showCancelDialog: Boolean = false,
    val isCopiedAmount: Boolean = false,
    val isCopiedUpiId: Boolean = false
)

@HiltViewModel
class StarPayViewModel @Inject constructor(
    private val apiClient: StarPayApiClient,
    private val repository: com.example.govind.domain.repository.GovindRepository
) : ViewModel() {

    companion object {
        private const val TAG = "StarPayViewModel"
        private const val POLL_INTERVAL_MS = 3000L
    }

    private val _uiState = MutableStateFlow(StarPayUiState())
    val uiState: StateFlow<StarPayUiState> = _uiState.asStateFlow()

    private var pollingJob: Job? = null
    private var timerJob: Job? = null
    private var isInitialized = false
    private var currentInternalOrderId: String = ""

    fun initSession(
        amount: Double,
        orderId: String = "",
        orderRef: String = "",
        description: String = "",
        customerName: String = "",
        customerEmail: String = "",
        customerPhone: String = "",
        internalOrderId: String = ""
    ) {
        if (isInitialized) return
        isInitialized = true
        currentInternalOrderId = internalOrderId

        _uiState.update {
            it.copy(
                isLoading = true,
                amount = amount,
                description = description.ifBlank { "GOVIND Fresh & Healthy Food" },
                error = null
            )
        }

        viewModelScope.launch {
            try {
                // Step 1: Create Order in StarPay
                val phoneToPass = if (customerPhone.matches(Regex("^[6-9]\\d{9}$"))) customerPhone else null
                val emailToPass = if (customerEmail.contains("@")) customerEmail else null
                val descToPass = description.ifBlank { "Govind Order ${internalOrderId.take(8)}" }

                val createReq = StarPayCreateOrderRequest(
                    amount = amount,
                    currency = "INR",
                    description = descToPass,
                    customerName = customerName.ifBlank { "Govind Customer" },
                    customerEmail = emailToPass,
                    customerPhone = phoneToPass,
                    metadata = if (internalOrderId.isNotBlank()) mapOf("internalOrderId" to internalOrderId) else null
                )

                val createResult = apiClient.createOrder(createReq)
                if (createResult.isFailure) {
                    val ex = createResult.exceptionOrNull()
                    Log.e(TAG, "Order creation failed", ex)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            status = StarPayOrderStatus.FAILED,
                            error = ex?.message ?: "Failed to initialize StarPay payment session"
                        )
                    }
                    return@launch
                }

                val orderData = createResult.getOrThrow()
                _uiState.update {
                    it.copy(
                        orderId = orderData.orderId,
                        orderRef = orderData.orderRef,
                        amount = orderData.amount,
                        reservedAmount = orderData.reservedAmount,
                        paymentToken = orderData.paymentToken,
                        upiTxnRef = orderData.upiTxnRef ?: "",
                        expiresAtIso = orderData.expiresAt,
                        status = StarPayOrderStatus.AWAITING_PAYMENT,
                        remainingSeconds = calculateRemainingSeconds(orderData.expiresAt)
                    )
                }

                // Step 2: Fetch QR & UPI Intent URL
                fetchQrData(orderData.orderId, orderData.paymentToken)

                // Step 3: Start countdown timer and polling
                startTimer()
                startPolling()

            } catch (e: Exception) {
                Log.e(TAG, "Error initializing payment session", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        status = StarPayOrderStatus.FAILED,
                        error = e.message ?: "Unexpected error initializing payment"
                    )
                }
            }
        }
    }

    private suspend fun fetchQrData(orderId: String, paymentToken: String) {
        val qrResult = apiClient.getQr(orderId, paymentToken)
        if (qrResult.isSuccess) {
            val qr = qrResult.getOrThrow()
            val rawBase64 = if (qr.qrDataUrl.contains("base64,")) {
                qr.qrDataUrl.substringAfter("base64,")
            } else {
                qr.qrDataUrl
            }
            _uiState.update {
                it.copy(
                    qrBase64 = rawBase64,
                    upiUrl = qr.upiUrl,
                    upiId = qr.upiId,
                    isLoading = false
                )
            }
        } else {
            Log.w(TAG, "Failed to load QR code: ${qrResult.exceptionOrNull()?.message}")
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun startPolling() {
        if (pollingJob?.isActive == true) return

        pollingJob = viewModelScope.launch {
            while (isActive) {
                delay(POLL_INTERVAL_MS)
                val state = _uiState.value
                if (state.orderId.isNotBlank() && state.paymentToken.isNotBlank()) {
                    if (state.status == StarPayOrderStatus.PAID || state.status == StarPayOrderStatus.REFUNDED) {
                        break
                    }
                    pollStatus(state.orderId, state.paymentToken)
                }
            }
        }
    }

    fun refreshStatus() {
        // Called on Lifecycle ON_RESUME
        val state = _uiState.value
        if (state.orderId.isNotBlank() && state.paymentToken.isNotBlank() &&
            state.status != StarPayOrderStatus.PAID && state.status != StarPayOrderStatus.REFUNDED
        ) {
            viewModelScope.launch {
                pollStatus(state.orderId, state.paymentToken)
            }
        }
    }

    private suspend fun pollStatus(orderId: String, paymentToken: String) {
        try {
            val result = apiClient.getOrderStatus(orderId, paymentToken)
            if (result.isSuccess) {
                val statusData = result.getOrThrow()
                val parsedStatus = StarPayOrderStatus.fromString(statusData.status)

                if (parsedStatus != _uiState.value.status) {
                    Log.d(TAG, "StarPay order status changed: ${_uiState.value.status} -> $parsedStatus")
                    _uiState.update {
                        it.copy(
                            status = parsedStatus,
                            upiTxnRef = statusData.upiTxnRef ?: it.upiTxnRef
                        )
                    }

                    if (parsedStatus == StarPayOrderStatus.PAID) {
                        pollingJob?.cancel()
                        timerJob?.cancel()
                        if (currentInternalOrderId.isNotBlank()) {
                            try {
                                repository.markOrderPaid(currentInternalOrderId, statusData.upiTxnRef)
                            } catch (e: Exception) {
                                Log.w(TAG, "Failed to mark internal order paid: ${e.message}")
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Polling status error: ${e.message}")
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000L)
                val current = _uiState.value.remainingSeconds
                if (current <= 1) {
                    _uiState.update {
                        it.copy(
                            remainingSeconds = 0,
                            status = if (it.status == StarPayOrderStatus.AWAITING_PAYMENT) StarPayOrderStatus.FAILED else it.status
                        )
                    }
                    pollingJob?.cancel()
                    break
                } else {
                    _uiState.update { it.copy(remainingSeconds = current - 1) }
                }
            }
        }
    }

    private fun calculateRemainingSeconds(expiresAtIso: String): Long {
        return try {
            val formats = listOf(
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.US),
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.US),
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") },
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }
            )
            for (fmt in formats) {
                try {
                    val date = fmt.parse(expiresAtIso)
                    if (date != null) {
                        val diffSeconds = (date.time - System.currentTimeMillis()) / 1000
                        return diffSeconds.coerceAtLeast(0)
                    }
                } catch (_: Exception) {}
            }
            1800L
        } catch (e: Exception) {
            1800L
        }
    }

    fun onUtrChanged(input: String) {
        val filtered = input.filter { it.isDigit() }.take(12)
        _uiState.update {
            it.copy(
                utrInput = filtered,
                utrError = if (filtered.length == 12) null else it.utrError
            )
        }
    }

    fun onNotesChanged(notes: String) {
        _uiState.update { it.copy(notesInput = notes) }
    }

    fun openManualUtrSheet() {
        _uiState.update { it.copy(showManualUtrSheet = true, utrError = null) }
    }

    fun closeManualUtrSheet() {
        _uiState.update { it.copy(showManualUtrSheet = false, utrError = null) }
    }

    fun showCancelConfirmation() {
        _uiState.update { it.copy(showCancelDialog = true) }
    }

    fun dismissCancelConfirmation() {
        _uiState.update { it.copy(showCancelDialog = false) }
    }

    fun submitManualUtr() {
        val state = _uiState.value
        val utr = state.utrInput.trim()
        if (utr.length != 12 || !utr.matches(Regex("^\\d{12}$"))) {
            _uiState.update { it.copy(utrError = "Please enter a valid 12-digit UTR number") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingUtr = true, utrError = null) }
            val result = apiClient.submitManualVerification(
                orderId = state.orderId,
                paymentToken = state.paymentToken,
                utrEntered = utr,
                notes = state.notesInput.ifBlank { null }
            )

            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isSubmittingUtr = false,
                        showManualUtrSheet = false,
                        status = StarPayOrderStatus.PENDING_VERIFICATION
                    )
                }
                // Continue polling
                startPolling()
            } else {
                val err = result.exceptionOrNull()?.message ?: "Failed to submit UTR verification"
                _uiState.update {
                    it.copy(
                        isSubmittingUtr = false,
                        utrError = err
                    )
                }
            }
        }
    }

    fun setCopiedAmount(copied: Boolean) {
        _uiState.update { it.copy(isCopiedAmount = copied) }
    }

    fun setCopiedUpiId(copied: Boolean) {
        _uiState.update { it.copy(isCopiedUpiId = copied) }
    }

    fun retryPayment(
        amount: Double,
        customerName: String,
        customerEmail: String,
        customerPhone: String,
        internalOrderId: String
    ) {
        isInitialized = false
        pollingJob?.cancel()
        timerJob?.cancel()
        initSession(
            amount = amount,
            customerName = customerName,
            customerEmail = customerEmail,
            customerPhone = customerPhone,
            internalOrderId = internalOrderId
        )
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
        timerJob?.cancel()
    }
}
