@file:Suppress("unused")

package samples

import org.keizar.android.tutorial.TutorialBuilder
import org.keizar.android.tutorial.buildTutorial
import org.keizar.android.tutorial.message
import org.keizar.android.tutorial.moveOpponent
import org.keizar.android.tutorial.requestMovePlayer
import org.keizar.game.Role
import kotlin.time.Duration.Companion.seconds

class TutorialSamples {
    fun TutorialBuilder.stepThen() {
        steps {
            step("welcome") {
                message("Welcome to Keizar! A prawn can move one square forward!")
                requestMovePlayer { "a2" to "a3" }
            }.then {
                message("Great!")
                delay(2.seconds)
            }
        }
    }

    fun buildTutorialComplete() {
        buildTutorial("example") {
            board {
                round {
                    curRole { Role.WHITE }
                    resetPieces {
                        white("a2")
                        black("d6")
                    }
                }
            }

            steps {
                step("welcome") {
                    message("Welcome to Keizar! A prawn can move one square forward!")
                    requestMovePlayer { "a2" to "a3" }
                }.then {
                    message("Great!")
                    delay(2.seconds)
                }

                step("move knight") {
                    message("The prawn becomes a knight! Now the knight can move in an L shape!")
                    requestMovePlayer { "b1" to "c3" }
                }.then {
                    message("Awesome!")
                    delay(2.seconds)
                }

                step("move opponent") {
                    moveOpponent { "d6" to "d5" }
                    message("Hey! The opponent is reaching the Keizar!")
                    delay(2.seconds)
                }

                step("catch opponent") {
                    message("Catch it!")
                    requestMovePlayer { "e5" to "f6" }
                }.then {
                    message("Well done.")
                    delay(2.seconds)
                }
            }
        }
    }
}