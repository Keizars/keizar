package org.keizar.server.database

data class Users(
    val userName: String // Primary Key
)

data class Games(
    val gameId: String // Primary Key
)

data class GameCollections(
    val gameCollectionId: String, // Primary Key
    val gameId: String, // Foreign Key
    val userId: String // Foreign Key
)

data class tokens(
    val token: String, // Primary Key
    val userId: String // Foreign Key
)

data class seedCollections(
    val seedCollectionId: String, // Primary Key
    val userId: String // Foreign Key
)



