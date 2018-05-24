package com.bean.serialport;

import android.util.Log;

import com.cedric.serialport.SerialPort;

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
			mOutputStream.write(bOutArray);
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
	StringBuilder sb ;
	private class ReadThread extends Thread {
		@Override
		public void run() {
			super.run();
			while(!isInterrupted()) {
				try
				{
					Log.e("ReadThread","test");
					if (mInputStream == null){
						return;
					}
					byte[] buffer=new byte[256];
					//int size = mInputStream.read(buffer);
		//-----------------------------------------------
					//判断拼接的字符串对象
					if (sb == null){
					  sb =new StringBuilder();
					}
					int size = mInputStream.read(buffer);
                          sleep(100);
					Log.d(TAG,"onDataReceived size. :  "+size);
                    if(size<3){
					  String Ser = new String(buffer,0,size);
					  sb.append(Ser);
					 }else {
					  String Ser = new String(buffer,0,size);
						sb.append(Ser);
						Log.d(TAG, "onDataReceived ... :  " + sb.toString());
						getBettryVoltReceived(sb.toString());

						if(sb.length()> size){
							Log.d(TAG,"onDataReceived  .. :  "+sb.toString());
							sb.delete(0,sb.length()+1);//拼接的字符串清除了 不用置为null， 避免重复创建
							Log.d(TAG,"onDataReceived  sb剩余 .. :  "+sb.toString());
						}
					}
		//----------------------------------------------
					if (size > 0){
						Log.e("ReadThread","test3   "+size);
						/*ComBean ComRecData = new ComBean(sPort,buffer,size);
						onDataReceived(ComRecData);*/
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
	protected abstract void getBettryVoltReceived(String batteryvolt);
}