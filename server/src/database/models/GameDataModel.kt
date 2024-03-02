package org.keizar.server.database.models

import kotlinx.serialization.json.JsonElement
import org.bson.codecs.pojo.annotations.BsonId
import java.util.UUID


data class GameDataModel(
    @BsonId
    val id: UUID, // Primary Key
    val userId1: String? = null, // Foreign Key -> Users, Index
    val userId2: String? = null, // Foreign Key -> Users, Index
    val round1Statistics: JsonElement,
    val round2Statistics: JsonElement,
    val timeStamp: String,
    val gameConfiguration: String
)