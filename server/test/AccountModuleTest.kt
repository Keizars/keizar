package org.keizar.server

import io.ktor.http.ContentType
import kotlinx.coroutines.test.runTest
import org.keizar.server.data.InMemoryDatabaseManagerImpl
import org.keizar.server.logic.AccountModuleImpl
import org.keizar.server.data.InMemoryAvatarStorage
import org.keizar.server.utils.AuthTokenManager
import org.keizar.utils.communication.account.AuthStatus
import java.io.File
import java.net.URL
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

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
            avatarStorage = InMemoryAvatarStorage(),
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
            avatarStorage = InMemoryAvatarStorage(),
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
            avatarStorage = InMemoryAvatarStorage(),
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
            avatarStorage = InMemoryAvatarStorage(),
        )

        assertNull(accountModule.getUser(UUID.randomUUID()))

        val respond = accountModule.register("test", byteArrayOf(1))
        assertEquals(AuthStatus.SUCCESS, respond.status)
        assertNotNull(respond.token)
        val userId = plainAuthTokenManager.matchToken(respond.token!!)
        assertNotNull(userId)

        val user = accountModule.getUser(UUID.fromString(userId))!!
        assertEquals("test", user.username)
        assertEquals("test", user.nickname)
    }

    @Test
    fun `test getUserByName`() = runTest {
        val accountModule = AccountModuleImpl(
            database = InMemoryDatabaseManagerImpl(),
            authTokenManager = plainAuthTokenManager,
            avatarStorage = InMemoryAvatarStorage(),
        )

        assertNull(accountModule.getUserByUsername("blah"))

        val respond = accountModule.register("test", byteArrayOf(1))
        assertEquals(AuthStatus.SUCCESS, respond.status)
        assertNotNull(respond.token)
        val userId = plainAuthTokenManager.matchToken(respond.token!!)
        assertNotNull(userId)

        val user = accountModule.getUserByUsername("test")!!
        assertEquals("test", user.nickname)
    }

    @Test
    fun `test updateInfo`() = runTest {
        val accountModule = AccountModuleImpl(
            database = InMemoryDatabaseManagerImpl(),
            authTokenManager = plainAuthTokenManager,
            avatarStorage = InMemoryAvatarStorage(),
        )

        val respond = accountModule.register("test", byteArrayOf(1))
        assertEquals(AuthStatus.SUCCESS, respond.status)
        assertNotNull(respond.token)
        val userId = plainAuthTokenManager.matchToken(respond.token!!)
        assertNotNull(userId)

        val user = accountModule.getUser(UUID.fromString(userId))
        assertEquals("test", user!!.username)
        assertEquals("test", user.nickname)
        assertEquals("", user.avatarUrl)

        val newNickname = "newNickname"
        val newUsername = "newUsername"
        accountModule.updateInfo(
            uid = UUID.fromString(userId),
            newUsername = newUsername,
            newNickname = newNickname,
        )

        val updatedUser = accountModule.getUser(UUID.fromString(userId))!!
        assertEquals(newNickname, updatedUser.nickname)
        assertEquals(newUsername, updatedUser.username)
    }

    @Test
    fun `test uploadNewAvatar`() = runTest {
        val accountModule = AccountModuleImpl(
            database = InMemoryDatabaseManagerImpl(),
            authTokenManager = plainAuthTokenManager,
            avatarStorage = InMemoryAvatarStorage(),
        )

        val respond = accountModule.register("test", byteArrayOf(1))
        assertEquals(AuthStatus.SUCCESS, respond.status)
        assertNotNull(respond.token)
        val userId = plainAuthTokenManager.matchToken(respond.token!!)
        assertNotNull(userId)

        val user = accountModule.getUser(UUID.fromString(userId))
        assertEquals("test", user!!.username)
        assertEquals("test", user.nickname)
        assertEquals("", user.avatarUrl)

        val avatarFile = File("test/testAvatar.png").absoluteFile
        avatarFile.inputStream().use {
            accountModule.uploadNewAvatar(
                uid = UUID.fromString(userId),
                input = it,
                contentType = ContentType.Image.PNG,
            )
        }

        val updatedAvatarUrl = accountModule.getUser(UUID.fromString(userId))!!.avatarUrl
        assertNotNull(updatedAvatarUrl)
    }
}