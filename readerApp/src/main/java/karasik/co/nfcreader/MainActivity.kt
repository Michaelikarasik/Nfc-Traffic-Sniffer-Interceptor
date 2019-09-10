package karasik.co.nfcreader

import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOError
import java.net.NetworkInterface
import java.net.Socket
import java.net.NetworkInterface.getNetworkInterfaces
import java.net.ServerSocket
import java.net.SocketException


class MainActivity : AppCompatActivity(), NfcAdapter.ReaderCallback{

    var SERVERPORT = 9753
    lateinit var socket : Socket
    var myip = "10.0.0.13"
    var commands = arrayOf("00A4040008315449432E494341", "948A8A3804718C793C", "94B201CC1D", "94B201F41D", "94B201441D", "94B202441D", "94B203441D", "94B204441D", "94B205441D", "94B206441D", "94B2014C1D", "94B2024C1D", "94B2034C1D", "94B2044C1D", "94B2054C1D", "94B2064C1D", "94B2074C1D", "94B2084C1D", "94B201EC1D", "94B202EC1D", "94B203EC1D", "94B204EC1D", "94DC014C1D0FD487AC6815BF48EE91401813464780000000000000000000000000B8", "94E200401D01E233550F359800000004EEEEEEE207E9000000000000000000000000", "94E200401D01E23B550F359AAA11FCB4EEEEEEE207E9000000000000000000000000", "943201C80300186A", "948E0000045A3339F4", "0084000009")
    lateinit var currentIsoDep : IsoDep

    private var nfcAdapter: NfcAdapter? = null
    var total = ""
    var response = ""

    inner class ServerThread : Runnable {

        override fun run() {
            if(!this@MainActivity::socket.isInitialized) {
                while (true) {
                    try {
                        Log.d("log", "trying to connect to " + SERVERPORT.toString() + " at ip " + myip)
                        Thread.sleep(100)
                        socket = Socket(myip, SERVERPORT)
                        Log.d("connected to", SERVERPORT.toString())
                        break
                    } catch (e: Exception) { }
                }
            }

            try {
                val inp = DataInputStream(socket.getInputStream())
                val out = DataOutputStream(socket.getOutputStream())
                while (true) {
                    try {
                        Thread.sleep(200)
                        var query = inp.readUTF()
                        var response = Utils.toHex(currentIsoDep.transceive(Utils.hexStringToByteArray(query)))
                        if(query.startsWith("948E000004")){
                            var new = "\nreceived: " + query + "\nDIDN'T respond with: " + response
                            Log.d("log", new)
                            total += new
                            var response = Utils.toHex(currentIsoDep.transceive(Utils.hexStringToByteArray("0084000009")))
                            new = "\nsent 0084000009\nDIDN'T respond with: " + response
                            Log.d("log", new)
                            total += new
                            continue
                        }
                        out.writeUTF(response)
                        var new = "\nreceived: " + query + "\nresponded: " + response
                        Log.d("log", new)
                        total += new
                    }
                    catch(e: Exception){
                        var mye = e
                    }
                }
            }
            catch(e : java.lang.Exception){
                var mye = e
            }
        }
    }

    fun doSocket(){
        var socketInit = Thread(ServerThread())
        socketInit.start()
        socketInit.join()
    }

    fun doCommandArray(){
        for(command in commands){
            response = Utils.toHex(currentIsoDep.transceive(Utils.hexStringToByteArray(command)))
            total += "\nsent: " + command + "\nreceived: " + response
        }
    }

    override fun onTagDiscovered(tag: Tag?) {
        val isoDep = IsoDep.get(tag)
        isoDep.connect()

        total = ""
        currentIsoDep = isoDep
        myip = textInput.text.toString()

        doSocket()

        runOnUiThread { textView.setText(total) }
        isoDep.close()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    public override fun onResume() {
        super.onResume()
        nfcAdapter?.enableReaderMode(this, this,
            NfcAdapter.FLAG_READER_NFC_B or
                    NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK or NfcAdapter.FLAG_READER_NFC_B,
            null)
    }

    public override fun onPause() {
        super.onPause()
        nfcAdapter?.disableReaderMode(this)
    }
}