package org.keizar.server

class EnvironmentVariables(
    val mongoDbConnectionString: String? = System.getenv("MONGODB_CONNECTION_STRING"),
    val tokenSecret: String? = System.getenv("TOKEN_SECRET"),
    val testing: Boolean = System.getenv("TESTING") == "true"
)
