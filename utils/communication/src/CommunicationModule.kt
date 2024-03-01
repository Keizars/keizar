package org.keizar.utils.communication

import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.keizar.utils.communication.message.ChangeBoard
import org.keizar.utils.communication.message.ConfirmNextRound
import org.keizar.utils.communication.message.Exit
import org.keizar.utils.communication.message.Message
import org.keizar.utils.communication.message.Move
import org.keizar.utils.communication.message.RemoteSessionSetup
import org.keizar.utils.communication.message.Request
import org.keizar.utils.communication.message.Respond
import org.keizar.utils.communication.message.SetReady
import org.keizar.utils.communication.message.PlayerStateChange
import org.keizar.utils.communication.message.RoomStateChange
import org.keizar.utils.communication.message.UserInfo

val CommunicationModule = SerializersModule {
    polymorphic(Message::class) {
        subclass(UserInfo::class)
        subclass(PlayerStateChange::class)
        subclass(RoomStateChange::class)
        subclass(RemoteSessionSetup::class)
        subclass(Exit::class)
        subclass(ConfirmNextRound::class)
        subclass(Move::class)
        subclass(ChangeBoard::class)
        subclass(SetReady::class)
    }
    polymorphic(Request::class) {
        subclass(UserInfo::class)
        subclass(Exit::class)
        subclass(ConfirmNextRound::class)
        subclass(Move::class)
        subclass(ChangeBoard::class)
        subclass(SetReady::class)
    }
    polymorphic(Respond::class) {
        subclass(PlayerStateChange::class)
        subclass(RoomStateChange::class)
        subclass(RemoteSessionSetup::class)
        subclass(ConfirmNextRound::class)
        subclass(Move::class)
    }
}
