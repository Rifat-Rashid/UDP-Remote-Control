package com.example.rifatrashid.androidcontrollerr;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class threadedPacketSender implements Runnable {
	DatagramPacket data;
	@Override
	public void run() {
		// TODO Auto-generated method stub
	    try {
	    	DatagramSocket s = new DatagramSocket();
			s.send(data);
	    } catch (Exception e) {

	    }
	}
	public threadedPacketSender(DatagramPacket p)  {
		
		try
		{
			data=p;	
		}		 
		catch (Exception e)
		{
			
		}
		
	}
	}