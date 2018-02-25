package sk.stuba.fei.uamt.diplomaswork;

import android.app.Application;
import android.bluetooth.BluetoothSocket;

import java.io.InputStream;

/**
 * Created by Zendo on 25.2.2018.
 */

public class BluetoothSocketState extends Application{
   private BluetoothSocket mmSocket;
    private InputStream mmInStream;

    public void setBluetoothSocket(BluetoothSocket mmSocket){
       this.mmSocket = mmSocket;
    }

    public void setInputStream(InputStream mmInStream){
        this.mmInStream = mmInStream;
    }

    public BluetoothSocket getBluetoothSocket(){
        return this.mmSocket;
    }

    public InputStream getInputStream(){
        return this.mmInStream;
    }


}
