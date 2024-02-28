package org.keizar.server.database.models

import kotlinx.serialization.json.JsonElement
import org.bson.codecs.pojo.annotations.BsonId

data class GameModel(
    @BsonId
    val id: String, // Primary Key
    val gameSnapshot: JsonElement,
)