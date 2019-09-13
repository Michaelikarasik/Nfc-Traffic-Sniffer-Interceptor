package com.example.myhostcardemulator;

import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;

class HostCardEmulatorService extends HostApduService {

    final int SERVERPORT = 9753;
    final String TAG = "Host Card Emulator Log";
    final String STATUS_FAILED = "6F00";

    String globalResponse = "";

    static ServerSocket serverSocket;
    static Socket socket;
    static CommandPipeline myPipeline;

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

        private String query;

        /**
         * Thread constructor
         * @param query Query to send to card
         */
        public ServerThread(String query){
            this.query = query;
        }

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
                out.writeUTF(query);

                Log.d(TAG, "reading");
                globalResponse = inp.readUTF();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Initialize and start a new command sending thread
     * @param query Query for thread to send
     */
    public void sendApduCommand(String query) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        myPipeline = new CommandPipeline();
        myPipeline.addStepToEnd(0, 1, "A4");
        myPipeline.addStepToEnd(1, 2, "00A40000", 2);
        myPipeline.addStepToEnd(2, 0);
        query = myPipeline.performPipeline(query);
        Thread commandSender = new Thread(new ServerThread(query));
        try {
            commandSender.start();
            commandSender.join();
        } catch (Exception e) {
            e.printStackTrace();
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
         try {
             sendApduCommand(hexCommandApdu);
         }
         catch(Exception e){
            e.printStackTrace();
         }

         return Utils.hexStringToByteArray(globalResponse);
     }
}