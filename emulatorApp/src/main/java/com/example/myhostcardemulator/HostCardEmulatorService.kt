package com.example.myhostcardemulator

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.os.Handler
import android.util.Log
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOError
import java.io.IOException
import java.lang.Exception
import java.lang.Long.parseLong
import java.lang.NullPointerException
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException
import android.system.Os.accept
import android.widget.EditText
import com.example.myhostcardemulator.HostCardEmulatorService.Companion.STATUS_FAILED
import kotlinx.android.synthetic.main.activity_main.*
import android.support.v7.app.AppCompatActivity

class HostCardEmulatorService: HostApduService() {

    /**
     * Server
     */
    val SERVERPORT = 9753
    companion object {
        val TAG = "Host Card Emulator Log"
        val STATUS_SUCCESS = "9000"
        val STATUS_FAILED = "6F00"
        val CLA_NOT_SUPPORTED = "6E00"
        val INS_NOT_SUPPORTED = "6D00"
        val SELECT_INS = "A4"
        val READ_RECORD = "B2"
        val DEFAULT_CLA = "00"
    }

    var globalResponse = ""
    var globalCommand = ""
    lateinit var serverSocket : ServerSocket
    lateinit var socket : Socket
    var ip = ""

    override fun onDeactivated(reason: Int) {
        Log.d(TAG, "Deactivated: " + reason)
    }

    inner class ServerThread : Runnable {

        fun socketInit(){
            serverSocket = ServerSocket(SERVERPORT)
            socket = serverSocket.accept()
        }

        override fun run() {
            if(!this@HostCardEmulatorService::serverSocket.isInitialized)
                socketInit()

            while (true) {
                try {
                    val inp = DataInputStream(socket.getInputStream())
                    val out = DataOutputStream(socket.getOutputStream())
                    out.writeUTF(globalCommand)
                    globalResponse = inp.readUTF()
                    break
                }
                catch(e : Exception) {
                    var mye = e
                }
            }
        }
    }

    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray {
        if (commandApdu == null)
            return Utils.hexStringToByteArray(STATUS_FAILED)


        ip = MainActivity().myTextInput.text.toString()
        Log.d(TAG, ip)
        val hexCommandApdu = Utils.toHex(commandApdu)
        globalCommand = hexCommandApdu
        var serverInit = Thread(ServerThread())
        serverInit.start()
        serverInit.join()

        return Utils.hexStringToByteArray(globalResponse)
    }
}