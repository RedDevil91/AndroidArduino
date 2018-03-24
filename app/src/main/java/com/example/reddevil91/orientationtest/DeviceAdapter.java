package com.example.reddevil91.orientationtest;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by RedDevil91 on 2018. 03. 23..
 * Adapter Class for device data
 */

public class DeviceAdapter extends ArrayAdapter {

    DeviceAdapter(Activity context, ArrayList<DetectedDevice> device_list){
        super(context, 0, device_list);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View device_view = convertView;
        if (device_view==null){
            device_view = LayoutInflater.from(getContext()).inflate(
                    R.layout.device_layout, parent, false);
        }

        DetectedDevice device = (DetectedDevice) getItem(position);

        TextView name = device_view.findViewById(R.id.dev_name);
        TextView address = device_view.findViewById(R.id.dev_address);

        name.setText(device.getBluetoothDeviceName());
        address.setText(device.getBluetoothDeviceAddress());

        return device_view;
    }
}
