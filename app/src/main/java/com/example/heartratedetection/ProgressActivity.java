package com.example.heartratedetection;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class ProgressActivity extends AppCompatActivity {

    private static final UUID HEART_RATE_SERVICE_UUID = UUID.fromString("0000180D-0000-1000-8000-00805f9b34fb");
    private static final UUID HEART_RATE_MEASUREMENT_CHARACTERISTIC_UUID = UUID.fromString("00002A37-0000-1000-8000-00805f9b34fb");
    private static final UUID CLIENT_CHARACTERISTIC_CONFIG_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private BluetoothGatt bluetoothGatt;
    TextView heartRateView;

    // Connect to heart rate monitor device

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);

        heartRateView = findViewById(R.id.textView);
        TextView medianTextView = findViewById(R.id.textView2);
    }

    private void connectToDevice(BluetoothDevice device) {
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
        bluetoothGatt = device.connectGatt(this, false, new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    // Connected to heart rate monitor device, discover
                    // services
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    bluetoothGatt.discoverServices();
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    // Disconnected from heart rate monitor device
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    // Services discovered, find heart rate measurement characteristic
                    BluetoothGattService service = gatt.getService(HEART_RATE_SERVICE_UUID);
                    if (service != null) {
                        BluetoothGattCharacteristic characteristic = service.getCharacteristic(HEART_RATE_MEASUREMENT_CHARACTERISTIC_UUID);
                        if (characteristic != null) {
                            // Enable notifications for heart rate measurement characteristic
                            if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                                // TODO: Consider calling
                                //    ActivityCompat#requestPermissions
                                // here to request the missing permissions, and then overriding
                                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                //                                          int[] grantResults)
                                // to handle the case where the user grants the permission. See the documentation
                                // for ActivityCompat#requestPermissions for more details.
                                return;
                            }
                            gatt.setCharacteristicNotification(characteristic, true);
                            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(String.valueOf(CLIENT_CHARACTERISTIC_CONFIG_UUID)));
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            gatt.writeDescriptor(descriptor);
                        }
                    }
                }
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicChanged(gatt, characteristic);
                if (HEART_RATE_MEASUREMENT_CHARACTERISTIC_UUID.equals(characteristic.getUuid())) {
                    // Heart rate measurement characteristic value changed, get heart rate data
                    int flags = characteristic.getProperties();
                    int format = -1;
                    if ((flags & 0x01) != 0) {
                        format = BluetoothGattCharacteristic.FORMAT_UINT16;
                    } else {
                        format = BluetoothGattCharacteristic.FORMAT_UINT8;
                    }
                    int heartRate = characteristic.getIntValue(format, 1);
                    // Use heart rate data
                    heartRateView.setText(heartRate + " Bpm");
                }
            }
        });

    }
    }