package com.bean.serialport;

import android.util.Log;

import com.cedric.serialport.SerialPort;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;

/**
 * @author benjaminwan
 *串口辅助工具类
 */
public abstract class  SerialHelper{
	private static final String TAG ="SerialHelper.class";
	private SerialPort mSerialPort;
	private OutputStream mOutputStream;
	private InputStream mInputStream;
	private ReadThread mReadThread;
	private SendThread mSendThread;
	//private String sPort="/dev/ttymxc0";
	private String sPort="/dev/ttyS3";
	private int iBaudRate=9600;
	private boolean _isOpen=false;
	private byte[] _bLoopData=new byte[]{0x30};
	private int iDelay=500;
	//----------------------------------------------------
	public SerialHelper(String sPort,int iBaudRate){
		this.sPort = sPort; //sport = serialCtrl.serialCOM --->SerialControl.sport
		this.iBaudRate=iBaudRate;
	}
	public SerialHelper(){
		//this("/dev/s3c2410_serial0",9600);
	   this("/dev/s3c2410_serial3",9600);
		// this("/dev/ttyS3",9600);
	}
	public SerialHelper(String sPort){
		this(sPort,9600);
	}
	public SerialHelper(String sPort,String sBaudRate){
		this(sPort,Integer.parseInt(sBaudRate));
	}
	//----------------------------------------------------
	public void open() throws SecurityException, IOException,InvalidParameterException{
		mSerialPort =  new SerialPort(new File(sPort), iBaudRate, 0);
		mOutputStream = mSerialPort.getOutputStream();
		mInputStream = mSerialPort.getInputStream();
		mReadThread = new ReadThread();  //暂时不用
		mReadThread.start();
	/*	mSendThread = new SendThread();  //暂时不用
		mSendThread.setSuspendFlag();
		mSendThread.start();*/
		_isOpen=true;
	}
	//----------------------------------------------------
	public void close(){
		if (mReadThread != null)
			mReadThread.interrupt();
		if (mSerialPort != null) {
			mSerialPort.close();
			mSerialPort = null;
		}
		_isOpen=false;
	}
	//----------------------------------------------------
	public void send(byte[] bOutArray){
		try
		{
			//mOutputStream.flush();
			mOutputStream.write(bOutArray);
			mOutputStream.flush();
			Log.d("mOutputStream : ","send : "+bOutArray);

		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	//----------------------------------------------------
	public void sendHex(String sHex){
		byte[] bOutArray = SerialFunc.HexToByteArr(sHex);
		Log.d("sendPortData : ","bOutArray : "+bOutArray);
		send(bOutArray);		
	}
	public void sendTxt(byte[] bOutArray){
		send(bOutArray);
	}
	//----------------------------------------------------
	public void sendTxt(String sTxt){
		byte[] bOutArray =sTxt.getBytes();
		send(bOutArray);		
	}
	//----------------------------------------------------
	public StringBuilder sb ;
	public ByteArrayOutputStream byteArrayOutputStream;
	private class ReadThread extends Thread {
		@Override
		public void run() {
			super.run();
			while(!isInterrupted()) {
				try
				{
					Log.i(TAG,"ReadThread test");
					byte[] buffer=new byte[256];
					if (mInputStream == null){
						return;
					}
					//判断拼接的字符串对象
					if (sb == null){
					  sb =new StringBuilder("");
					}
					int size = mInputStream.read(buffer);
                          sleep(100);
					Log.d(TAG,"onDataReceived size. :  "+size);
					//字节数组输出流
					if(byteArrayOutputStream ==null) {
						byteArrayOutputStream = new ByteArrayOutputStream();
						Log.d(TAG,"onDataReceived . :........  ");
					}

					byteArrayOutputStream.reset();//重新计数;
					byteArrayOutputStream.write(buffer,0,size);
					String Serial = SerialFunc.ByteArrToHex(byteArrayOutputStream.toByteArray());
					Log.d(TAG,"onDataReceived .Serial 1:" + Serial + " 长度 "+Serial.length());
					if(size<2){
						sb.append(Serial);
						Log.d(TAG,"onDataReceived .Serial 2:" + sb + " 长度 "+sb.length());
					}else {
						byteArrayOutputStream.close();
						sb.append(Serial);
						Log.d(TAG,"onDataReceived .Serial 3:" + sb + " 长度 "+sb.length());
						String[] voltArr = sb.toString().split(" ");
						getBettryVoltReceived(voltArr);
						if (sb.length()>0){
							sb.delete(0, sb.length() + 1);//拼接的字符串清除了 不用置为null， 避免重复创建
							Log.d(TAG, "onDataReceived  sb剩余 .4. :  " + sb);
						}
					}
				} catch (Throwable e)
				{
					e.printStackTrace();
					return;
				}
			}
		}
	}
//----------------------------------------------------
	private class SendThread extends Thread{
		public boolean suspendFlag = true; // 控制线程的执行
		@Override
		public void run() {
			super.run();
			while(!isInterrupted()) {
				synchronized (this)
				{
					while (suspendFlag)
					{
						try
						{
							wait();
						} catch (InterruptedException e)
						{
							e.printStackTrace();
						}
					}
				}
				send(getbLoopData());
				try
				{
					Thread.sleep(iDelay);
				} catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		}

		//线程暂停
		public void setSuspendFlag() {
		this.suspendFlag = true;
		}
		
		//唤醒线程
		public synchronized void setResume() {
		this.suspendFlag = false;
		notify();
		}
	}
	//----------------------------------------------------
	public int getBaudRate()
	{
		return iBaudRate;
	}
	public boolean setBaudRate(int iBaud)
	{
		if (_isOpen)
		{
			return false;
		} else
		{
			iBaudRate = iBaud;
			return true;
		}
	}
	public boolean setBaudRate(String sBaud)
	{
		int iBaud = Integer.parseInt(sBaud);
		return setBaudRate(iBaud);
	}
	//----------------------------------------------------
	public String getPort()
	{
		return sPort;
	}
	public boolean setPort(String sPort)
	{
		if (_isOpen)
		{
			return false;
		} else
		{
			this.sPort = sPort;
			return true;
		}
	}
	//----------------------------------------------------
	public boolean isOpen()
	{
		return _isOpen;
	}
	//----------------------------------------------------
	public byte[] getbLoopData()
	{
		return _bLoopData;
	}
	//----------------------------------------------------
	public void setbLoopData(byte[] bLoopData)
	{
		this._bLoopData = bLoopData;
	}
	//----------------------------------------------------
	public void setTxtLoopData(String sTxt){
		this._bLoopData = sTxt.getBytes();
	}
	//----------------------------------------------------
	public void setHexLoopData(String sHex){
		this._bLoopData = SerialFunc.HexToByteArr(sHex);
	}
	//----------------------------------------------------
	public int getiDelay()
	{
		return iDelay;
	}
	//----------------------------------------------------
	public void setiDelay(int iDelay)
	{
		this.iDelay = iDelay;
	}
	//----------------------------------------------------
	public void startSend()
	{
		if (mSendThread != null)
		{
			mSendThread.setResume();
		}
	}
	//----------------------------------------------------
	public void stopSend()
	{
		if (mSendThread != null)
		{
			mSendThread.setSuspendFlag();
		}
	}
	//----------------------------------------------------
	protected abstract void onDataReceived(ComBean ComRecData);
	protected abstract void getBettryVoltReceived(String[] batteryvolt);
}