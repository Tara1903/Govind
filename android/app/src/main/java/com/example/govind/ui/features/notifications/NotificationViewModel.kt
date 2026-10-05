package com.example.govind.ui.features.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.govind.data.repository.NotificationItem
import com.example.govind.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount

    fun loadNotifications() {
        viewModelScope.launch {
            _isLoading.value = true
            _notifications.value = notificationRepository.getNotifications()
            _unreadCount.value = _notifications.value.count { !it.isRead }
            _isLoading.value = false
        }
    }

    fun markRead(notificationId: String) {
        viewModelScope.launch {
            notificationRepository.markAsRead(notificationId)
            _notifications.value = _notifications.value.map {
                if (it.id == notificationId) it.copy(readAt = java.time.Instant.now().toString())
                else it
            }
            _unreadCount.value = _notifications.value.count { !it.isRead }
        }
    }

    fun markAllRead() {
        viewModelScope.launch {
            notificationRepository.markAllAsRead()
            _notifications.value = _notifications.value.map {
                it.copy(readAt = it.readAt ?: java.time.Instant.now().toString())
            }
            _unreadCount.value = 0
        }
    }
}
