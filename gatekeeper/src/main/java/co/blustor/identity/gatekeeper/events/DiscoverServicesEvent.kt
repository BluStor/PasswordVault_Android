package co.blustor.identity.gatekeeper.events

import co.blustor.identity.gatekeeper.BluetoothConstants.EventName
import co.blustor.identity.gatekeeper.callbacks.DiscoverServicesCallback

class DiscoverServicesEvent(val callback: DiscoverServicesCallback) : Event {

    override val name = EventName.DISCOVER_SERVICES
}
