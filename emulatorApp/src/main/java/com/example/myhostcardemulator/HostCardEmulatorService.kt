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
import com.example.myhostcardemulator.HostCardEmulatorService.Companion.STATUS_FAILED

class HostCardEmulatorService: HostApduService() {

    var selectedRecord = ""
    var total = ""
    var SERVERPORT = 9753
    var globalResponse = ""
    var globalCommand = ""
    lateinit var serverSocket : ServerSocket
    lateinit var socket : Socket

    companion object {
        val TAG = "Host Card Emulator Log"
        val STATUS_SUCCESS = "9000"
        val STATUS_FAILED = "6F00"
        val CLA_NOT_SUPPORTED = "6E00"
        val INS_NOT_SUPPORTED = "6D00"
        val SELECT_INS = "A4"
        val READ_RECORD = "B2"
        val DEFAULT_CLA = "00"

        val EMPTY_RESPONSE = "00000000000000000000000000000000000000000000000000000000009000"
        val CARD_CALYPSO = "6F228408315449432E494341A516BF0C13C70800000000C12A45575307060A070620042D9000"
        val CARD_ENVIRONMENT = "06EC1EA00125BDBD529120030214000000000000000009218CC00000009000"
        var CARD_COUNTERS = "0014DC00000000000000000000000000000000000000000000000000009000"
        val CARD_EVENTS = arrayOf("006223543C0F7AA8781EF4EEEEEEEB69EA6523B06849015B1D4B00E8829000", "01E24354352592A86A4B24EEEEEEEA6D1DE002A4BB15B1D4A8093820009000", "01E22354159EA2A82B3D44EEEEEEEA6D39600600E818B30CAC093820009000", "00622354117DD2A822FBA4EEEEEEEB6A90252470986B198B1D4B00E8829000", "01E23B54117CBAA7D654C4EEEEEEE207E90000000000000000000000009000", "01E23354117CB800000004EEEEEEE207E90000000000000000000000009000")
        val CARD_CONTRACTS = arrayOf("0FE087AC680FBF49B691401C32064780000000000000000000000000B79000", "00000000000000000000000000000000000000000000000000000000009000", "00000000000000000000000000000000000000000000000000000000009000", "00000000000000000000000000000000000000000000000000000000009000", "00000000000000000000000000000000000000000000000000000000009000", "00000000000000000000000000000000000000000000000000000000009000", "00000000000000000000000000000000000000000000000000000000009000", "00000000000000000000000000000000000000000000000000000000009000")
        val CARD_SPECIAL_EVENTS = arrayOf("006223543C0F7AA8781EF4EEEEEEEB69EA6523B06849015B1D4B00E8829000", "00000000000000000000000000000000000000000000000000000000009000", "00000000000000000000000000000000000000000000000000000000009000", "00000000000000000000000000000000000000000000000000000000009000", "00000000000000000000000000000000000000000000000000000000009000", "00000000000000000000000000000000000000000000000000000000009000", "00000000000000000000000000000000000000000000000000000000009000", "00000000000000000000000000000000000000000000000000000000009000")
        var CARD_ENCRYPTION_ANSWER = "60030D16ED200006EC1EA00125BDBD529120030214000000000000000009218CC00000009000"
    }

    private fun getLocalIpAddress(): String? {
        try {
            val en = NetworkInterface.getNetworkInterfaces()
            while (en.hasMoreElements()) {
                val intf = en.nextElement()
                val enumIpAddr = intf.getInetAddresses()
                while (enumIpAddr.hasMoreElements()) {
                    val inetAddress = enumIpAddr.nextElement()
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString()
                    }
                }
            }
        } catch (ex: SocketException) {
            Log.e("ServerActivity", ex.toString())
        }

        return null
    }

    override fun onDeactivated(reason: Int) {
        Log.d(TAG, "Deactivated: " + reason)
    }

    override fun onCreate(){
        Log.d(TAG, "started")
    }

    inner class ServerThread : Runnable {

        override fun run() {
            if(!this@HostCardEmulatorService::serverSocket.isInitialized){
                serverSocket = ServerSocket(SERVERPORT)
                socket = serverSocket.accept()
            }
            while (true) {
                try {
                    val inp = DataInputStream(socket.getInputStream())
                    val out = DataOutputStream(socket.getOutputStream())
                    out.writeUTF(globalCommand)
                    globalResponse = inp.readUTF()
                    break
                }
                catch(e : Exception){
                    var mye = e
                }
            }
        }
    }

    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray {
        if (commandApdu == null)
            return Utils.hexStringToByteArray(STATUS_FAILED)

        val hexCommandApdu = Utils.toHex(commandApdu)
        globalCommand = hexCommandApdu
        var serverInit = Thread(ServerThread())
        serverInit.start()
        serverInit.join()

        return Utils.hexStringToByteArray(globalResponse)
    }
}