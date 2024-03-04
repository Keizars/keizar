package org.keizar.server.database.models

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement
import org.bson.codecs.pojo.annotations.BsonId
import org.keizar.utils.communication.game.GameDataStore
import org.keizar.utils.communication.game.jsonElementToRoundStats
import java.util.UUID


data class GameDataModel(
    @BsonId
    val id: UUID, // Primary Key
    val userId: String? = null, // Foreign Key -> Users, Index
    val opponentId: String? = null, // Foreign Key -> Users, Index
    val round1Statistics: JsonElement,
    val round2Statistics: JsonElement,
    val timeStamp: String,
    val gameConfiguration: String,
    var userSaved: Boolean = false
)

//fun modelToData(model: GameDataModel): GameDataStore {
//    return GameDataStore(
//        model.id.toString(),
//        jsonElementToRoundStats(model.round1Statistics),
//        jsonElementToRoundStats(model.round2Statistics),
//        model.gameConfiguration,
//        model.userId,
//        model.opponentId,
//        model.timeStamp,
//        model.userSaved
//    )
//}

fun dataToModel(data: GameDataStore): GameDataModel {
    if (data.id == null) {
        return GameDataModel(
            UUID.randomUUID(),
            data.userId,
            data.opponentId,
            Json.encodeToJsonElement(data.round1Statistics),
            Json.encodeToJsonElement(data.round2Statistics),
            data.currentTimestamp,
            data.gameConfiguration,
            data.userSaved
        )
    } else {
        return GameDataModel(
            UUID.fromString(data.id),
            data.userId,
            data.opponentId,
            Json.encodeToJsonElement(data.round1Statistics),
            Json.encodeToJsonElement(data.round2Statistics),
            data.currentTimestamp,
            data.gameConfiguration,
            data.userSaved
        )
    }
}