package co.blustor.identity.gatekeeper

import android.util.Log
import java.io.PrintWriter
import java.io.StringWriter


object BluetoothLog {
    private val tag = "BluetoothLog"

    fun i(msg: String) {
        android.util.Log.i(tag, msg)
    }

    fun e(msg: String) {
        Log.e(tag, msg)
    }

    fun v(msg: String) {
        Log.v(tag, msg)
    }

    fun d(msg: String) {
        Log.d(tag, msg)
    }

    fun w(msg: String) {
        Log.w(tag, msg)
    }

    fun w(e: Throwable) {
        w(getThrowableString(e))
    }

    fun e(e: Throwable) {
        e(getThrowableString(e))
    }

    private fun getThrowableString(e: Throwable): String {
        val writer = StringWriter()
        val printWriter = PrintWriter(writer)

        e.printStackTrace(printWriter)

        val text = writer.toString()

        printWriter.close()

        return text
    }
}
