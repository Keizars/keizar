package org.keizar.server.database.mongodb

import com.mongodb.client.model.Filters
import org.bson.conversions.Bson

infix fun Bson.and(other: Bson): Bson {
    return Filters.and(this, other)
}

data class Field(val name: String) {
    infix fun <TItem> eq(value: TItem): Bson {
        return Filters.eq(name, value)
    }
}