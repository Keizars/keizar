package org.keizar.server.database

import org.keizar.server.database.local.InMemorySeedBankDbControl
import org.keizar.server.database.local.InMemoryUserDbControl

interface DatabaseManager {
    val user: UserDbControl
    val seedBank: SeedBankDbControl
}

class InMemoryDatabaseManagerImpl : DatabaseManager {
    override val user: UserDbControl = InMemoryUserDbControl()
    override val seedBank: SeedBankDbControl = InMemorySeedBankDbControl()
}
