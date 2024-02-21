@file:Suppress("unused")

package samples

import org.keizar.android.tutorial.buildMove

class MoveBuilderSamples {

    fun specifySeparately() {
        // Specify the starting and ending positions separately
        buildMove {
            from("a1")
            to("a2")
        }
    }

    fun naturally() {
        // Describes the move naturally
        buildMove { "a1" to "a2" }
    }
}