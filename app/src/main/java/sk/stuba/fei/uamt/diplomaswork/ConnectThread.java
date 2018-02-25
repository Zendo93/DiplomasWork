package sk.stuba.fei.uamt.diplomaswork;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * Created by Zendo on 14.2.2018.
 */

public class ConnectThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
    private final InputStream mmInStream;
    private byte[] mmBuffer; // mmBuffer store for the stream
    private Handler mHandler;
    private Context context;

    public ConnectThread(BluetoothDevice device, UUID uuid, Context context) throws IOException {
        // Use a temporary object that is later assigned to mmSocket
        // because mmSocket is final.
        InputStream tmpIn = null;
        BluetoothSocket tmp = null;
        mmDevice = device;
        this.context = context;

        try {
            // Get a BluetoothSocket to connect with the given BluetoothDevice.
            // MY_UUID is the app's UUID string, also used in the server code.
            tmp = device.createRfcommSocketToServiceRecord(uuid);
        } catch (IOException e) {
            Log.e("connect", "Socket's create() method failed", e);
        }
        mmSocket = tmp;

        tmpIn = mmSocket.getInputStream();
        mmInStream = tmpIn;
    }

    public void run() {
        // Cancel discovery because it otherwise slows down the connection.
        //mBluetoothAdapter.cancelDiscovery();

        try {
            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.
            mmSocket.connect();
        } catch (IOException connectException) {
            // Unable to connect; close the socket and return.
            try {
                mmSocket.close();
            } catch (IOException closeException) {
                Log.e("connect", "Could not close the client socket", closeException);
            }
            return;
        }

        BluetoothSocketState state = ((BluetoothSocketState) context.getApplicationContext());
        state.setBluetoothSocket(mmSocket);
        state.setInputStream(mmInStream);
        context.startActivity(new Intent(context,ProgressActivity.class));
    }
    // Closes the client socket and causes the thread to finish.
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e("connect", "Could not close the client socket", e);
        }
    }
    public BluetoothSocket getMmSocket(){
        return mmSocket;
    }
}
