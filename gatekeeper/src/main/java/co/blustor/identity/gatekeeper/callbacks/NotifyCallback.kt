package co.blustor.identity.gatekeeper.callbacks

import java.util.*

interface NotifyCallback {

    fun onNotify(serviceUUID: UUID, characteristicUUID: UUID, value: ByteArray)
}
