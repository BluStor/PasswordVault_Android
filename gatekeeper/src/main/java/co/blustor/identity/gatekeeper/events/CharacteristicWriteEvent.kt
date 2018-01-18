package co.blustor.identity.gatekeeper.events

import co.blustor.identity.gatekeeper.BluetoothConstants.EventName
import co.blustor.identity.gatekeeper.callbacks.CharacteristicWriteCallback
import java.util.*

class CharacteristicWriteEvent(
    val serviceUUID: UUID, val characteristicUUID: UUID, val value: ByteArray, val callback: CharacteristicWriteCallback
) : Event {

    override val name = EventName.CHARACTERISTIC_WRITE
}
