package org.keizar.client.internal

import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.receiveDeserialized
import io.ktor.client.plugins.websocket.sendSerialized
import io.ktor.serialization.WebsocketDeserializeException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import org.keizar.client.exception.NetworkFailureException
import org.keizar.utils.communication.message.Request
import org.keizar.utils.communication.message.Respond
import kotlin.coroutines.CoroutineContext


internal interface WebsocketSessionHandler : AutoCloseable {
    suspend fun start()
    fun sendRequest(request: Request)
}

internal abstract class AbstractWebsocketSessionHandler(
    private val session: DefaultClientWebSocketSession,
    parentCoroutineContext: CoroutineContext,
) : WebsocketSessionHandler {
    private val myCoroutineScope: CoroutineScope =
        CoroutineScope(parentCoroutineContext + Job(parent = parentCoroutineContext[Job]))

    override suspend fun start() {
        try {
            myCoroutineScope.launch {
                session.messageInflow()
            }
            myCoroutineScope.launch {
                session.messageOutflow()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw NetworkFailureException(cause = e)
        }
    }

    override fun close() {
        myCoroutineScope.cancel()
    }

    private val outflowChannel: Channel<Request> = Channel()

    private suspend fun DefaultClientWebSocketSession.messageOutflow() {
        while (true) {
            try {
                val request = outflowChannel.receive()
                println("Client sending: $request")
                sendSerialized(request)
            } catch (e: CancellationException) {
                // ignore
            }
        }
    }

    private suspend fun DefaultClientWebSocketSession.messageInflow() {
        while (true) {
            try {
                val respond = receiveDeserialized<Respond>()
                println("Client received: $respond")
                processResponse(respond)
            } catch (e: WebsocketDeserializeException) {
                // ignore
            }
        }
    }

    abstract suspend fun processResponse(respond: Respond)
    override fun sendRequest(request: Request) {
        myCoroutineScope.launch {
            outflowChannel.send(request)
        }
    }
}