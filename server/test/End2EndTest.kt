package org.keizar.server

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

class End2EndTest {

    private val server =
        getServer(
            env = EnvironmentVariables(
                testing = true,
                mongoDbConnectionString = ""
            )
        ).apply {
            start(wait = false)
        }

    @Test
    fun test() {
        // TODO
    }

    @AfterEach
    fun tearDown() {
        server.stop(0, 0)
    }
}