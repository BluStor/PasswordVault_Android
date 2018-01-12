package co.blustor.identity.gatekeeper.events

import co.blustor.identity.gatekeeper.BluetoothConstants.EventName
import co.blustor.identity.gatekeeper.callbacks.DescriptorWriteCallback
import java.util.*

class DescriptorWriteEvent(val serviceUUID: UUID, val characteristicUUID: UUID, val descriptorUUID: UUID, val value: ByteArray, val callback: DescriptorWriteCallback) : Event {

    override val name = EventName.DESCRIPTOR_WRITE
}
