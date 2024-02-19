package org.keizar.android.tutorial

import androidx.compose.runtime.Stable
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import org.keizar.utils.communication.game.BoardPos
import kotlin.time.Duration


/**
 * Actions that a step is requesting. For example, requesting the player to move a piece.
 * @see TutorialSession.requests
 */
abstract class TutorialRequests {
    /**
     * A flow of requests that the current step is requesting.
     *
     * `null` if the current step is not requesting anything, in which case the UI should cancel any existing requests.
     */
    @Stable
    abstract val request: StateFlow<TutorialRequest<*>?>

    private inline fun <reified T> filterRequest(): Flow<T?> {
        return request.map {
            when (it) {
                null -> null
                is T -> it
                else -> null
            }
        }
    }

    /**
     * A flow of requests that the current step is requesting.
     */
    @Stable
    val requestingPlayerMove: Flow<TutorialRequest.MovePlayer?> by lazy { filterRequest<TutorialRequest.MovePlayer>() }

    @Stable
    val requestingClickNext: Flow<TutorialRequest.ClickNext?> by lazy {
        filterRequest<TutorialRequest.ClickNext>()
    }

    @Stable
    val requestingShowPossibleMoves: Flow<TutorialRequest.ShowPossibleMoves?> by lazy { filterRequest<TutorialRequest.ShowPossibleMoves>() }
}

/**
 * @see TutorialRequests
 */
sealed interface TutorialRequest<R> {
    val isResponded: Boolean get() = response.isCompleted

    val response: Deferred<R>

    /**
     * Responds to the request with the given data.
     *
     * A request can only be responded once. Throws [IllegalStateException] if responded more than once.
     */
    fun respond(data: R)


    abstract class CompletableTutorialRequest<R> : TutorialRequest<R> {
        protected val completableResponse: CompletableDeferred<R> = CompletableDeferred()
        final override val response get() = completableResponse
        override fun respond(data: R) {
            if (completableResponse.isCompleted) {
                throw IllegalStateException("Request already responded with '${completableResponse.getCompleted()}', but attempted to respond again with: $data")
            }
            completableResponse.complete(data)
        }
    }

    data class MovePlayer(
        /**
         * Logical position of the piece to be moved from.
         */
        val from: BoardPos,
        /**
         * Logical position of the piece to be moved to.
         */
        val to: BoardPos,
    ) : CompletableTutorialRequest<BoardPos>()

    class ClickNext : CompletableTutorialRequest<Unit>() {
        fun respond() = respond(Unit)
    }

    class ShowPossibleMoves(val logicalPos: BoardPos, val duration: Duration) : CompletableTutorialRequest<Unit>() {
        fun respond() = respond(Unit)
    }
}

suspend inline fun <R> TutorialRequest<R>.awaitResponse(): R = response.await()
