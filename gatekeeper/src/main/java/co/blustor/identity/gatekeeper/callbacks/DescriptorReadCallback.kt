package co.blustor.identity.gatekeeper.callbacks

interface DescriptorReadCallback {

    fun onDescriptorNotFound()

    fun onDescriptorRead(status: Int, value: ByteArray)

    fun onNotConnected()

    fun onTimeout()
}
