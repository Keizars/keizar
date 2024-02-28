package org.keizar.server

import kotlinx.coroutines.test.runTest
import org.keizar.server.database.InMemoryDatabaseManagerImpl
import org.keizar.server.modules.AccountModuleImpl
import org.keizar.server.utils.AuthTokenManager
import org.keizar.utils.communication.account.AuthStatus
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.assertNotNull

class AccountModuleTest {
    private val plainAuthTokenManager = object : AuthTokenManager {
        override fun createToken(userId: String): String = userId
        override fun matchToken(token: String): String = token
    }

    @Test
    fun `test register`() = runTest {
        val accountModule = AccountModuleImpl(
            database = InMemoryDatabaseManagerImpl(),
            authTokenManager = plainAuthTokenManager,
        )

        var respond = accountModule.register("test", byteArrayOf(1))
        assertEquals(AuthStatus.SUCCESS, respond.status)

        respond = accountModule.register("test", byteArrayOf(1))
        assertEquals(AuthStatus.DUPLICATED_USERNAME, respond.status)

        respond = accountModule.register("test2", byteArrayOf(1))
        assertEquals(AuthStatus.SUCCESS, respond.status)

        respond = accountModule.register("test2", byteArrayOf(1))
        assertEquals(AuthStatus.DUPLICATED_USERNAME, respond.status)
    }

    @Test
    fun `test login`() = runTest {
        val accountModule = AccountModuleImpl(
            database = InMemoryDatabaseManagerImpl(),
            authTokenManager = plainAuthTokenManager,
        )
        var respond = accountModule.login("test", byteArrayOf(1))
        assertEquals(AuthStatus.USER_NOT_FOUND, respond.status)

        respond = accountModule.register("test", byteArrayOf(1))
        assertEquals(AuthStatus.SUCCESS, respond.status)

        respond = accountModule.login("test2", byteArrayOf(1))
        assertEquals(AuthStatus.USER_NOT_FOUND, respond.status)

        respond = accountModule.login("test", byteArrayOf(1))
        assertEquals(AuthStatus.SUCCESS, respond.status)

        respond = accountModule.login("test", byteArrayOf(2))
        assertEquals(AuthStatus.WRONG_PASSWORD, respond.status)
    }

    @Test
    fun `test isUsernameTaken`() = runTest {
        val accountModule = AccountModuleImpl(
            database = InMemoryDatabaseManagerImpl(),
            authTokenManager = plainAuthTokenManager,
        )
        assertFalse(accountModule.isUsernameTaken("test"))

        val respond = accountModule.register("test", byteArrayOf(1))
        assertEquals(AuthStatus.SUCCESS, respond.status)

        assertTrue(accountModule.isUsernameTaken("test"))
    }

    @Test
    fun `test getUser and getUserByName`() = runTest {
        val accountModule = AccountModuleImpl(
            database = InMemoryDatabaseManagerImpl(),
            authTokenManager = plainAuthTokenManager,
        )
        
        assertNull(accountModule.getUser(UUID.randomUUID()))
        assertNull(accountModule.getUserByName("blah"))

        val respond = accountModule.register("test", byteArrayOf(1))
        assertEquals(AuthStatus.SUCCESS, respond.status)
        assertNotNull(respond.token)
        val userId = plainAuthTokenManager.matchToken(respond.token!!)
        assertNotNull(userId)
        
        val user1 = accountModule.getUser(UUID.fromString(userId))
        val user2 = accountModule.getUser(UUID.fromString(userId))
        assertEquals(user1!!, user2!!)
        assertEquals("test", user1.username)
        assertEquals("test", user1.nickname)
    }
}