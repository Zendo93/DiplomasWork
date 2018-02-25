package sk.stuba.fei.uamt.diplomaswork;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Zendo on 14.2.2018.
 */

public class ProcessThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private byte[] mmBuffer;
    private TextView humidity;

    public ProcessThread(BluetoothSocket mmSocket, InputStream mmInStream, TextView humidity) {
        this.mmSocket = mmSocket;
        this.mmInStream = mmInStream;
        this.humidity = humidity;
    }


    public void run() {
        mmBuffer = new byte[1024];
        int numBytes; // bytes returned from read()

        // Keep listening to the InputStream until an exception occurs.
        while (true) {
            try {
                Log.e("BT","connected");
                // Read from the InputStream.
                numBytes = mmInStream.read(mmBuffer);
                // Send the obtained bytes to the UI activity.
                Log.e("BT",Integer.toString(numBytes));
                 final String message = new String(mmBuffer,0,numBytes);
                Log.e("BT",message);
                displyHumidity(humidity,message);
            } catch (IOException e) {
                Log.d("error", "Input stream was disconnected", e);
                break;
            }
        }
    }

    public void displyHumidity(final TextView humidity, final String text) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            public void run() {
                humidity.setText(text);
            }
        });
    }

    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e("connect", "Could not close the client socket", e);
        }
    }
}
