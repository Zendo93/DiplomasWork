package sk.stuba.fei.uamt.diplomaswork;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private BluetoothAdapter BA;
    private Set<BluetoothDevice> pairedDevices;
    private List<BluetoothDevice> foundedDevices;
    private ConnectThread connectThreadMicrontroler;
    private ConnectThread connectThreadMobile;
    private Menu menu;
    private final int REQUEST_CODE_FOR_ENABLING_BLUETOOTH = 0;
    private Handler mHandler;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent
                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                foundedDevices.add(device);
                Log.i("BT", device.getName() + "\n" + device.getAddress());
                if (device.getName() == null){
                    generateDevice(context,"unknown device",device);
                } else {
                    generateDevice(context, device.getName(), device);
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.i("BT", "Discovery finished");
                MenuItem searchDevices = menu.findItem(R.id.action_settings);
                searchDevices.setTitle(R.string.action_settings);
                searchDevices.setEnabled(true);
            } else if (BluetoothDevice.ACTION_UUID.equals(action)){
                Log.i("BT", "action uuid");
                BluetoothDevice device = intent
                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null){
                    Log.i("BT", device.getName() + "\n" + device.getAddress());
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        foundedDevices = new ArrayList<>();
        BA = BluetoothAdapter.getDefaultAdapter();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_UUID);
        registerReceiver(mReceiver, filter);
        Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(turnOn, REQUEST_CODE_FOR_ENABLING_BLUETOOTH);
        setContentView(R.layout.activity_main);
        setTitle(R.string.title_activity_main);
        mHandler = new Handler();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                refreshFoundedDevices();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BA.cancelDiscovery();
        BA.disable();
        unregisterReceiver(mReceiver);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_FOR_ENABLING_BLUETOOTH) {
            startSearching();
        }
    }


    private void generateDevice(final Context context, final String deviceName, final BluetoothDevice deviceToConnect){
        LinearLayout devices = (LinearLayout) findViewById(R.id.devices);
        TextView device = new TextView(context);
        LinearLayout.LayoutParams lpDevice = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        device.setLayoutParams(lpDevice);
        device.setPadding(dpToPx(context,10),dpToPx(context,10),0,dpToPx(context,10));
        device.setClickable(true);
        device.setText(deviceName);
        device.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.e("clicked", deviceName);
                        BA.cancelDiscovery();

                        try {
                            connectThreadMicrontroler = new ConnectThread(deviceToConnect,UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"),context);
                            //connectThreadMobile = new ConnectThread(deviceToConnect,UUID.fromString("00001112-0000-1000-8000-00805f9b34fb"),context);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        connectThreadMicrontroler.run();
                        //connectThreadMobile.run();

                        //deviceToConnect.fetchUuidsWithSdp();
                    }
                });

        TextView line = new TextView(context);
        line.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
        line.setHeight(dpToPx(context,1));
        line.setBackground(ContextCompat.getDrawable(context,R.color.grey));

        devices.addView(device);
        devices.addView(line);

    }

    private int dpToPx(Context context ,int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    private void refreshFoundedDevices(){
        LinearLayout devices = (LinearLayout) findViewById(R.id.devices);
        devices.removeAllViews();
        MenuItem searchDevices = menu.findItem(R.id.action_settings);
        searchDevices.setTitle("Vyhľadávanie...");
        searchDevices.setEnabled(false);
        foundedDevices.clear();
        BA.startDiscovery();
    }

    private void startSearching(){
        BA.startDiscovery();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.e("BT","back pressed");
    }
}
