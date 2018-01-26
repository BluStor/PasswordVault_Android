package co.blustor.identity.gatekeeper.events

import co.blustor.identity.gatekeeper.BluetoothConstants.EventName
import co.blustor.identity.gatekeeper.callbacks.DisconnectCallback

class DisconnectEvent(val callback: DisconnectCallback) : Event {

    override val name = EventName.DISCONNECT
}
