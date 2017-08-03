package com.LockE.anthonyguo.LockE;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.UUID;

public class MotorControl extends AppCompatActivity {
    private static final String TAG = "MotorControl";
    Button Brake, Forward, RotateLeft, RotateRight, Reverse;
    TextView status;
    String message;
    public BluetoothConnectionService mBluetoothConnection;
    public BluetoothDevice mBluetoothDevice;
    private UUID MY_UUID;
    String uuidString;
    public boolean BTConnectionFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_motor_control);
        Intent i = getIntent();
        mBluetoothDevice = i.getExtras().getParcelable("BT Connection");
        uuidString = i.getExtras().getString("UUID");
        MY_UUID = UUID.fromString(uuidString);
        mBluetoothConnection = new BluetoothConnectionService(MotorControl.this);
        startConnection();

        Brake = (Button) findViewById(R.id.Brake);
        Forward = (Button) findViewById(R.id.Forward);
        RotateLeft = (Button) findViewById(R.id.RotateLeft);
        RotateRight = (Button) findViewById(R.id.RotateRight);
        Reverse = (Button) findViewById(R.id.Reverse);
        status = (TextView) findViewById(R.id.status);
        status.setBackgroundColor(Color.RED);

        registerReceiver(mReceiver, new IntentFilter("incomingMessage"));
        registerReceiver(connectedReceiver, new IntentFilter("BTStatus"));
        registerReceiver(writeOutReceiver, new IntentFilter("WriteOutIntent"));
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter("incomingMessage"));
        LocalBroadcastManager.getInstance(this).registerReceiver(connectedReceiver, new IntentFilter("BTStatus"));
        LocalBroadcastManager.getInstance(this).registerReceiver(writeOutReceiver, new IntentFilter("WriteOutIntent"));
        //Log.d(TAG, "onCreate: registered receivers");

        Brake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(BTConnectionFlag) {
                    mBluetoothConnection.write("5");
                    //Log.d(TAG, "IRLED");
                }
            }
        });

        Forward.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if(BTConnectionFlag) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            mBluetoothConnection.write("1");
                            //Log.d(TAG, "Forward");
                            return true;
                        case MotionEvent.ACTION_UP:
                            mBluetoothConnection.write("0");
                            //Log.d(TAG, "Coast");
                            return true;
                    }
                }
                return false;
            }
        });

        RotateLeft.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (BTConnectionFlag) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            mBluetoothConnection.write("3");
                            //Log.d(TAG, "Rotate Left");
                            return true;
                        case MotionEvent.ACTION_UP:
                            mBluetoothConnection.write("0");
                            //Log.d(TAG, "Coast");
                            return true;
                    }
                }
                return false;
            }
        });

        RotateRight.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (BTConnectionFlag) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            mBluetoothConnection.write("4");
                            //Log.d(TAG, "Rotate Right");
                            return true;
                        case MotionEvent.ACTION_UP:
                            mBluetoothConnection.write("0");
                            //Log.d(TAG, "Coast");
                            return true;
                    }
                }
                return false;

            }
        });

        Reverse.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (BTConnectionFlag) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            mBluetoothConnection.write("2");
                            //Log.d(TAG, "Reverse");
                            return true;
                        case MotionEvent.ACTION_UP:
                            mBluetoothConnection.write("0");
                            //Log.d(TAG, "Coast");
                            return true;
                    }
                }
                return false;
            }
        });
    }

    public BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String text = intent.getStringExtra("Message");
            message = new String();
            message = text;
            //Log.d(TAG, "onReceive: " + message);
            if(message.contains("N")) {
                status.setBackgroundColor(Color.parseColor("#EEEEEE"));
                //Log.d(TAG, "onReceive: Unhit");
            }
            else if (message.contains("I")) {
                status.setBackgroundColor(Color.BLACK);
                //Log.d(TAG, "onReceive: Hit");
            }
            else if(message.contains("R")) {
                status.setText("0");
            }
            else if(!message.contains("\n")) {
                status.setText(message);
            }
        }
    };

    public BroadcastReceiver connectedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String text1 = intent.getStringExtra("State");
            if (text1.equals("Connected")) {
                status.setBackgroundColor(Color.parseColor("#EEEEEE"));
                BTConnectionFlag = true;
            }
            else if(text1.equals("Disconnected")) {
                status.setBackgroundColor(Color.RED);
                //Log.e(TAG, "onReceive: Disconnected" );
                BTConnectionFlag = false;
            }
        }
    };

    public BroadcastReceiver writeOutReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String text2 = intent.getStringExtra("writeMessage");
            if (text2.equals("WriteFail")) {
                status.setBackgroundColor(Color.RED);
                Toast.makeText(MotorControl.this, "BT unable to communicate, restart app", Toast.LENGTH_SHORT).show();
            }
        }
    };

    public void startConnection(){
        //Log.d(TAG, "startconnection: Initializing RFcomm bluetooth connection");
        mBluetoothConnection.startClient(mBluetoothDevice, MY_UUID);
    }

    @Override
    protected void onDestroy() {
        //Log.d(TAG, "onDestroy called");
        super.onDestroy();
        unregisterReceiver(mReceiver);
        unregisterReceiver(writeOutReceiver);
        unregisterReceiver(connectedReceiver);
        if (BTConnectionFlag)
            mBluetoothConnection.disconnect();
    }
}
