package com.muhammad.wear.run.data

import com.muhammad.core.connectivity.domain.DeviceNode
import com.muhammad.core.connectivity.domain.DeviceType
import com.muhammad.core.connectivity.domain.NodeDiscovery
import com.muhammad.core.connectivity.domain.messaging.MessagingAction
import com.muhammad.core.connectivity.domain.messaging.MessagingClient
import com.muhammad.core.connectivity.domain.messaging.MessagingError
import com.muhammad.core.domain.util.EmptyResult
import com.muhammad.wear.run.domain.PhoneConnector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.shareIn

class WatchToPhoneConnector(
    nodeDiscovery : NodeDiscovery,
    applicationScope : CoroutineScope,
    private val messagingClient : MessagingClient
) : PhoneConnector{
    private val _connectedNode = MutableStateFlow<DeviceNode?>(null)
    override val connectedNode = _connectedNode.asStateFlow()
    @OptIn(ExperimentalCoroutinesApi::class)
    override val messagingActions = nodeDiscovery.observeConnectedDevices(DeviceType.WATCH).flatMapLatest{ connectedNodes ->
        val node = connectedNodes.firstOrNull()
        if(node != null && node.isNearBy){
            _connectedNode.value = node
            messagingClient.connectToNode(node.id)
        } else flowOf()
    }.shareIn(applicationScope, SharingStarted.Lazily)
    override suspend fun sendActionToPhone(action: MessagingAction): EmptyResult<MessagingError> {
        return messagingClient.sendOrQueueAction(action)
    }
}