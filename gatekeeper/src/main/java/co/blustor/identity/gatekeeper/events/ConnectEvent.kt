package co.blustor.identity.gatekeeper.events

import android.content.Context
import co.blustor.identity.gatekeeper.BluetoothConstants.EventName
import co.blustor.identity.gatekeeper.callbacks.ConnectCallback

class ConnectEvent(val context: Context, val address: String, val callback: ConnectCallback) : Event {

    override val name = EventName.CONNECT
}
