@file:Suppress("MemberVisibilityCanBePrivate")

package org.keizar.android.tutorial

object Tutorials {
    val Refresher1 = buildTutorial("fresher-1") {
        steps {
            step("move black") {
                awaitNext()
            }

            step("") {

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