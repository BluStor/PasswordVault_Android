package co.blustor.identity.gatekeeper.callbacks

interface DiscoverServicesCallback {

    fun onServicesDiscovered(status: Int)

    fun onNotConnected()

    fun onTimeout()
}
