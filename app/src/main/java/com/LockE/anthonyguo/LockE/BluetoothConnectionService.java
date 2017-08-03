package com.LockE.anthonyguo.LockE;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by Anthony Guo on 7/5/2017.
 */

public class BluetoothConnectionService {
    private static final String TAG = "BluetoothConnectionServ";
    private static final String appName = "App";

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final BluetoothAdapter mBluetoothAdapter;
    Context mContext;

    private AcceptThread mInsecureAcceptThread;
    private ConnectThread mConnectThread;
    private BluetoothDevice mmDevice;
    private UUID deviceUUID;
    private ConnectedThread mConnectedThread;

    public BluetoothConnectionService(Context context) {
        mContext = context;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        start();
    }

    private class AcceptThread extends  Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread(){
            BluetoothServerSocket temp = null;

            try {
                temp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(appName, MY_UUID);
                //Log.d(TAG, "AcceptThread: Setting up server " + MY_UUID);
            } catch (IOException e){

            }
            mmServerSocket = temp;
        }

        public void run() {
            //Log.d(TAG, "run: AcceptThread Running");
            BluetoothSocket socket = null;
            try {
                //Log.d(TAG, "run: RFcom server socket start");
                socket = mmServerSocket.accept();
                //Log.d(TAG, "run: RFcom accepted connection");
            } catch (IOException e) {
                //Log.e(TAG, "Accept thread: IOexception" + e.getMessage());
            }
            if(socket != null) {
                connected(socket,mmDevice);
            }
        }
        public void cancel() {
            //Log.d(TAG, "cancel: Cancel AcceptThread");
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                //Log.e(TAG, "cancel: Close failed" + e.getMessage() );
            }
        }
    }

    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;

        public ConnectThread(BluetoothDevice device, UUID uuid) {
            //Log.d(TAG, "ConnectThread: started");
            mmDevice = device;
            deviceUUID = uuid;
        }

        public void run() {
            BluetoothSocket temp = null;
            //Log.i(TAG, "run: mmConnectThread");

            try {
                //Log.d(TAG, "Trying to create rfcomm");
                temp = mmDevice.createRfcommSocketToServiceRecord(deviceUUID);
            } catch (IOException e) {
                //Log.e(TAG, "ConnectThread run fail");
            }
            mmSocket = temp;
            mBluetoothAdapter.cancelDiscovery();
            String state;
            try {
                mmSocket.connect();
                //Log.d(TAG, "run: Connection Successful");
                state = "Connected";
            } catch (IOException e) {
                try {
                    mmSocket.close();
                    //Log.d(TAG, "run: close socket");
                } catch (IOException e1) {
                    //Log.e(TAG, "run: fail to close socket");
                }
                //Log.d(TAG, "run: cannot connect to socket");
                state = "Disconnected";
            }
            Intent BTConnectionStatus = new Intent("BTStatus");
            BTConnectionStatus.putExtra("State", state);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(BTConnectionStatus);

            connected(mmSocket,mmDevice);
        }
        public void cancel () {
            try {
                //Log.d(TAG, "cancel: Closing client socket");
                mmSocket.close();
            } catch (IOException e) {
                //Log.e(TAG, "cancel: close() of mmSocket in connect thread fail" + e.getMessage());
            }
        }
    }

    public synchronized void start() {
        //Log.d(TAG, "start");

        if(mConnectThread !=null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if(mInsecureAcceptThread == null) {
            mInsecureAcceptThread = new AcceptThread();
            mInsecureAcceptThread.start();
        }
    }

    public void startClient(BluetoothDevice device, UUID uuid) {
        //Log.d(TAG, "startClient: Started");

        mConnectThread = new ConnectThread(device, uuid);
        mConnectThread.start();
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            //Log.d(TAG, "ConnectedThread: Starting");

            mmSocket = socket;
            InputStream tempIn = null;
            OutputStream tempOut = null;

            try {
                tempIn = mmSocket.getInputStream();
                tempOut = mmSocket.getOutputStream();
            } catch (IOException e) {
                //Log.e(TAG, "ConnectedThread: Failed" );
            }

            mmInStream = tempIn;
            mmOutStream = tempOut;
        }

        public void run() {
            byte [] buffer = new byte[256];
            int bytes;

            while(true) {
                try {
                    bytes = mmInStream.read(buffer);
                    String readMessage = new String(buffer,0,bytes);
                    //Log.d(TAG, "run: " + readMessage);

                    Intent incomingMessageIntent = new Intent("incomingMessage");
                    incomingMessageIntent.putExtra("Message", readMessage);
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(incomingMessageIntent);
                } catch (IOException e) {
                    break;
                }
            }
        }

        public void write(String message) {
            byte[] msgBuffer = message.getBytes();
            //Log.d(TAG, "write: Writing to outputstream" + msgBuffer);
            try {
                mmOutStream.write(msgBuffer);
            } catch (IOException e) {
                Intent writeIntent = new Intent("WriteOutIntent");
                writeIntent.putExtra("writeMessage", "WriteFail");
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(writeIntent);
                //Log.e(TAG, "write: error writing to output stream");
            }

        }

        public void cancel() {
            try {
                mmSocket.close();
                //Log.d(TAG, "cancel: Socket Closed");
            } catch (IOException e) {
                //Log.e(TAG, "cancel: failed cancel");
            }
        }
    }

    private void connected(BluetoothSocket mmSocket, BluetoothDevice mmDevice) {
        //Log.d(TAG, "connected: Starting");

        mConnectedThread = new ConnectedThread(mmSocket);
        mConnectedThread.start();
    }

    public void disconnect() {
        //Log.d(TAG, "disconnect: called");
        mConnectedThread.cancel();
    }

    public void write(String out) {
        //Log.d(TAG, "write: Write Called");
        mConnectedThread.write(out);
    }
}
