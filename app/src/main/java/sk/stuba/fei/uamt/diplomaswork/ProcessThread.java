package sk.stuba.fei.uamt.diplomaswork;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

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
    private TextView heartRate;
    private int counter;
    private boolean startSaving = false;
    private boolean endSaving = true;
    private LineGraphSeries<DataPoint> series;
    private DataPoint[] graphValues;
    private int index=0;
    private double peakElement =  0;
    private double previousElement;
    private double nextElement=0;
    private int counter2 = 0;
    private int peakCounter = 0;
    long timeBegin = 0;
    long timeEnd = 0;
    double difference = 0;
    double heartBeat = 0;
    boolean firstPeak = true;

    public ProcessThread(BluetoothSocket mmSocket, InputStream mmInStream,LineGraphSeries<DataPoint> series,DataPoint[] graphValues, TextView temperature, TextView heartRate) {
        this.mmSocket = mmSocket;
        this.mmInStream = mmInStream;
        this.series = series;
        this.graphValues = graphValues;
        this.temperature = temperature;
        this.heartRate = heartRate;
        this.counter = 0;
    }


    public void run() {
        mmBuffer = new byte[1];
        int numBytes;
        StringBuilder data = new StringBuilder();
        while (true) {
            try {
                numBytes = mmInStream.read(mmBuffer,0,1);
                String message = new String(mmBuffer,0,numBytes);

                if (startSaving){
                    data.append(message);
                }
                if (message.contains("*") && counter > 0){
                    if (data.toString().contains("TEP")){
                        message = parseData(data);
                        if (isNumeric(message)){
                            displyTemperature(temperature, message);
                        }
                     } else {
                        message = parseData(data);
                        if (isNumeric(message)) {
                            calculateHeartBeat(message);

                            if (index > 399) {
                                index = 0;
                            }
                            reRenderGraph(index, message);
                            index++;
                        }
                    }
                    Log.e("BT", data.toString());
                    data.setLength(0);
                    data.append("*");
                }
                if (message.contains("*") && counter == 0){
                    ++counter;
                    data.append(message);
                    startSaving = true;
                }
            } catch (IOException e) {
                Log.d("error", "Input stream was disconnected", e);
                try {
                    mmSocket.connect();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    public static boolean isNumeric(String str)
    {
        try
        {
            double d = Double.parseDouble(str);
        }
        catch(NumberFormatException nfe)
        {
            return false;
        }
        return true;
    }

    private void calculateHeartBeat(String data){
        previousElement = peakElement;
        peakElement = nextElement;
        nextElement = Double.parseDouble(data);


        if (peakElement > previousElement && peakElement > nextElement && peakElement > 1){
            Log.e("peak", Double.toString(peakElement));
            if (firstPeak) {
                timeBegin = System.currentTimeMillis();
                firstPeak = false;
            } else {
                Log.e("peak", Long.toString(timeBegin) + " timeToBegin");
                timeEnd = System.currentTimeMillis();
                Log.e("peak", Long.toString(timeEnd) + " timeToEnd");
                difference = (timeEnd - timeBegin)/1000.0;
                timeBegin = timeEnd;

                heartBeat = (60 / difference);


                Log.e("peak", Double.toString(difference) + " difference");
                Log.e("peak", Double.toString(heartBeat) + " heartBeat");
                displyHeartRate(heartRate,  /*Double.toString(peakElement) + "\n heartBeat=" + */Long.toString(Math.round(heartBeat)) + " bpm");
            }
        }

    }
    private String parseData(StringBuilder data){
        data.deleteCharAt(data.length()-1);
        data.deleteCharAt(0);
        String parts[] = data.toString().split("=");
        return parts[1];
    }
    private void displyTemperature(final TextView temperature, final String text) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            public void run() {
                temperature.setText(Long.toString(Math.round(Double.parseDouble(text))) + " \u2103");
            }
        });
    }

    private void displyHeartRate(final TextView temperature, final String text) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            public void run() {
                heartRate.setText(text);
            }
        });
    }

    private void reRenderGraph(final int index ,final String graphValue){
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
