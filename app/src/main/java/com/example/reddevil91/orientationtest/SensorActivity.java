package com.example.reddevil91.orientationtest;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.UUID;

public class SensorActivity extends AppCompatActivity implements SensorEventListener {
    private static final String TAG = "SENSOR_ACTIVITY_TAG";
    private ConnectedThread connection;
    private SensorManager sensorManager;
    private Sensor accelerometer, magnetometer;
    private float acc_values[], mag_values[];
    private float[] orientation = new float[3];

    private final int CYCLE_TIME = 200;
    private Handler timerHandler = new Handler();
    private Runnable timerEvent = new Runnable() {
        @Override
        public void run() {
            float[] buffer = new float[3];
            System.arraycopy(orientation, 0, buffer, 0, buffer.length);
            byte[] out_message = createBluetoothMessage(buffer);
            connection.write(out_message);
            timerHandler.postDelayed(this, CYCLE_TIME);
        }
    };

    private byte[] createBluetoothMessage(float[] orientation_buffer){
        byte[] message = new byte[8];
        for (int i=0; i<orientation_buffer.length; i ++){
            int tmp = (int) Math.round(Math.toDegrees(orientation_buffer[i]));
            if (tmp < 0) {
                message[2*i] = 1;
            }
            else {
                message[2*i] = 0;
            }
            message[2*i+1] = (byte) Math.abs(tmp);
        }
        SeekBar speed_val = findViewById(R.id.speed);
        CheckBox start_stop = findViewById(R.id.start_stop);
        message[6] = (byte) (start_stop.isChecked() ? 1 : 0);
        message[7] = (byte) speed_val.getProgress();
        return message;
    }

    private interface MessageConstants{
        static final int READ = 0;
        static final int WRITE = 1;
        static final int TOAST = 2;
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MessageConstants.READ){
                connection.write(new String((char[]) msg.obj).getBytes());
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        Intent i = getIntent();
        final String address = i.getStringExtra(MainActivity.EXTRA_ADDRESS);
        final UUID uuid = UUID.fromString(i.getStringExtra(MainActivity.EXTRA_UUID));

        Button start_btn = findViewById(R.id.bluetooth_btn);

        Thread connect_thread = new ConnectThread(address, uuid);
        connect_thread.start();

        start_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timerHandler.postDelayed(timerEvent, CYCLE_TIME);
            }
        });

        SeekBar speed = findViewById(R.id.speed);

        TextView speed_value = findViewById(R.id.speed_value);
        speed_value.setText(String.format(getString(R.string.speed_val), speed.getProgress()));

        speed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                TextView value = findViewById(R.id.speed_value);
                value.setText(String.format(getString(R.string.speed_val), progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            acc_values = sensorEvent.values;
        }
        else if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            mag_values = sensorEvent.values;
        }

        if (acc_values != null && mag_values != null) {
            float R_M[] = new float[9];
            float I_M[] = new float[9];
            SensorManager.getRotationMatrix(R_M, I_M, acc_values, mag_values);
            SensorManager.getOrientation(R_M, orientation);

            TextView roll = findViewById(R.id.roll);
            TextView pitch = findViewById(R.id.pitch);
            TextView yaw = findViewById(R.id.yaw);

            roll.setText(String.format(getString(R.string.roll), Math.toDegrees(orientation[1])));
            pitch.setText(String.format(getString(R.string.pitch), Math.toDegrees(orientation[2])));
            yaw.setText(String.format(getString(R.string.yaw), Math.toDegrees(orientation[0])));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        // do nothing
    }

    public class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;

        ConnectThread(String address, UUID uuid) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;

            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothDevice device = adapter.getRemoteDevice(address);
            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                tmp = device.createRfcommSocketToServiceRecord(uuid);
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                Log.e(TAG, "Could not connect!", connectException);
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }
            Log.i(TAG, "Connected");
            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            manageMyConnectedSocket(mmSocket);
        }
    }

    public void manageMyConnectedSocket(BluetoothSocket socket){
        // create connected thread!!!
        connection = new ConnectedThread(socket);
        connection.start();
        Log.i(TAG, "Communication tunnel created!");
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BufferedReader mmInStream;
        private final OutputStream mmOutStream;
        private char[] mmBuffer; // mmBuffer store for the stream

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams; using temp objects because
            // member streams are final.
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
            }

            mmInStream = new BufferedReader(new InputStreamReader(tmpIn));
            mmOutStream = tmpOut;
        }

        public void run() {
            int numBytes; // bytes returned from read()
            int offset;
            mmBuffer = new char[1024];
            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                try {
                    // Read from the InputStream.
                    if (mmInStream.ready()) {
                        // copy buffer to prevent buffer modifications while send message to handler
                        numBytes = mmInStream.read(mmBuffer, 0, mmBuffer.length);
                        char[] copy_array = new char[numBytes];
                        System.arraycopy(mmBuffer, 0, copy_array, 0, numBytes);
                        // Send the obtained bytes to the UI activity.
                        Message readMsg = mHandler.obtainMessage(
                                MessageConstants.READ, numBytes, -1,
                                copy_array);
                        readMsg.sendToTarget();
                    }
                } catch (IOException e) {
                    Log.d(TAG, "Input stream was disconnected", e);
                    break;
                }
            }
        }

        // Call this from the main activity to send data to the remote device.
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);

                // Share the sent message with the UI activity.
//                Message writtenMsg = mHandler.obtainMessage(
//                        MessageConstants.WRITE, -1, -1, mmBuffer);
//                writtenMsg.sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);

                // Send a failure message back to the activity.
                Message writeErrorMsg =
                        mHandler.obtainMessage(MessageConstants.TOAST);
                Bundle bundle = new Bundle();
                bundle.putString("toast",
                        "Couldn't send data to the other device");
                writeErrorMsg.setData(bundle);
                mHandler.sendMessage(writeErrorMsg);
            }
        }

        // Call this method from the main activity to shut down the connection.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
            Log.i(TAG, "Connection closed");
        }
    }

    @Override
    protected void onDestroy() {
        connection.cancel();
        super.onDestroy();
    }
}
