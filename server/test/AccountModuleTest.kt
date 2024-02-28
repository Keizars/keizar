package org.keizar.server

import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.keizar.server.database.InMemoryDatabaseManagerImpl
import org.keizar.server.modules.AccountModuleImpl
import org.keizar.server.utils.AuthTokenManager
import org.keizar.utils.communication.account.AuthStatus
import kotlin.test.Test

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

        respond = accountModule.login("test", byteArrayOf(1))
        assertEquals(AuthStatus.SUCCESS, respond.status)

        respond = accountModule.login("test", byteArrayOf(2))
        assertEquals(AuthStatus.WRONG_PASSWORD, respond.status)
    }
}