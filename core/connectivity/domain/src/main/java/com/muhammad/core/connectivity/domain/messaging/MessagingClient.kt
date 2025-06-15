package com.muhammad.core.connectivity.domain.messaging

import com.muhammad.core.domain.util.EmptyResult
import kotlinx.coroutines.flow.Flow

interface MessagingClient{
    fun connectToNode(nodeId : String) : Flow<MessagingAction>
    suspend fun sendOrQueueAction(action : MessagingAction) : EmptyResult<MessagingError>
}