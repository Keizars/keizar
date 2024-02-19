@file:Suppress("MemberVisibilityCanBePrivate")

package org.keizar.android.tutorial

import org.keizar.game.Role
import kotlin.time.Duration.Companion.seconds

object Tutorials {
    val Refresher1 = buildTutorial("fresher-1") {
        board {
            tiles {
            }
            round {
                curRole { Role.BLACK }
                resetPieces {
                    white("b2")
                    black("g7")
                }
            }
        }

        playerStartAsBlack()

        steps {
            step("move black") {
                moveOpponent { "g7" to "g6" }
                delay(1.seconds)
                moveOpponent { "g6" to "g8" }
                delay(1.seconds)
                awaitNext()
            }

            step("") {
                message("Ok")
                awaitNext()
            }
        }
    }
    private val list = buildList {
        add(Refresher1)
    }

    fun getById(id: String): Tutorial {
        return list.first { it.id == id }
    }
}