package org.keizar.server.database.models

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement
import org.bson.codecs.pojo.annotations.BsonId
import org.keizar.utils.communication.game.GameData
import org.keizar.utils.communication.game.jsonElementToRoundStats
import java.util.UUID


data class GameDataModel(
    @BsonId
    val id: UUID, // Primary Key
    val userId1: String? = null, // Foreign Key -> Users, Index
    val userId2: String? = null, // Foreign Key -> Users, Index
    val round1Statistics: JsonElement,
    val round2Statistics: JsonElement,
    val timeStamp: String,
    val gameConfiguration: String,
    var userSaved: Boolean = false
)

fun modelToData(model: GameDataModel): GameData {
    return GameData(
        model.id.toString(),
        jsonElementToRoundStats( model.round1Statistics),
        jsonElementToRoundStats(model.round2Statistics),
        model.gameConfiguration,
        model.userId1,
        model.userId2,
        model.timeStamp,
        model.userSaved
    )
}

fun dataToModel(data: GameData): GameDataModel {
    if (data.id == null) {
        return GameDataModel(
            UUID.randomUUID(),
            data.user1,
            data.user2,
            Json.encodeToJsonElement(data.round1Statistics),
            Json.encodeToJsonElement(data.round2Statistics),
            data.currentTimestamp,
            data.gameConfiguration,
            data.userSaved
        )
    } else {
        return GameDataModel(
            UUID.fromString(data.id),
            data.user1,
            data.user2,
            Json.encodeToJsonElement(data.round1Statistics),
            Json.encodeToJsonElement(data.round2Statistics),
            data.currentTimestamp,
            data.gameConfiguration,
            data.userSaved
        )
    }
}