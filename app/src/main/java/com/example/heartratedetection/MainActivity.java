package com.example.heartratedetection;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final UUID WATCH_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final int REQUEST_ENABLE_BT = 1000;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice watchDevice;
    private BluetoothSocket watchSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Check if device supports Bluetooth and if it's enabled
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Device does not support Bluetooth
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            // Bluetooth is not enabled, prompt user to turn it on
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        // Search for watch device
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName().equals("Watch Name")) {
                    watchDevice = device;
                    break;
                }
            }
        }

        // Establish connection with watch device
        if (watchDevice != null) {
            try {
                watchSocket = watchDevice.createRfcommSocketToServiceRecord(WATCH_UUID);
                watchSocket.connect();
                // Connection established, do something with the watch
            } catch (IOException e) {
                // Error connecting to watch
                e.printStackTrace();
            }
        } else {
            // Watch device not found
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button continueBtn = findViewById(R.id.button);
        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this,ProgressActivity.class);
                startActivity(intent);

            }
        });

    }
}