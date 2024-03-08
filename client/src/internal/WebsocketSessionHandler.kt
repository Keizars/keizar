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
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.launch
import org.keizar.client.exception.NetworkFailureException
import org.keizar.utils.communication.message.Request
import org.keizar.utils.communication.message.Respond
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.CoroutineContext


internal interface WebsocketSessionHandler : AutoCloseable {
    val isClosed: Boolean
    suspend fun start()
    suspend fun sendRequest(request: Request)
}

/**
 * The template method class for a websocket session handler with an (abstract)
 * configurable [processResponse] method.
 * Provides a [sendRequest] method to send requests through the websocket.
 */
internal abstract class AbstractWebsocketSessionHandler(
    private val session: DefaultClientWebSocketSession,
    parentCoroutineContext: CoroutineContext,
    /**
     * If true, the handler with cancel the websocket session when it is closed.
     */
    private val cancelWebsocketOnExit: Boolean,
) : WebsocketSessionHandler {
    private val myCoroutineScope: CoroutineScope =
        CoroutineScope(parentCoroutineContext + Job(parent = parentCoroutineContext[Job]))

    override suspend fun start() {
        try {
            myCoroutineScope.launch {
                session.messageInflow()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw NetworkFailureException(cause = e)
        }
    }

    private val _isClosed = AtomicBoolean(false)
    override val isClosed: Boolean get() = _isClosed.get()

    /**
     * A concurrent-safe idempotent close method.
     * Will cancel the websocket session if [cancelWebsocketOnExit] is set.
     */
    override fun close() {
        if (_isClosed.compareAndSet(false, true)) {
            myCoroutineScope.cancel()
            if (cancelWebsocketOnExit) session.cancel()
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
            } catch (e: ClosedReceiveChannelException) {
                println("Websocket session $session closed")
                return
            }
        }
    }

    abstract suspend fun processResponse(respond: Respond)
    override suspend fun sendRequest(request: Request) {
        println("Client sending: $request")
        session.sendSerialized(request)
    }
}