package org.keizar.server.database

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import software.amazon.awssdk.services.dynamodb.model.AttributeValue

class DatabaseTest {
    @Test
    fun `test dao put, getAllItems and delete`() {
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
}