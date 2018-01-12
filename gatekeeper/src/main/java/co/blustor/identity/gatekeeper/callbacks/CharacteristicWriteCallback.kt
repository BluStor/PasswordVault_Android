package co.blustor.identity.gatekeeper.callbacks

interface CharacteristicWriteCallback {

    fun onCharacteristicNotFound()

    fun onCharacteristicWrite(status: Int)

    fun onNotConnected()

    fun onTimeout()
}
