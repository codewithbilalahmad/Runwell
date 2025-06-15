package com.muhammad.core.connectivity.data.messaging

import android.content.Context
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.muhammad.core.connectivity.domain.messaging.MessagingAction
import com.muhammad.core.connectivity.domain.messaging.MessagingClient
import com.muhammad.core.connectivity.domain.messaging.MessagingError
import com.muhammad.core.domain.util.EmptyResult
import com.muhammad.core.domain.util.Result
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json

class WearMessagingClient(
    private val context: Context,
) : MessagingClient {
    private val client = Wearable.getMessageClient(context)
    private val messageQueue = mutableListOf<MessagingAction>()
    private var connectedNodeId: String? = null
    override fun connectToNode(nodeId: String): Flow<MessagingAction> {
        connectedNodeId = nodeId
        return callbackFlow {
            val listener: (MessageEvent) -> Unit = { event ->
                if (event.path.startsWith(BASE_PATH_MESSAGING_ACTION)) {
                    val json = event.data.decodeToString()
                    val action = Json.decodeFromString<MessagingActionDto>(json)
                    trySend(action.toMessagingAction())
                }
            }
            client.addListener(listener)
            messageQueue.forEach { message ->
                sendOrQueueAction(message)
            }
            messageQueue.clear()
            awaitClose {
                client.removeListener(listener)
            }
        }
    }

    override suspend fun sendOrQueueAction(action: MessagingAction): EmptyResult<MessagingError> {
        return connectedNodeId?.let { id ->
            try {
                val json = Json.encodeToString(action.toMessagingActionDto())
                client.sendMessage(id, BASE_PATH_MESSAGING_ACTION, json.encodeToByteArray()).await()
                Result.Success(Unit)
            } catch (e: ApiException) {
                Result.Failure(if (e.status.isInterrupted) MessagingError.CONNECTION_INTERRUPTED else MessagingError.UNKNOWN)
            }
        } ?: run{
            messageQueue.add(action)
            Result.Failure(MessagingError.DISCONNECTED)
        }
    }

    companion object {
        private const val BASE_PATH_MESSAGING_ACTION = "runwell/messaging_action"
    }
}