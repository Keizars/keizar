package org.keizar.server.database.models

import org.bson.codecs.pojo.annotations.BsonId

data class SeedBankModel(
    @BsonId
    val userId: String, // Foreign Key -> Users, Index
    val gameSeed: String,
)
