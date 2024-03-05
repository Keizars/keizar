package org.keizar.server.database.models

import org.bson.codecs.pojo.annotations.BsonId
import java.util.UUID

data class SeedBankModel(
    @BsonId
    val id: UUID = UUID.randomUUID(),
    val userId: String, // Foreign Key -> Users, Index
    val gameSeed: String,
)
