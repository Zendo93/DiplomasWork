
package sk.stuba.fei.uamt.diplomaswork;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
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

public class ProgressActivity extends AppCompatActivity {

    private final Handler mHandler = new Handler();
    private Runnable mTimer1;
    private LineGraphSeries<DataPoint> series;
    private String graphValue;
    private BufferedReader fileReader;
    private DataPoint[] graphValues;
    private int index;
    private ProcessThread processThread;
    private double peakElement =  0;
    private double previousElement=0;
    private double nextElement=0;
    private int counter = 0;
    private int peakCounter = 0;
    long timeBegin = 0;
    long timeEnd = 0;
    long changeGraphTimeBegin = 0;
    double difference = 0;
    double heartBeat = 0;
    boolean firstPeak = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);
        hideStatusBar();
        try {
            fileReader = inicializeReader();
            graphValues = inicializeValues(fileReader);
            createGraph(graphValues);

            TextView temperature = (TextView) findViewById(R.id.temperature);
            TextView humidity = (TextView) findViewById(R.id.heartRate);
            TextView warning = (TextView) findViewById(R.id.warning);
            BluetoothSocketState state = (BluetoothSocketState) getApplicationContext();
            processThread = new ProcessThread(state.getBluetoothSocket(),state.getInputStream(), series, graphValues, temperature, humidity, warning, getBeepAdapter(100));
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
        graph.getViewport().setMaxY(2.5);
        graph.setTitle("EKG");
        graph.getGridLabelRenderer().setHorizontalAxisTitle(" ");
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
            values[i] = new DataPoint(i*0.01,0);
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        processThread.cancel();
        processThread.interrupt();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        processThread.cancel();
        processThread.interrupt();
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideStatusBar();
    }
    private MediaPlayer getBeepAdapter(int volume)
    {
        AudioManager manager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        manager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);

        Uri notification = RingtoneManager
                .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        return MediaPlayer.create(getApplicationContext(), notification);

    }
}
