package co.blustor.identity.gatekeeper.events

import co.blustor.identity.gatekeeper.BluetoothConstants.EventName
import co.blustor.identity.gatekeeper.callbacks.RequestMtuCallback

class RequestMtuEvent(val mtu: Int, val callback: RequestMtuCallback) : Event {

    override val name = EventName.REQUEST_MTU
}
