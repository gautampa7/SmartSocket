
package com.example.smartsocket;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {


    private TextView batteryTxt;
    private TextView statusText;
    private TextView deviceConText;
    private TextView socketStatus;
    private Switch threshsw;

    String address = "20:18:08:34:FB:D7";
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    int level;
    int threshold=80;
    int lowthresh;
    char flag;

    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context ctxt, Intent intent) {

            level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            batteryTxt.setText(level + "%");

            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;

            if (isCharging) {
                statusText.setText("Charging");
            } else {
                statusText.setText("Not Charging");
            }
            lowthresh=threshold-10;
            if (level <= lowthresh)
                {
                    flag = 'C';
                    socketStatus.setText("Socket Power ON");
                }
            else if (level >= threshold) {
                flag = 'O';
                socketStatus.setText("Socket Power OFF");
                }
            new ConnectBT().execute();
            if(isBtConnected)
            sendSignal(String.valueOf(flag));

            if(isBtConnected)
               deviceConText.setText("Socket Connected");
            else
                deviceConText.setText("Socket Disconnected");


        }

    };



    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);
        batteryTxt = this.findViewById(R.id.textView);
        statusText = this.findViewById(R.id.textView2);
        deviceConText = this.findViewById(R.id.textView4);
        socketStatus = this.findViewById(R.id.textView5);
        threshsw = this.findViewById(R.id.switch1);

        this.registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.BLUETOOTH_ADMIN);

        if(permissionCheck ==  PackageManager.PERMISSION_GRANTED){
            Toast.makeText(this, "Permission granted", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Permission not granted :(", Toast.LENGTH_SHORT).show();
        }


        threshsw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if(threshsw.isChecked())
                threshold=100;
            else
                threshold=80;

        }});

    }

    private void sendSignal ( String number ) {
        if ( btSocket != null ) {
            try {
                btSocket.getOutputStream().write(number.toString().getBytes());
            } catch (IOException e) {
                //msg("Error");
                isBtConnected = false;

            }
        }

    }

    private void Disconnect () {
        if ( btSocket!=null ) {
            try {
                btSocket.close();
            } catch(IOException e) {
                msg("Error");
            }
        }

        finish();
    }

    private void msg (String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void> {
        private boolean ConnectSuccess = true;


        @Override
        protected Void doInBackground (Void... devices) {
            try {
                if ( btSocket==null || !isBtConnected ) {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice clientsocket = myBluetooth.getRemoteDevice(address);
                    btSocket = clientsocket.createInsecureRfcommSocketToServiceRecord(myUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();


                }
            } catch (IOException e) {
                ConnectSuccess = false;
            }

            return null;
        }

        @Override
        protected void onPostExecute (Void result) {
            super.onPostExecute(result);

            if (!ConnectSuccess) {
                //msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
                isBtConnected = false;
                finish();
            } else {

                isBtConnected = true;
            }


        }
    }
}

