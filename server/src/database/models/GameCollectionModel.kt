package org.keizar.server.database.models

import org.bson.codecs.pojo.annotations.BsonId

data class GameCollectionModel(
    @BsonId
    val id: Int, // Primary Key
    val gameId: String, // Foreign Key -> Games
    val userId: String, // Foreign Key -> Users, Index
)