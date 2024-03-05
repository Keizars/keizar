package org.keizar.server.database.models

data class SeedBankModel(
    val userId: String, // Foreign Key -> Users, Index
    val gameSeed: String,
)
