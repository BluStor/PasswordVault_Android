package co.blustor.identity.gatekeeper.events

import co.blustor.identity.gatekeeper.BluetoothConstants.EventName

interface Event {

    val name: EventName
}
