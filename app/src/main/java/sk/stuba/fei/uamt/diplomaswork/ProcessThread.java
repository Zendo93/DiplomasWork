package sk.stuba.fei.uamt.diplomaswork;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Zendo on 14.2.2018.
 */

public class ProcessThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private byte[] mmBuffer;
    private TextView temperature;
    private TextView humidity;
    private int counter;
    private boolean startSaving = false;
    private boolean endSaving = true;
    private LineGraphSeries<DataPoint> series;
    private DataPoint[] graphValues;
    private int index=1;

    public ProcessThread(BluetoothSocket mmSocket, InputStream mmInStream,LineGraphSeries<DataPoint> series,DataPoint[] graphValues, TextView temperature, TextView humidity) {
        this.mmSocket = mmSocket;
        this.mmInStream = mmInStream;
        this.series = series;
        this.graphValues = graphValues;
        this.temperature = temperature;
        this.humidity = humidity;
        this.counter = 0;
    }


    public void run() {
        mmBuffer = new byte[1];
        int numBytes; // bytes returned from read()
        StringBuilder buider = new StringBuilder();

        // Keep listening to the InputStream until an exception occurs.
        while (true) {
            try {
                //Log.e("BT","connected");
                // Read from the InputStream.
                numBytes = mmInStream.read(mmBuffer,0,1);
                // Send the obtained bytes to the UI activity.
                Log.e("BT",Integer.toString(numBytes));
                String message = new String(mmBuffer,0,numBytes);
                //Log.e("BT",message);

                if (startSaving){
                    buider.append(message);
                }
                if (message.contains("*") && counter > 0){
                    buider.deleteCharAt(buider.length()-1);
                    buider.deleteCharAt(0);
                    if (buider.toString().contains("TEP")){
                        displyTemperature(temperature,buider.toString());
                     } else {
                        displyHumidity(humidity, buider.toString());
                        String parts[] = buider.toString().split("=");
                        if (index > 399)
                        {
                            index = 0;
                        }
                        reRenderGraph(index, parts[1]);
                        index++;
                    }

                    Log.e("BT", buider.toString());
                    buider.setLength(0);
                    counter = 0;
                    startSaving = false;
                    endSaving = false;
                }
                if (message.contains("*")){
                    ++counter;
                    buider.append(message);
                    startSaving = true;
                }
                Log.e("BT", buider.toString());
               //displyTemperature(temperature,message);
            } catch (IOException e) {
                Log.d("error", "Input stream was disconnected", e);
                break;
            }
        }
    }

    public void displyTemperature(final TextView temperature, final String text) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            public void run() {
                temperature.setText(text);
            }
        });
    }

    public void displyHumidity(final TextView temperature, final String text) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            public void run() {
                humidity.setText(text);
            }
        });
    }

    public void reRenderGraph(final int index ,final String graphValue){
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            public void run() {
                graphValues = updateValue(index,graphValues,graphValue);
                series.resetData(graphValues);
            }
        });
    }

    private DataPoint[] updateValue(int i, DataPoint[] values, String valueFromFile){
        values[i] = new DataPoint(i*0.01,Double.parseDouble(valueFromFile));
        return values;
    }

    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e("connect", "Could not close the client socket", e);
        }
    }
}
