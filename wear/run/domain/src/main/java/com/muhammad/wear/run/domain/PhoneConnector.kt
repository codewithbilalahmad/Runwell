package com.muhammad.wear.run.domain

import com.muhammad.core.connectivity.domain.DeviceNode
import com.muhammad.core.connectivity.domain.messaging.MessagingAction
import com.muhammad.core.connectivity.domain.messaging.MessagingError
import com.muhammad.core.domain.util.EmptyResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface PhoneConnector{
    val connectedNode : StateFlow<DeviceNode?>
    val messagingActions : Flow<MessagingAction>
    suspend fun sendActionToPhone(action : MessagingAction) : EmptyResult<MessagingError>
}