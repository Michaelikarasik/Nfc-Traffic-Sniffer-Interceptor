package com.example.myhostcardemulator;

import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.lang.Exception;
import java.lang.NullPointerException;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import android.widget.EditText;
import android.support.v7.app.AppCompatActivity;

class HostCardEmulatorService extends HostApduService {

        int SERVERPORT = 9753;
        String TAG = "Host Card Emulator Log";
        String STATUS_SUCCESS = "9000";
        String STATUS_FAILED = "6F00";
        String CLA_NOT_SUPPORTED = "6E00";
        String INS_NOT_SUPPORTED = "6D00";
        String SELECT_INS = "A4";
        String READ_RECORD = "B2";
        String DEFAULT_CLA = "00";

        String globalResponse = "";
        String globalCommand = "";
        ServerSocket serverSocket;
        Socket socket;
        String ip = "";

        public void onDeactivated(int reason) {
            Log.d(TAG, "Deactivated: " + reason);
        }

        private class ServerThread implements Runnable {
            void socketInit() throws IOException {
                serverSocket = new ServerSocket(SERVERPORT);
                socket = serverSocket.accept();
            }

            public void run() {
                if(serverSocket == null) {
                    try {
                        socketInit();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                while (true) {
                    try {
                        DataInputStream inp = new DataInputStream(socket.getInputStream());
                        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                        out.writeUTF(globalCommand);
                        globalResponse = inp.readUTF();
                        break;
                    }
                    catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

         public byte[] processCommandApdu(byte[] commandApdu, Bundle extras) {
             if (commandApdu == null)
                 return Utils.hexStringToByteArray(STATUS_FAILED);


             ip = ((EditText) new MainActivity().findViewById(R.id.myTextInput)).getText().toString();
             Log.d(TAG, "found ip " + ip);
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