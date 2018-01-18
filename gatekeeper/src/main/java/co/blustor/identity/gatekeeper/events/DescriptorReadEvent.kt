package co.blustor.identity.gatekeeper.events

import co.blustor.identity.gatekeeper.BluetoothConstants
import co.blustor.identity.gatekeeper.callbacks.DescriptorReadCallback
import java.util.*

class DescriptorReadEvent(
    val serviceUUID: UUID, val characteristicUUID: UUID, val descriptorUUID: UUID, val callback: DescriptorReadCallback
) : Event {

    override val name = BluetoothConstants.EventName.DESCRIPTOR_READ
}
