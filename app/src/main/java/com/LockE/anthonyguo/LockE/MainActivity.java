package com.LockE.anthonyguo.LockE;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

//Credits to Mitch Tabian from Youtube

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private static final String TAG = "MainActivity";
    BluetoothAdapter mBluetoothAdapter;
    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();
    public DeviceListAdapter mDeviceListAdapter;
    Set<BluetoothDevice> pairedDevices;

    public BluetoothConnectionService mBluetoothConnection;
    Button btnOnOff;
    Button motormovement;

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    BluetoothDevice mBTdevice;

    ListView DeviceList;

    public boolean flag3 = false;
    public boolean flag1 = false;

    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(mBluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, mBluetoothAdapter.ERROR);
                switch(state){
                    case BluetoothAdapter.STATE_OFF:
                        //Log.d(TAG, "onReceive: STATE OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        //Log.d(TAG, "mBroadcastReceiver1: STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        //Log.d(TAG, "mBroadcastReceiver1: STATE ON");
                        checkPairs();
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        //Log.d(TAG, "mBroadcastReceiver1: STATE TURNING ON");
                        checkPairs();
                        break;
                }
            }
        }
    };

    private BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            //Log.d(TAG, "onReceive : Action found");

            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra (BluetoothDevice.EXTRA_DEVICE);
                if (!mBTDevices.contains(device)) {
                    mBTDevices.add(device);
                }
                //Log.d(TAG, "onReceive: " + device.getAddress() + ": " + device.getName());
                mDeviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view, mBTDevices);
                DeviceList.setAdapter(mDeviceListAdapter);
            }
        }
    };

    private BroadcastReceiver mBroadcastReceiver4 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED){
                    //Log.d(TAG, "Bond Bonded");
                    mBTdevice = mDevice;
                }

                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
                    //Log.d(TAG, "Bond Bonding");
                }

                if (mDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                    //Log.d(TAG, "Bond None");
                }
            }
        }
    };


    @Override
    protected void onDestroy() {
        //Log.d(TAG, "onDestroy called");
        super.onDestroy();
        if (flag1) unregisterReceiver(mBroadcastReceiver1);
        if (flag3) unregisterReceiver(mBroadcastReceiver3);
        unregisterReceiver(mBroadcastReceiver4);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DeviceList = (ListView) findViewById(R.id.DeviceList);
        mBTDevices = new ArrayList<>();

        btnOnOff = (Button) findViewById(R.id.btnOnOff);
        motormovement = (Button) findViewById(R.id.motormovement);

        IntentFilter filter4 = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver4, filter4);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        DeviceList.setOnItemClickListener(MainActivity.this);

        Toast.makeText(MainActivity.this, "Click BT Button", Toast.LENGTH_SHORT).show();

        btnOnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Log.d(TAG, "OnClick:enabling/disabling bluetooth");
                TurnBTOn();
            }
        });

        motormovement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mBTdevice == null) {
                    Toast.makeText(MainActivity.this, "Bluetooth Device Not Selected", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(MainActivity.this, "Wait for a few seconds for BT connection", Toast.LENGTH_LONG).show();
                    Intent i = new Intent(MainActivity.this, MotorControl.class);
                    i.putExtra("BT Connection", mBTdevice);
                    i.putExtra("UUID", "00001101-0000-1000-8000-00805F9B34FB");
                    startActivity(i);
                    //Log.d(TAG, "onClick: Move to motor movement activity");
                }
            }
        });
    }

    public void TurnBTOn() {
        if (mBluetoothAdapter == null) {
            //Log.d(TAG, "Does not support BT");
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBTintent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTintent);

            IntentFilter BTintent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTintent);
        }
        if (mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.disable();

            Intent enableBTintent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTintent);

            IntentFilter BTintent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTintent);
        }
        flag1 = true;
    }

    public void checkPairs() {
        pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0){
            for (BluetoothDevice device : pairedDevices) {
                if(!mBTDevices.contains(device)) {
                    mBTDevices.add(device);
                }
                //Log.d(TAG, "onReceive: " + device.getAddress() + ": " + device.getName());
                mDeviceListAdapter = new DeviceListAdapter(this, R.layout.device_adapter_view, mBTDevices);
                DeviceList.setAdapter(mDeviceListAdapter);
            }
        }
    }

    public void btnScan(View view) {
        //Log.d(TAG, "looking for unpaired devices");

        if(mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();

            checkBTPermissions();

            mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
        }

        if (!mBluetoothAdapter.isDiscovering()) {
            checkBTPermissions();

            mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
        }
        flag3 = true;
    }

    //Ignore Errors from this function
    private void checkBTPermissions() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
            }
        }
        else{
            //Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        mBluetoothAdapter.cancelDiscovery();
        //Log.d(TAG, "onItemClick: Clicked on Device");
        String deviceName = mBTDevices.get(i).getName();
        String deviceAddress = mBTDevices.get(i).getAddress();
        //Log.d(TAG, "onItemClick: Name = " + deviceName);
        //Log.d(TAG, "onItemClick: Address = " + deviceAddress);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
            //Log.d(TAG, "Trying to pair with " + deviceName);
            mBTDevices.get(i).createBond();
            mBTdevice = mBTDevices.get(i);
        }
    }
}
