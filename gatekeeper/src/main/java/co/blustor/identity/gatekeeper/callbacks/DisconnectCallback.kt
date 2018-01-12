package co.blustor.identity.gatekeeper.callbacks

interface DisconnectCallback {

    fun onDisconnected()

    fun onNotConnected()

    fun onTimeout()
}
