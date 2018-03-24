package com.example.reddevil91.orientationtest;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private BluetoothAdapter bluetoothAdapter;
    private static final int REQUEST_ENABLE_BT = 1;
    private int selectedPosition = -1;
    ArrayList<DetectedDevice> device_list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        else {
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mReceiver, filter);
            bluetoothAdapter.startDiscovery();
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
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                DetectedDevice dev = new DetectedDevice(deviceName, deviceHardwareAddress, device.getBondState() == BluetoothDevice.BOND_BONDED);
                device_list.add(dev);
                ListView devices = findViewById(R.id.device_list);
                DeviceAdapter adapter = (DeviceAdapter) devices.getAdapter();
                adapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK){
            Toast.makeText(getApplicationContext(), "Bluetooth enabled!", Toast.LENGTH_SHORT).show();
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mReceiver, filter);
            bluetoothAdapter.startDiscovery();
        }
        else if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(getApplicationContext(), "Bluetooth is disabled!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }
}
