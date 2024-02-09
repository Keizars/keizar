package org.keizar.utils.communication

import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.keizar.utils.communication.message.ConfirmNextRound
import org.keizar.utils.communication.message.Exit
import org.keizar.utils.communication.message.Message
import org.keizar.utils.communication.message.Move
import org.keizar.utils.communication.message.PlayerAllocation
import org.keizar.utils.communication.message.Request
import org.keizar.utils.communication.message.Respond
import org.keizar.utils.communication.message.StateChange
import org.keizar.utils.communication.message.UserInfo

val CommunicationModule = SerializersModule {
    polymorphic(Message::class) {
        subclass(UserInfo::class)
        subclass(StateChange::class)
        subclass(PlayerAllocation::class)
        subclass(Exit::class)
        subclass(ConfirmNextRound::class)
        subclass(Move::class)
    }
    polymorphic(Request::class) {
        subclass(UserInfo::class)
        subclass(Exit::class)
        subclass(ConfirmNextRound::class)
        subclass(Move::class)
    }
    polymorphic(Respond::class) {
        subclass(StateChange::class)
        subclass(PlayerAllocation::class)
        subclass(ConfirmNextRound::class)
        subclass(Move::class)
    }
}
