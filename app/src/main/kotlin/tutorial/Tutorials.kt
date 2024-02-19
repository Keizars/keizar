@file:Suppress("MemberVisibilityCanBePrivate")

package org.keizar.android.tutorial

import androidx.compose.material3.Text
import org.keizar.game.Role
import org.keizar.game.TileType
import kotlin.time.Duration.Companion.seconds

object Tutorials {
    val Refresher1 = buildTutorial("fresher-1") {
        board {
            tiles {
                change("f3", TileType.ROOK)
                change("a3", TileType.KNIGHT)
                change("b5", TileType.BISHOP)
                change("e8", TileType.QUEEN)
                change("e6", TileType.KING)
            }
            round {
                allowFreeMove()
                disableWinner()
                curRole { Role.BLACK }
                resetPieces {
                    white("f3")
                    white("f4")
                    black("g7")
                }
            }
        }

        playerStartAsBlack()

        steps {
            step("start") {
                awaitNext()
            }

            step("move black") {
                showPossibleMovesThenMove { "g7" to "g5" }
                delay(1.seconds)
                showPossibleMovesThenMove { "g5" to "g4" }
                delay(1.seconds)
                awaitNext()
            }

            step("capture white") {
                showPossibleMovesThenMove { "g4" to "f3" }
                awaitNext()
            }

            step("move as rook") {
                showPossibleMovesThenMove { "f3" to "a3" }
                awaitNext()
            }

            step("move as knight") {
                showPossibleMovesThenMove { "a3" to "b5" }
                awaitNext()
            }

            step("move as bishop") {
                showPossibleMovesThenMove { "b5" to "e8" }
                awaitNext()
            }

            step("move as queen") {
                showPossibleMovesThenMove { "e8" to "e6" }
                awaitNext()
            }

            step("move as king to keizar") {
                showPossibleMovesThenMove { "e6" to "d5" }
                awaitNext()
            }

            step("show rules") {
                showBottomSheet {
                    Text(text = "Rules")
                }
            }
        }
    }

    private suspend inline fun StepActionContext.showPossibleMovesThenMove(moveBuilder: MoveBuilder.() -> Unit) {
        val builder = MoveBuilder().apply(moveBuilder)
        val (from, to) = builder.build()
        showPossibleMoves(from)
        movePlayer(from, to)
    }

    private val list = buildList {
        add(Refresher1)
    }

    fun getById(id: String): Tutorial {
        return list.first { it.id == id }
    }
}