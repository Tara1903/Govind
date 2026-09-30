package com.example.govind.domain.repository

import com.example.govind.data.model.DeliveryLocation
import kotlinx.coroutines.flow.Flow

interface TrackingRepository {
    fun listenToOrderTracking(orderId: String): Flow<DeliveryLocation?>
}
