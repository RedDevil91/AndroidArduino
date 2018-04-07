package com.example.reddevil91.orientationtest;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private BluetoothAdapter bluetoothAdapter;
    private static final int REQUEST_ENABLE_BT = 1;
    public static final String EXTRA_NAME = "extra_name";
    public static final String EXTRA_ADDRESS = "extra_address";
    public static final String EXTRA_UUID = "extra_uuid";
    private boolean fetchFlag = false;
    private int selectedPosition = -1;
    ArrayList<DetectedDevice> device_list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fetchFlag = false;

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        else {
            registerIntentFilters();
        }

        DeviceAdapter device_adapter = new DeviceAdapter(this, device_list);

        ListView device_listview = findViewById(R.id.device_list);
        device_listview.setAdapter(device_adapter);

        device_listview.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                if (selectedPosition != pos && selectedPosition != -1) {
                    ListView dev_list = findViewById(R.id.device_list);
                    View dev_view = dev_list.getChildAt(selectedPosition);
                    CheckBox prev_dev = dev_view.findViewById(R.id.selected);
                    prev_dev.setChecked(false);
                }
                CheckBox connect = view.findViewById(R.id.selected);
                connect.setChecked(!connect.isChecked());
                Button connect_btn = findViewById(R.id.connect_btn);
                if (connect.isChecked()) {
                    selectedPosition = pos;
                    connect_btn.setEnabled(true);
                }
                else {
                    selectedPosition = -1;
                    connect_btn.setEnabled(false);
                }
            }
        });

        Button connect_btn = findViewById(R.id.connect_btn);

        connect_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedPosition > -1) {
                    BluetoothDevice device = getSelectedDevice();
                    if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                        device.createBond();
                    }
                    else {
                        bluetoothAdapter.cancelDiscovery();
                        registerReceiver(discoveryReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
                    }
                }
            }
        });
    }

    private BluetoothDevice getSelectedDevice(){
        DetectedDevice selectedDevice = device_list.get(selectedPosition);
        return bluetoothAdapter.getRemoteDevice(selectedDevice.getBluetoothDeviceAddress());
    }

    private void getPairedDevices(){
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            ListView devices = findViewById(R.id.device_list);
            DeviceAdapter adapter = (DeviceAdapter) devices.getAdapter();
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                DetectedDevice dev = new DetectedDevice(deviceName, deviceHardwareAddress, device.getBondState() == BluetoothDevice.BOND_BONDED);
                device_list.add(dev);
            }
            adapter.notifyDataSetChanged();
        }
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                ProgressBar spinner = findViewById(R.id.spinner);
                spinner.setVisibility(View.GONE);
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                DetectedDevice dev = new DetectedDevice(deviceName, deviceHardwareAddress, device.getBondState() == BluetoothDevice.BOND_BONDED);
                device_list.add(dev);
                ListView devices = findViewById(R.id.device_list);
                DeviceAdapter adapter = (DeviceAdapter) devices.getAdapter();
                adapter.notifyDataSetChanged();
            }
            else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                BluetoothDevice dev = getSelectedDevice();
                if (dev.getBondState() == BluetoothDevice.BOND_BONDED)
                {
                    bluetoothAdapter.cancelDiscovery();
                    registerReceiver(discoveryReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
                }
            }
            else if (BluetoothDevice.ACTION_UUID.equals(action)){
                BluetoothDevice d = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Parcelable[] uuidExtra = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID);

                if(uuidExtra ==  null) {
                    Log.e("TAG", "UUID = null");
                }
                if(d != null && uuidExtra != null && !fetchFlag){
                    startSensorActivity(uuidExtra);
                    fetchFlag = true;
                }
            }
        }
    };

    private final BroadcastReceiver discoveryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                BluetoothDevice dev = getSelectedDevice();
                if (dev.getBondState() == BluetoothDevice.BOND_BONDED) {
                    dev.fetchUuidsWithSdp();
                    unregisterReceiver(discoveryReceiver);
                }
            }
        }
    };

    private void startSensorActivity(Parcelable[] uuids){
        DetectedDevice device = device_list.get(selectedPosition);
        Intent i = new Intent(MainActivity.this, SensorActivity.class);
        i.putExtra(EXTRA_NAME, device.getBluetoothDeviceName());
        i.putExtra(EXTRA_ADDRESS, device.getBluetoothDeviceAddress());
        i.putExtra(EXTRA_UUID, uuids[0].toString());
        startActivity(i);
    }

    private void registerIntentFilters(){
        registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
        registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_UUID));
        bluetoothAdapter.startDiscovery();
        ProgressBar spinner = findViewById(R.id.spinner);
        spinner.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK){
            Toast.makeText(getApplicationContext(), "Bluetooth enabled!", Toast.LENGTH_SHORT).show();
            registerIntentFilters();
        }
        else if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(getApplicationContext(), "Bluetooth is disabled!", Toast.LENGTH_SHORT).show();
            // TODO: create a enable bluetooth notification or sg
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        unregisterReceiver(discoveryReceiver);
    }
}
