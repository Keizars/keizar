package org.keizar.android.tutorial

object Tutorials {
    private val list = buildList<Tutorial> {
        add(buildTutorial("fresher-1") {

        })
    }

    fun getById(id: String): Tutorial {
        return list.first { it.id == id }
    }
}