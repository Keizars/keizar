package org.keizar.server.database

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import software.amazon.awssdk.services.dynamodb.model.AttributeValue

class DatabaseTest {
    @Test
    fun `test userdao put, getAllItems and delete`() {
        val daoTest = UserDao()
        val users1 = daoTest.getAllItems()
        val testUser = Users("test")
        daoTest.putItem(testUser)
        val users2 = daoTest.getAllItems()
        assertEquals(users1.size + 1, users2.size)
        daoTest.deleteItem(mapOf("user_name" to AttributeValue.builder().s("test").build()))
        val users3 = daoTest.getAllItems()
        assertEquals(users1.size, users3.size)
    }

    @Test
    fun `test gamesDao put, getAllItems and delete`() {
        val daoTest = GamesDao()
        val games1 = daoTest.getAllItems()
        val testGame = Games("test1", "test_user1", "test_user2")
        daoTest.putItem(testGame)
        val games2 = daoTest.getAllItems()
        assertEquals(games1.size + 1, games2.size)
        val testGame2 = Games("test2", "test_user2", "test_user3")
        daoTest.putItem(testGame2)
        val user2Games = daoTest.getGamesByUserId("test_user2")
        assertEquals(2, user2Games.size)
        daoTest.deleteItem(mapOf("game_id" to AttributeValue.builder().s("test1").build()))
        daoTest.deleteItem(mapOf("game_id" to AttributeValue.builder().s("test2").build()))
        val games3 = daoTest.getAllItems()
        assertEquals(games1.size, games3.size)
    }
}