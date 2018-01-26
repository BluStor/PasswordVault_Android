package co.blustor.identity.gatekeeper.callbacks

interface ConnectCallback {

    fun onConnectionStateChange(status: Int, newState: Int)

    fun onBluetoothNotSupported()

    fun onTimeout()
}
