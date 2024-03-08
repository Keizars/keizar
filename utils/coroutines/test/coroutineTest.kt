import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import org.junit.jupiter.api.Test
import org.keizar.utils.coroutines.childScope
import org.keizar.utils.coroutines.childSupervisorScope
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CoroutineExtensionsTest {

    val testDispatcher = Dispatchers.Default

    @Test
    fun `test CoroutineScope childSupervisorScope`() = run {

        val parentScope = CoroutineScope(testDispatcher)
        val childScope = parentScope.childSupervisorScope()

        assertTrue( childScope.coroutineContext[Job] is Job)
    }

    @Test
    fun `test CoroutineScope childScope`() = run {
        val parentScope = CoroutineScope(testDispatcher)
        val childScope = parentScope.childScope()

        assertTrue(childScope.coroutineContext[Job] is Job)
    }

    @Test
    fun `test CoroutineContext childSupervisorScope`() {
        val parentContext: CoroutineContext = testDispatcher
        val childScope = parentContext.childSupervisorScope()

        assertTrue(childScope.coroutineContext[Job] is Job)
    }

    @Test
    fun `test CoroutineContext childScope`() {
        val parentContext: CoroutineContext = testDispatcher
        val childScope = parentContext.childScope()

        assertTrue(childScope.coroutineContext[Job] is Job)
    }
}