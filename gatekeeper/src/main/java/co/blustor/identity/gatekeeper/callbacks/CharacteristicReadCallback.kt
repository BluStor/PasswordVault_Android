package co.blustor.identity.gatekeeper.callbacks

interface CharacteristicReadCallback {

    fun onCharacteristicNotFound()

    fun onCharacteristicRead(status: Int, value: ByteArray)

    fun onDisconnected()

    fun onTimeout()
}
