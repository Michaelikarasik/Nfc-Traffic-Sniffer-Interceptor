package com.example.myhostcardemulator;

import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

class HostCardEmulatorService extends HostApduService {

    final int SERVERPORT = 9753;
    final String TAG = "Host Card Emulator Log";
    final String STATUS_FAILED = "6F00";

    String globalResponse = "";
    String globalCommand = "";

    static ServerSocket serverSocket;
    static Socket socket;

    /**
     * Void called on NFC connection was deactivation
     * @param reason Reason for deactivation
     */
    public void onDeactivated(int reason) {
        Log.d(TAG, "Deactivated: " + reason);
    }


    /**
     * Thread for setting up socket connection to other phone
     * and transmitting APDU queries.
     */
    private class ServerThread implements Runnable {

        /**
         * Method for initializing the ServerSocket
         * and the socket connecting to the other phone
         * @throws IOException
         */
        void socketInit() throws IOException {
            if(serverSocket == null){
                Log.d(TAG, "init server socket");
                serverSocket = new ServerSocket(SERVERPORT);
            }
            if(socket == null){
                Log.d(TAG, "init socket");
                socket = serverSocket.accept();
            }
        }

        /**
         * In its run method the thread initializes the sockets
         * if needed, writes the newly received APDU command
         * to the other phone and waits for an answer
         */
        public void run() {
            try {
                socketInit();

                DataInputStream inp = new DataInputStream(socket.getInputStream());
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());

                Log.d(TAG, "writing");
                out.writeUTF(globalCommand);

                Log.d(TAG, "reading");
                globalResponse = inp.readUTF();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This method will be called when a command APDU has been received from a remote device.
     * A response APDU can be provided directly by returning a byte-array in this method.
     * @param commandApdu The APDU that was received from the remote device
     * @param extras A bundle containing extra data. May be null.
     * @return A byte-array containing the response APDU, or null if no response APDU can be sent at this point.
     */
     public byte[] processCommandApdu(byte[] commandApdu, Bundle extras) {
         if (commandApdu == null)
             return Utils.hexStringToByteArray(STATUS_FAILED);

         //ip = ((EditText) new MainActivity().findViewById(R.id.myTextInput)).getText().toString();
         //Log.d(TAG, "found ip " + ip);
         String hexCommandApdu = Utils.toHex(commandApdu);
         globalCommand = hexCommandApdu;
         Thread serverInit = new Thread(new ServerThread());
         try {
            serverInit.start();
            serverInit.join();
         } catch (Exception e) {
             e.printStackTrace();
         }

         return Utils.hexStringToByteArray(globalResponse);
     }
}