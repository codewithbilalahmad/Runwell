package com.muhammad.core.connectivity.domain.messaging

import com.muhammad.core.domain.util.Error

enum class MessagingError: Error{
    CONNECTION_INTERRUPTED,
    DISCONNECTED, UNKNOWN
}