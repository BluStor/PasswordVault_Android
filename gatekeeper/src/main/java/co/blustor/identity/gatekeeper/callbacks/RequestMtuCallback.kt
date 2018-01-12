package co.blustor.identity.gatekeeper.callbacks

interface RequestMtuCallback {

    fun onMtuChanged(mtu: Int, status: Int)

    fun onNotConnected()

    fun onTimeout()
}
