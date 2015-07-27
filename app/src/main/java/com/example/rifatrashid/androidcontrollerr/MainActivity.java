package com.example.rifatrashid.androidcontrollerr;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.widget.SeekBar;
import android.widget.TextView;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


public class MainActivity extends ActionBarActivity {

    SeekBar motorControl, servoControl;
    InetAddress IPAddress = null;
    DatagramSocket clientSocket = null;
    final String IP = "192.168.1.3";
    final int PORT = 9898;
    int motorSpeedValue = 0;
    dataSender DataSender;
    int servoSpeedValue = 0;
    private boolean isConnected = false;
    private int progressNewMotor = 0;
    private int progressOldMotor = 30;
    private int progressNewServo = 0;
    private int progressOldServo = 90;
    private TextView signalText;

    public static int motorDirection = 0;
    public static int servoDirection = 0;
    public static int servoValue = 0;
    public static int motorValue = 0;

    public static int oldmotoeValue = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setElevation(0);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00796B")));
        final Animation in = new AlphaAnimation(0.0f, 1.0f);
        in.setDuration(2000);

        final Animation out = new AlphaAnimation(1.0f, 0.0f);
        out.setDuration(3000);

        out.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                signalText.setText("");
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        AnimationSet animationSet = new AnimationSet(true);
        animationSet.addAnimation(in);
        animationSet.setStartOffset(3000);
        animationSet.addAnimation(out);
        signalText = (TextView) findViewById(R.id.signalText);

        //Setup basic networking stuff...
        try {
            setUpConnection();
            isConnected = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (isConnected) {
            try {
                signalText.setText("Connected to Server");
                signalText.startAnimation(animationSet);

                //Set servo and motor speeds to 0
                //sendPacket("_escA_0_");
                //sendPacket("_servoA_0_1_");

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
                progressNewMotor = progress;

                if (Math.abs(progressNewMotor - progressOldMotor) >= 5) {
                    DataSender = new dataSender();
                    DataSender.setRunning(true);
                    DataSender.start();
                    progressOldMotor = progressNewMotor;
                    motorSpeedValue = progress;
                    if (motorSpeedValue >= 30) {
                        try {
                            motorValue = motorSpeedValue - 30;
                            motorDirection = 1;
                            oldmotoeValue = motorValue;
                            //sendPacket("_escA_" + String.valueOf(motorSpeedValue - 30) + "_");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        motorDirection = 0;
                        motorValue = 30 - motorSpeedValue;
                        oldmotoeValue = motorValue;
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
                    motorValue = 0;
                    //sendPacket("_escA_" + String.valueOf(0) + "_");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        servoControl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressNewServo = progress;
                if (Math.abs(progressNewServo - progressOldServo) >= 10) {
                    DataSender = new dataSender();
                    DataSender.setRunning(true);
                    DataSender.start();
                    servoSpeedValue = progress;
                    if (servoSpeedValue >= 60) {
                        try {
                            servoDirection = 0;
                            servoValue = (servoSpeedValue - 60);
                            //sendPacket("_servoA_" + String.valueOf((servoSpeedValue - 60)) + "_0_");
                            progressOldServo = progressNewServo;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {

                        try {
                            //System.out.println(servoSpeedValue);
                            //sendPacket("_servoA_" + String.valueOf(60 - servoSpeedValue) + "_1_");
                            servoDirection = 1;
                            servoValue = (60 - servoSpeedValue);
                            progressOldServo = progressNewServo;
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
                servoControl.setProgress(60);
                try {
                    servoValue = 0;
                    //DataSender.setRunning(false);
                    //sendPacket("_servoA_" + String.valueOf(0) + "_0_");
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

    class dataSender extends Thread {

        private boolean isRunning = false;

        public dataSender() {

        }

        public void run() {
            //while (isRunning) {
                //Send data
                try {

                    sendPacket("_" + String.valueOf(motorDirection) + "_" + String.valueOf(motorValue) + "_" + String.valueOf(servoDirection) + "_" + String.valueOf(servoValue) + "_");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                /*
                try {
                    Thread.sleep(65);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                */
                System.out.println(Math.abs(motorValue - oldmotoeValue));
           // }
        }

        private void sendPacket(String message) throws Exception {
            String sendMessage = message;
            byte[] sendData = sendMessage.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendMessage.length(), IPAddress, PORT);
            clientSocket.send(sendPacket);
            //System.out.println("s");
        }

        public void setRunning(boolean b) {
            this.isRunning = b;
        }
    }
}
