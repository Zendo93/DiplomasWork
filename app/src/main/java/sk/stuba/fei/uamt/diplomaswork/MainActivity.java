package sk.stuba.fei.uamt.diplomaswork;

import android.app.Activity;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.support.v7.app.ActionBar;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class MainActivity extends AppCompatActivity {

    private final Handler mHandler = new Handler();
    private Runnable mTimer1;
    private LineGraphSeries<DataPoint> series;
    private String graphValue;
    private BufferedReader fileReader;
    private DataPoint[] graphValues;
    private int index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        hideStatusBar();
        try {
            fileReader = inicializeReader();
            graphValue = readCsvFile(fileReader);
            graphValues = inicializeValues(graphValue);
            createGraph(graphValues);
            index = 1;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void hideStatusBar() {
        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        // Remember that you should never show the action bar if the
        // status bar is hidden, so hide that too if necessary.
        //ActionBar actionBar = getActionBar();
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
    }

    private void createGraph(DataPoint[] graphValues) {
        GraphView graph = (GraphView) findViewById(R.id.graph);

        series = new LineGraphSeries<>(graphValues);
        graph.addSeries(series);
        // graph.setTitle("EKG");
        //graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        /*mSeries1 = new LineGraphSeries<>(generateData());
        graph.addSeries(mSeries1);
        graph.setTitle("EKG");*/
    }

    /*@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(MainActivity.ARG_SECTION_NUMBER));
    }*/

    @Override
    public void onResume() {
        super.onResume();
        mTimer1 = new Runnable() {
            @Override
            public void run() {
                try {
                    if ((graphValue = readCsvFile(fileReader)) != null)
                    {
                        if (index > 8)
                        {
                            index = 0;
                        }
                        graphValues = updateValue(index,graphValues,graphValue);
                        series.resetData(graphValues);
                        index++;
                       mHandler.postDelayed(this,500);
                    }

                } catch (FileNotFoundException e) {
                   e.printStackTrace();
                }

            }
        };
        mHandler.postDelayed(mTimer1, 500);

    }


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

    private DataPoint[] inicializeValues(String valueFromFile){
        DataPoint[] values = new DataPoint[9];
        values[0] = new DataPoint(0,Double.parseDouble(valueFromFile));
        for (int i=1; i<9;i++) {
            values[i] = new DataPoint(i,0);
        }
        return  values;
    }

    private DataPoint[] updateValue(int i, DataPoint[] values, String valueFromFile){
        values[i] = new DataPoint(i,Double.parseDouble(valueFromFile));
        return values;
    }

    private BufferedReader inicializeReader(){
        InputStream is = getResources().openRawResource(R.raw.ekgzataz);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
        return reader;
    }
}
