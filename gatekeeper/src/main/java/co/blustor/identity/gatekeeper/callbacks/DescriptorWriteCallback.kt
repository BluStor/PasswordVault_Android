package co.blustor.identity.gatekeeper.callbacks

interface DescriptorWriteCallback {

    fun onDescriptorNotFound()

    fun onDescriptorWrite(status: Int)

    fun onNotConnected()

    fun onTimeout()
}
