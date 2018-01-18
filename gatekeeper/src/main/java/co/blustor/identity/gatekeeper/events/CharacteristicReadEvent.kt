package co.blustor.identity.gatekeeper.events

import co.blustor.identity.gatekeeper.BluetoothConstants
import co.blustor.identity.gatekeeper.callbacks.CharacteristicReadCallback
import java.util.*

class CharacteristicReadEvent(
    val serviceUUID: UUID, val characteristicUUID: UUID, val callback: CharacteristicReadCallback
) : Event {

    override val name = BluetoothConstants.EventName.CHARACTERISTIC_READ
}
