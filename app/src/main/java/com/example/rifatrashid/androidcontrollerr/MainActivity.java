package com.example.rifatrashid.androidcontrollerr;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SeekBar;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


public class MainActivity extends ActionBarActivity {

    SeekBar motorControl, servoControl;
    InetAddress IPAddress = null;
    DatagramSocket clientSocket = null;
    final String IP = "192.168.1.3";
    final int PORT = 9595;
    int motorSpeedValue = 0;
    int servoSpeedValue = 0;
    private boolean isConnected = false;
    private int progressNewMotor = 0;
    private int progressOldMotor = 0;
    private int progressNewServo = 0;
    private int progressOldServo = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setElevation(0);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00796B")));
        //Setup basic networking stuff...
        try {
            setUpConnection();
            isConnected = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (isConnected) {
            try {
                //Set servo and motor speeds to 0
                sendPacket("_escA_0_");
                sendPacket("_servoA_0_1_");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //End
        motorControl = (SeekBar) findViewById(R.id.escControl);
        servoControl = (SeekBar) findViewById(R.id.servoControl);
        motorControl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressNewMotor = progress - 30;
                if ((progressNewMotor - progressOldMotor) >= 6) {
                    progressOldMotor = progressNewMotor;
                    motorSpeedValue = progress - 30;
                    if (motorSpeedValue >= 0) {
                        try {
                            sendPacket("_escA_" + String.valueOf(motorSpeedValue) + "_");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                motorControl.setProgress(30);
                try {
                    sendPacket("_escA_" + String.valueOf(0) + "_");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        servoControl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressNewServo = progress;
                if((progressNewServo - progressOldServo) >= 6){
                    servoSpeedValue = progress - 30;
                    if (servoSpeedValue >= 0) {
                        try {
                            sendPacket("_servoA_" + String.valueOf(servoSpeedValue) + "_0_");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else if (servoSpeedValue < 0) {
                        try {
                            sendPacket("_servoA_" + String.valueOf(servoSpeedValue) + "_1_");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                servoControl.setProgress(30);
                try {
                    sendPacket("_servoA_" + String.valueOf(0) + "_0_");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setUpConnection() throws Exception {
        IPAddress = InetAddress.getByName(IP);
        clientSocket = new DatagramSocket();
    }

    private void sendPacket(String message) throws Exception {
        String sendMessage = message;
        byte[] sendData = sendMessage.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendMessage.length(), IPAddress, PORT);
        Thread t = new Thread(new threadedPacketSender(sendPacket));
        t.start();
    }
}
