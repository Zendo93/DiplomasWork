
package sk.stuba.fei.uamt.diplomaswork;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.support.v7.app.ActionBar;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Set;

public class ProgressActivity extends AppCompatActivity {

    private final Handler mHandler = new Handler();
    private Runnable mTimer1;
    private LineGraphSeries<DataPoint> series;
    private String graphValue;
    private BufferedReader fileReader;
    private DataPoint[] graphValues;
    private int index;
    private ProcessThread processThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);
        hideStatusBar();
        try {
            fileReader = inicializeReader();
            /*graphValue = readCsvFile(fileReader);
            graphValues = inicializeValues(graphValue);*/
            graphValues = inicializeValues(fileReader);
            createGraph(graphValues);
            index = 1;
            TextView temperature = (TextView) findViewById(R.id.temperature);
            TextView humidity = (TextView) findViewById(R.id.humidity);
            BluetoothSocketState state = (BluetoothSocketState) getApplicationContext();
            processThread = new ProcessThread(state.getBluetoothSocket(),state.getInputStream(), series, graphValues, temperature, humidity);
            processThread.start();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void hideStatusBar() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
    }

    private void createGraph(DataPoint[] graphValues) {
        GraphView graph = (GraphView) findViewById(R.id.graph);

        series = new LineGraphSeries<>(graphValues);
        graph.addSeries(series);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(1.5);
        graph.setTitle("EKG");
        graph.getGridLabelRenderer().setHorizontalAxisTitle(" ");
    }

   /*@Override
    public void onResume() {
        super.onResume();
        mTimer1 = new Runnable() {
            @Override
            public void run() {
                try {
                    if ((graphValue = readCsvFile(fileReader)) != null)
                    {
                        if (index > 399)
                        {
                            index = 0;
                        }
                        graphValues = updateValue(index,graphValues,graphValue);
                        series.resetData(graphValues);
                        index++;
                       mHandler.postDelayed(this,2);
                    }

                } catch (FileNotFoundException e) {
                   e.printStackTrace();
                }

            }
        };
        mHandler.postDelayed(mTimer1, 2);

    }*/


    @Override
    public void onPause() {
        mHandler.removeCallbacks(mTimer1);
        super.onPause();
    }

    private String readCsvFile(BufferedReader fileReader) throws FileNotFoundException {
        String value = null;
        try {
            value = fileReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return value;
    }

    private DataPoint[] inicializeValues(BufferedReader fileReader) throws FileNotFoundException {
        DataPoint[] values = new DataPoint[400];
        String valueFromFile = " ";
        for (int i=0; i<400;i++) {
            valueFromFile = readCsvFile(fileReader);
            values[i] = new DataPoint(i*0.01,Double.parseDouble(valueFromFile));
        }
        return  values;
    }

    private DataPoint[] updateValue(int i, DataPoint[] values, String valueFromFile){
        values[i] = new DataPoint(i*0.01,Double.parseDouble(valueFromFile));
        return values;
    }

    private BufferedReader inicializeReader(){
        InputStream is = getResources().openRawResource(R.raw.ekgzataz);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
        return reader;
    }
}
