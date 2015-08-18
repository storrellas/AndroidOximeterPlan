package com.neuroelectrics.stim;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.androidplot.LineRegion;
import com.androidplot.xy.XYPlot;

import com.neuroelectrics.stim.R;
import com.neuroelectrics.stim.bluetooth.BluetoothManager;
import com.neuroelectrics.stim.deviceManager.ChannelData;
import com.neuroelectrics.stim.deviceManager.DeviceManager;
import com.neuroelectrics.stim.deviceManager.DeviceRegisters;
import com.neuroelectrics.stim.deviceManager.StarStimProtocol;
import com.neuroelectrics.stim.deviceManager.DeviceManager.OpenErrorTypes;
import com.neuroelectrics.stim.deviceManager.iHandler.IDeviceManagerHandler;
import com.neuroelectrics.stim.deviceManager.iHandler.IScanDiscoveryFinishedHandler;
import com.neuroelectrics.stim.plotmanager.PlotManager;
import com.neuroelectrics.stim.stimulation.NubeConnector;
import com.neuroelectrics.stim.stimulation.StimulationManager;
import com.neuroelectrics.stim.stimulation.iHandler.IStimulationHandler;
import com.neuroelectrics.stim.util.ILoggerOutput;
import com.neuroelectrics.stim.util.Logger;
import com.neuroelectrics.stim.util.Reference;

public class MainClass extends Activity implements IScanDiscoveryFinishedHandler, ILoggerOutput, IDeviceManagerHandler, IStimulationHandler{

	
	//  ----------------
	//  -- ATTRIBUTES --
	//  ----------------
    
	// UI logger
	private Logger logger;	
	
	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;
    
	// FileOutputStream
	private FileOutputStream loggerFileOS;
	
	// API for the bluetooth services
	public BluetoothManager btManager;
	public DeviceManager _device;
    int _numOfChannels;
    boolean _isReceiverPresent = false;
		
	// Managers for the interface
    public PlotManager plotManager;
    public ProgressPopUpManager progressPopUpManager;
    public NubeConnector nubeConnector;
    
    
    // Controls the stimulation
    public StimulationManager stimulationManager;
    
	//  -------------
	//  -- METHODS --
	//  -------------
    
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
        // ------------------------------
        // Initialise UIElement
        // ------------------------------               
        UIElement.inflater          = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
		
		UIElement.macAddressText     = (EditText)       findViewById(R.id.macAddressText);
        UIElement.stimPlot           = (XYPlot)         findViewById(R.id.stimPlot);
        UIElement.sessionsContainer  = (LinearLayout)   findViewById(R.id.SessionsContainer);
        UIElement.launchStimButton   = (Button)         findViewById(R.id.launchStimulationButton);
        UIElement.backgroundForPopup = (RelativeLayout) findViewById(R.id.backgroundForPopup);
        UIElement.device_status      = (View)           findViewById(R.id.device_status);
        
        // Cheating section
        // ----------------------------------------------------
        //UIElement.macAddressText.setText("00:07:80:7C:5D:2A");
        UIElement.macAddressText.setText("00:07:80:64:EB:89");
        // ------------------------------
      
        // This prevent from going activity on pause
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
		// Starting Logger/Console	
		try {
			File dirLoggerFile = new File(Environment.getExternalStorageDirectory() + "/NIC");
			dirLoggerFile.mkdirs();
			
			// Create log file
			File loggerFile = new File(dirLoggerFile, "log.txt");
			loggerFile.createNewFile();
			loggerFileOS = new FileOutputStream(loggerFile);			
		} catch (Exception e) {
			e.printStackTrace();
		}
		// First Log
        logger = Logger.getInstance(this);		
		logger.info("Start Application", Logger.LOG_FILE_ON );
        
        
		// Checks whether BT is enabled or not
		checkBluetooth();
		
		
	    // Android for the bluetooth manager services
	    btManager        = BluetoothManager.getInstance(this, this);
	    _device          = new DeviceManager(this, this, this);
		_numOfChannels   = 8;

		// Create the StimulationManager object
		stimulationManager = new StimulationManager( _device, this );
		
		
		// Initialises the PlotManager for controlling the stimulation
		plotManager = new PlotManager(UIElement.stimPlot);     
		plotManager.configureStimPlot(stimulationManager.get_stimulationRampUpDuration(), 
									  stimulationManager.get_stimulationDuration(), 
									  stimulationManager.get_stimulationRampDownDuration());
		


		// Load stimulation templates
		nubeConnector = new NubeConnector();
		nubeConnector.loadStimParameters();
		
		// Initialise progressPopUp
		progressPopUpManager = new ProgressPopUpManager( this );
		
		// Initially button is disabled
		UIElement.device_status.setEnabled(false);
		
	}

	/**
	 * Checks whether bluetooth is present, and if its activated
	 */
	private void checkBluetooth() {

		// Get local Bluetooth adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		

    	logger.info("Checking whether bluetooth needs to be activated ...", Logger.LOG_FILE_ON | Logger.VISUAL_CONSOLE_ON);
    	
		// If BT is not on, request that it be enabled.
		if (!mBluetoothAdapter.isEnabled()) {						
			Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, 0);
		}	
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		
        findViewById(R.id.mainLayout).requestFocus();
		
		return true;
	}


	@Override
	public void setVisualLog(String msg) {
		// Do nothing
	}

	@Override
	public void setFileLog(String msg) {
		try {
			loggerFileOS.write( msg.getBytes() );
			loggerFileOS.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	/**
	 * Handle for the Open Device button
	 * @param view
	 */
	public void openDeviceHandle(View view){
		Log.w("com.neuroelectrics", "Clicked OpenDevice");	
		
		logger.info("Clicked Open device", Logger.LOG_FILE_ON );
			    	
	    // Open the device
		String macAddress = UIElement.macAddressText.getText().toString();
		logger.info("Connecting to " + macAddress + "...", Logger.VISUAL_CONSOLE_ON | Logger.LOG_FILE_ON);

		// Open Device
		OpenErrorTypes openErrorTypes = _device.openDevice(macAddress, true);
		if( openErrorTypes == OpenErrorTypes.ERROR_NO_ERROR){
			
			_isReceiverPresent = true;
			logger.info("Opening device successful", Logger.VISUAL_CONSOLE_ON | Logger.LOG_FILE_ON);
						
	        // Set device to be StarStim
	        String deviceCurrentName = this.getDeviceName( macAddress );
	        if( deviceCurrentName.equals("NE-STARSTIM") ){
	            _numOfChannels = 8;
	            _device.setIsStarStim(true);
	        }
	        if( deviceCurrentName.equals("NE-ENOBIO8")){
	            _numOfChannels = 8;
	            _device.setIsStarStim(false);
	        }
	        if( deviceCurrentName.equals("NE-ENOBIO20")){
	            _numOfChannels = 20;
	            _device.setIsStarStim(false);
	        }
	        if( deviceCurrentName.equals("NE-ENOBIO32") ){
	            _numOfChannels = 32;
	            _device.setIsStarStim(false);
	        }

	        // Notify the consumers the number of channels available
            _device.setNumOfChannels(_numOfChannels);
//            _fileWriter.setNumOfChannels(_numOfChannels);
//            _fileWriter.setRecordingEASYFile(true);
            
            
	        logger.info("Opened device '" + deviceCurrentName + "' (" + macAddress + ")", Logger.VISUAL_CONSOLE_ON | Logger.LOG_FILE_ON);
	        logger.info("Setting number of channels to " + _device.getNumOfChannels() , Logger.VISUAL_CONSOLE_ON | Logger.LOG_FILE_ON);
	        logger.info("Stimulation device '" + ((_device.isAStimulationDevice()==true)?"true":"false") + "' ", Logger.VISUAL_CONSOLE_ON | Logger.LOG_FILE_ON);
			
	        
	        
	        // Launching EEG Registers
	        logger.info("Launching initEEG Regsiters", Logger.VISUAL_CONSOLE_ON | Logger.LOG_FILE_ON);
	        _initEEGRegisters( true );
	        

	        
	        // Request firmware version
	        SystemClock.sleep(500); // Wait for a bit of time
	        this.reportFirmwareVersion();
	        
    		logger.info("Device opened successfully", Logger.VISUAL_CONSOLE_ON | Logger.LOG_FILE_ON);

    		// Message for the user to signal Open Sucessful
    		Toast.makeText(getApplicationContext(), "Device opened successfully =)", Toast.LENGTH_LONG).show();

    		UIElement.device_status.setEnabled(true);
	    }else{
		    // General failure
	        logger.info("Error on opening device", Logger.VISUAL_CONSOLE_ON | Logger.LOG_FILE_ON);
	        
    		// Message for the user to signal Close
    		Toast.makeText(getApplicationContext(), "Error on opening device", Toast.LENGTH_LONG).show();
	    }
		

		
	}

	
	/*!
	 * Report firmware version
	 */
	public void reportFirmwareVersion(){
		logger.info("Device firmware version is " + _device.getFirmwareVersion(), Logger.LOG_FILE_ON);

		if( _device.getFirmwareVersion() < DeviceManager.FWVERSION_ANDROID_COMPATIBLE ){
			Toast.makeText(this, "Firmware version is not compatible with Android NIC! Closing device", Toast.LENGTH_LONG).show();
			this.closeDeviceHandle( getWindow().getDecorView().findViewById(android.R.id.content) );
			return;
		}
		
		logger.info("Configure Android to run at 125 SPS", Logger.LOG_FILE_ON);
	    Byte[] sampleRate = new Byte[1];
		//sampleRate[0] = (byte) DennisRegisters.EEG_STREAMING_RATE_125SPS;
		sampleRate[0] = 4;
		_device.writeRegister(DeviceManager.StarStimRegisterFamily.EEG_REGISTERS, (byte) DeviceRegisters.EEG_STREAMING_RATE_ADDR, sampleRate, (byte) 1);
		
////////////////////////////////		
		if(true) return;
///////////////////////////////
		
		int multisampleREG, sampleBeaconREG;
	    if( _device.is1000SPS() == 1 ){
	        multisampleREG = 1; sampleBeaconREG = 2;
	    }else{
	        if( _device.getFirmwareVersion() >= 1201 ){
	            multisampleREG = 1; sampleBeaconREG = 3;
	        }else{
	            multisampleREG = 0; sampleBeaconREG = 1;
	        }
	    }


	    
	    // Configure Compression
	    if( _device.getFirmwareVersion() >= 1202 && (_device.is1000SPS()== 0) ){
	        //_device.eegCompressionType( StarStimProtocol.EEGCompressionType.EEG_NO_COMPRESSION );
	        //_device.eegCompressionType( StarStimProtocol.EEGCompressionType.EEG_16BIT_COMPRESSION );
	        _device.eegCompressionType( StarStimProtocol.EEGCompressionType.EEG_12BIT_COMPRESSION );
	    }

	    // Configure Samples per Beacon
	    _device.samplesPerBeacon( sampleBeaconREG );
	    _device.multipleSample  ( multisampleREG  );	    
	    
	}
	
	/**
	 * Handle for the Close Device button
	 * @param view
	 */
	public void closeDeviceHandle(View view){
		Log.w("com.neuroelectrics", "Clicked CloseDevice");
				
    	logger.info("Closing device", Logger.LOG_FILE_ON);
    	
	    // Close the device
	    _isReceiverPresent = _device.closeDevice( false );
	    if(_isReceiverPresent){
	        logger.info("Device Closed successfully", Logger.LOG_FILE_ON);
    		// Message for the user to signal
    		Toast.makeText(getApplicationContext(), "Device closed successfully", Toast.LENGTH_LONG).show();
    		
    		// Initially button is disabled
    		UIElement.device_status.setEnabled(false);

	    }else{
	    	logger.info("Error on closing device", Logger.LOG_FILE_ON);
	    	
    		// Message for the user to signal
    		Toast.makeText(getApplicationContext(), "Device failed to close", Toast.LENGTH_LONG).show();
	    }
		
	}
	
	/**
	 * Handle for the Exit Application button
	 * @param view
	 */
	public void exitAppHandle(View view){				
    	logger.info("Exiting application", Logger.LOG_FILE_ON);
    	
    	// Unregister the application
    	btManager.unregisterReceiver(this);
		
    	// Finishes the application
        finish();
        System.exit(0);		
		
	}
	
	/**
	 * This class treats when a new device status is received
	 * @author sergi
	 *
	 */
	private class InitStimulationThread extends Thread{
		
		@Override
		public void run() {
			stimulationManager.initStimulation();
			
			// Enable ok button
			progressPopUpManager.enableOkButton();
			
			
			
		}
		
	}
	
	/**
	 * Handle for the Launch Stimulation button
	 * @param view
	 */
	public void launchStimulationHandle(View view){
		
		logger.info("New message", Logger.LOG_FILE_ON);		
		this.progressPopUpManager.show();
    
		// Show a toast
		if( !_device.isAStimulationDevice() ){
			Toast.makeText(getApplicationContext(), "Device is not starstim. Please revise ...", Toast.LENGTH_LONG).show();
			return;
		}
		
		if( !_isReceiverPresent ){
			logger.info("Recieved not connected!", Logger.LOG_FILE_ON);
			return;
		}
		
        // kick off the data generating thread:
		new InitStimulationThread().start();

				

	}
		
	
	public void onClick(View view){
		logger.info("Clicked button", Logger.LOG_FILE_ON);
	}

	/**
	 * Handle to say ok for the pop up
	 * @param view
	 */
	public void okpopupbuttonHandler(View view){
		logger.info("Clicked button ok in pop up window", Logger.LOG_FILE_ON);
		
		// Hide popup manager
		progressPopUpManager.hide();
		
		// start stimulation on device
		stimulationManager.startStimulation();
		
		// Notify stimulation started
		notifyStimulationStarted();
	}
	
	
    /**
     * Obtains the deviceName
     */
	public String getDeviceName(String macAddress){
		
		int i;
	    int numOfPairedDevices = DeviceManager.getNumberOfPairedDevices(this, this);
	    //loggerMacroDebug("Number of paired devices " + QString::number(numOfPairedDevices));

	    // Search in paired devices
	    Reference <String> macAddressCandidate = new Reference<String>("");
	    Reference<String> nameDevice = new Reference<String>("");
	    for(i = 0; i < numOfPairedDevices; i++){
	        // Get information from device
	        int result = DeviceManager.getPairedDeviceInfo(i, nameDevice, macAddressCandidate, this, this);

	        if( result == 1 && macAddressCandidate.get().equals(macAddress)) break;
	        
	    }

	    if( i == numOfPairedDevices ){
	    	logger.info("There was an error retrieving device name", Logger.VISUAL_CONSOLE_ON | Logger.LOG_FILE_ON);
	    }

	    return nameDevice.get();
		
	}
	
	/**
	 * Initialises the registres from EEG bank
	 */
	public void _initEEGRegisters( boolean all )
	{
		
		long startTime = System.currentTimeMillis();
	    
	    logger.info( "Manager._initEEGRegisters all:" + all, Logger.VISUAL_CONSOLE_ON | Logger.LOG_FILE_ON);
	    if (!_device.isAStimulationDevice())
	    {
			Byte[] REG = new Byte[1];
			Byte[] REGRead = new Byte[1];
	        //CONFIG

	        //Is old HW
	        if (_device.getFirmwareVersion()<593)
	            REG[0] = (byte) 0xA6;
	        else{
	            if( _device.is1000SPS() == 1) REG[0] = (byte) 0x85;//ALERT 1000 SPS
	            else REG[0] = (byte) 0x86;
	        }

	        // Register 0x01
//	        logger.info("Writing register EEG_REG(" + 0x01 + ")=" + String.format("0x%02X",REG[0]), Logger.LOG_FILE_ON);
	        _device.writeRegister(DeviceManager.StarStimRegisterFamily.EEG_REGISTERS, (byte) 0x01, REG, (byte) 1);

//	        _device.readRegister(DeviceManager.StarStimRegisterFamily.EEG_REGISTERS, (byte) 0x01, REGRead, (byte) 1);
//	        logger.info( "Reading register EEG_REG(" + 0x01 + ")="  + String.format("0x%02X Expected=0x%02X", REGRead[0], REG[0]), Logger.LOG_FILE_ON );
	        
	        // Register 0x02
	        REG[0] = 0x10;
//	        logger.info("Writing register EEG_REG(" + 0x02 + ")=" + String.format("0x%02X",REG[0]), Logger.LOG_FILE_ON);
	        _device.writeRegister(DeviceManager.StarStimRegisterFamily.EEG_REGISTERS, (byte) 0x02, REG, (byte) 1);

//	        _device.readRegister(DeviceManager.StarStimRegisterFamily.EEG_REGISTERS, (byte) 0x02, REGRead, (byte) 1);
//	        logger.info( "Reading register EEG_REG(" + 0x02 + ")="  + String.format("0x%02X Expected=0x%02X", REGRead[0], REG[0]), Logger.LOG_FILE_ON );

	        // Register 0x03
	        REG[0] = (byte) 0xCC;
//	        logger.info("Writing register EEG_REG(" + 0x03 + ")=" + String.format("0x%02X",REG[0]), Logger.LOG_FILE_ON);
	        _device.writeRegister(DeviceManager.StarStimRegisterFamily.EEG_REGISTERS, (byte) 0x03, REG, (byte) 1);

//	        _device.readRegister(DeviceManager.StarStimRegisterFamily.EEG_REGISTERS, (byte) 0x03, REGRead, (byte) 1);
//	        logger.info( "Reading register EEG_REG(" + 0x03 + ")="  + String.format("0x%02X Expected=0x%02X", REGRead[0], REG[0]), Logger.LOG_FILE_ON );

	        if (all)
	        {
	            // LOFF
	            REG[0] = 0x00;
//		        logger.info("Writing register EEG_REG(" + 0x04 + ")=" + String.format("0x%02X",REG[0]), Logger.LOG_FILE_ON);
		        _device.writeRegister(DeviceManager.StarStimRegisterFamily.EEG_REGISTERS, (byte) 0x04, REG, (byte) 1);

//		        _device.readRegister(DeviceManager.StarStimRegisterFamily.EEG_REGISTERS, (byte) 0x04, REGRead, (byte) 1);
//		        logger.info( "Reading register EEG_REG(" + 0x04 + ")="  + String.format("0x%02X Expected=0x%02X", REGRead[0], REG[0]), Logger.LOG_FILE_ON );

	            // CHANNEL
	            REG[0] = 0x00;
//		        logger.info("Writing register EEG_REG(" + 0x05 + ")=" + String.format("0x%02X",REG[0]), Logger.LOG_FILE_ON);
		        _device.writeRegister(DeviceManager.StarStimRegisterFamily.EEG_REGISTERS, (byte) 0x05, REG, (byte) 1);

//		        _device.readRegister(DeviceManager.StarStimRegisterFamily.EEG_REGISTERS, (byte) 0x05, REGRead, (byte) 1);
//		        logger.info( "Reading register EEG_REG(" + 0x05 + ")="  + String.format("0x%02X Expected=0x%02X", REGRead[0], REG[0]), Logger.LOG_FILE_ON );

	            // REST OF THE REGISTERS
		        Byte[] values = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
	            				 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};  // There are 13 values

//		        logger.info("Writing register EEG_REG(" + 0x0D + ")=" + String.format("0x%02X",REG[0]), Logger.LOG_FILE_ON);
		        _device.writeRegister(DeviceManager.StarStimRegisterFamily.EEG_REGISTERS, (byte) 0x0D, values, (byte) 13);

//	            for (int i=0;i<13;i++)
//	            {
//			        _device.readRegister(DeviceManager.StarStimRegisterFamily.EEG_REGISTERS, (byte) (0x0D+i), REGRead, (byte) 1);
//			        logger.info( "Reading register EEG_REG(" + 0x0D + ")="  + String.format("0x%02X Expected=0x%02X", REGRead[0], REG[0]), Logger.LOG_FILE_ON );
//	                
//	            }


	            //EEG_CH_INFO_ADDR	           
	            int shift = 1;
	            int aux = 0;
	            for (int i = 0;i < _numOfChannels; i++)
	                	aux = aux | (shift <<i);
	            aux = ~ aux;
	            
	            // Byte0
	            REG[0] = (byte) (aux & 0x000000FF);
////////////////////////////////////////////	            
	            //REG[0] = (byte) 0xFE;
////////////////////////////////////////////	            
//		        logger.info("Writing register EEG_REG(" + 0x40 + ")=" + String.format("0x%02X",REG[0]), Logger.LOG_FILE_ON);
	            _device.writeRegister(DeviceManager.StarStimRegisterFamily.EEG_REGISTERS, (byte) 0x40, REG, (byte) 1);

//	            _device.readRegister(DeviceManager.StarStimRegisterFamily.EEG_REGISTERS, (byte) 0x40, REGRead, (byte) 1);
//		        logger.info( "Reading register EEG_REG(" + 0x40 + ")="  + String.format("0x%02X Expected=0x%02X", REGRead[0], REG[0]), Logger.LOG_FILE_ON );
		        
		        // Byte 1
	            REG[0] = (byte) ((aux & 0x0000FF00) >> 8);
//		        logger.info("Writing register EEG_REG(" + 0x41 + ")=" + String.format("0x%02X",REG[0]), Logger.LOG_FILE_ON);
	            _device.writeRegister(DeviceManager.StarStimRegisterFamily.EEG_REGISTERS, (byte) 0x41, REG, (byte) 1);

//	            _device.readRegister(DeviceManager.StarStimRegisterFamily.EEG_REGISTERS, (byte) 0x41, REGRead, (byte) 1);
//		        logger.info( "Reading register EEG_REG(" + 0x41 + ")="  + String.format("0x%02X Expected=0x%02X", REGRead[0], REG[0]), Logger.LOG_FILE_ON );
	            
		        // Byte 2
	            REG[0] = (byte) ((aux & 0x00FF0000) >> 16);
//		        logger.info("Writing register EEG_REG(" + 0x42 + ")=" + String.format("0x%02X",REG[0]), Logger.LOG_FILE_ON);
	            _device.writeRegister(DeviceManager.StarStimRegisterFamily.EEG_REGISTERS, (byte) 0x42, REG, (byte) 1);

//	            _device.readRegister(DeviceManager.StarStimRegisterFamily.EEG_REGISTERS, (byte) 0x42, REGRead, (byte) 1);
//		        logger.info( "Reading register EEG_REG(" + 0x42 + ")="  + String.format("0x%02X Expected=0x%02X", REGRead[0], REG[0]), Logger.LOG_FILE_ON );
	            
		        // Byte 3
	            REG[0] = (byte) ((aux & 0xFF000000) >> 24);            
//		        logger.info("Writing register EEG_REG(" + 0x43 + ")=" + String.format("0x%02X",REG[0]), Logger.LOG_FILE_ON);
	            _device.writeRegister(DeviceManager.StarStimRegisterFamily.EEG_REGISTERS, (byte) 0x43, REG, (byte) 1);

//	            _device.readRegister(DeviceManager.StarStimRegisterFamily.EEG_REGISTERS, (byte) 0x43, REGRead, (byte) 1);
//		        logger.info( "Reading register EEG_REG(" + 0x43 + ")="  + String.format("0x%02X Expected=0x%02X", REGRead[0], REG[0]), Logger.LOG_FILE_ON );
	        }// End: If (all)
	    }
	    else
	    {

	    	boolean result;
	        
			Byte[] REG = new Byte[1];
			REG[0] = 0x00;
			Byte[] REGRead = new Byte[1];
	        
			// CH_INFO
	        result=_device.writeRegister(DeviceManager.StarStimRegisterFamily.STIM_REGISTERS, (byte) 4, REG, (byte) 1);
/*
//	        if (!result)
//	            logger.info( "######################ERROR _initEEGRegisters" );
//	        result=_device.readRegister(DeviceManager.StarStimRegisterFamily.EEG_REGISTERS, (byte) 4, REGRead, (byte) 1);

//	        if (!result)
//	            logger.info( "######################ERROR _initEEGRegisters" );
//
////	        logger.info ("REGRead:" +  REGRead[0]);
//
//	        unsigned char values[25] = {0x86, 0x00, 0xCC, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,0x00,
//	                                   0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
//	                                   0x00, 0x00, 0x00, 0x00, 0x00};
//
//
	        
	        byte eegRegIs1000SPS;
	        if( _device.is1000SPS() == 1) eegRegIs1000SPS = (byte) 0x85;//ALERT 1000 SPS
	        else eegRegIs1000SPS = (byte) 0x86;

	        Byte[] values1 = {eegRegIs1000SPS, 0x00, (byte) 0xCC, 0x00, 0x00};


	        Byte[] values2 = {0x00, 0x00, 0x00, 0x00,0x00};

	        Byte[] values3 = {0x00, 0x00, 0x00, 0x00, 0x00};

	        Byte[] values4 = {0x00, 0x00, 0x00, 0x00, 0x00};

	        Byte[] values5 = {0x00, 0x00, 0x00, 0x00, 0x00};

	        Byte[] readValues = new Byte[5];

	        int dir = 1;

	        // Writing dir = 1
	        result = _device.writeRegister(DeviceManager.StarStimRegisterFamily.EEG_REGISTERS, (byte) dir, values1, (byte) 5);
//	        if (!result)
//            logger.info( "######################ERROR _initEEGRegisters" );
	        result = _device.readRegister(DeviceManager.StarStimRegisterFamily.EEG_REGISTERS, (byte) dir, readValues, (byte) 5);
//	        if (!result)
//            logger.info( "######################ERROR _initEEGRegisters" );
//	        logger.info("readValues:" + readValues[0] + " " + readValues[1] + " " + readValues[2] + " " + readValues[3] + " " + readValues[4]);

	        // Writing dir = 6
	        dir += 5;
	        result = _device.writeRegister(DeviceManager.StarStimRegisterFamily.EEG_REGISTERS, (byte) dir, values2, (byte) 5);
//	        if (!result)
//            logger.info( "######################ERROR _initEEGRegisters" );
	        result = _device.readRegister(DeviceManager.StarStimRegisterFamily.EEG_REGISTERS, (byte) dir, readValues, (byte) 5);
//	        if (!result)
//            logger.info( "######################ERROR _initEEGRegisters" );
//	        logger.info("readValues:" + readValues[0] + " " + readValues[1] + " " + readValues[2] + " " + readValues[3] + " " + readValues[4]);

	        
	        // Writing dir = 11
	        dir+=5;
	        result = _device.writeRegister(DeviceManager.StarStimRegisterFamily.EEG_REGISTERS, (byte) dir, values3, (byte) 5);
//	        if (!result)
//            logger.info( "######################ERROR _initEEGRegisters" );
//	        result = _device.readRegister(DeviceManager.StarStimRegisterFamily.EEG_REGISTERS, (byte) dir, readValues, (byte) 5);
//	        if (!result)
//            logger.info( "######################ERROR _initEEGRegisters" );
//	        logger.info("readValues:" + readValues[0] + " " + readValues[1] + " " + readValues[2] + " " + readValues[3] + " " + readValues[4]);

	        // Writing dir = 16
	        dir+=5;
	        result = _device.writeRegister(DeviceManager.StarStimRegisterFamily.EEG_REGISTERS, (byte) dir, values4, (byte) 5);
//	        if (!result)
//            logger.info( "######################ERROR _initEEGRegisters" );
//	        result = _device.readRegister(DeviceManager.StarStimRegisterFamily.EEG_REGISTERS, (byte) dir, readValues, (byte) 5);
//	        if (!result)
//            logger.info( "######################ERROR _initEEGRegisters" );
//	        logger.info("readValues:" + readValues[0] + " " + readValues[1] + " " + readValues[2] + " " + readValues[3] + " " + readValues[4]);

	        // Writing dir = 21
	        dir+=5;
	        result = _device.writeRegister(DeviceManager.StarStimRegisterFamily.EEG_REGISTERS, (byte) dir, values5, (byte) 5);
//	        if (!result)
//            logger.info( "######################ERROR _initEEGRegisters" );
//	        result = _device.readRegister(DeviceManager.StarStimRegisterFamily.EEG_REGISTERS, (byte) dir, readValues, (byte) 5);
//	        if (!result)
//            logger.info( "######################ERROR _initEEGRegisters" );
//	        logger.info("readValues:" + readValues[0] + " " + readValues[1] + " " + readValues[2] + " " + readValues[3] + " " + readValues[4]);
/**/	        
	        
	        // ---------------------------------------------------

	        
	        byte eegRegIs1000SPS;
	        if( _device.is1000SPS() == 1) eegRegIs1000SPS = (byte) 0x85;//ALERT 1000 SPS
	        else eegRegIs1000SPS = (byte) 0x86;
	        
	        
	        Byte[] initArray = new Byte[26];
	        int i;
	        for( i = 0; i < 26; i++) initArray[i] = 0x00;
	        initArray[0] = eegRegIs1000SPS;
	        initArray[2] = (byte) 0xCC;

	        // Write register
	        result = _device.writeRegister(DeviceManager.StarStimRegisterFamily.EEG_REGISTERS, (byte) DeviceRegisters.EEG_REGS_CONFIG_1_ADDR, initArray, (byte) 26);


	        // Check correctness
	        int retriesPerformed = _device.checkRegWritten(initArray, DeviceManager.StarStimRegisterFamily.EEG_REGISTERS, DeviceRegisters.EEG_REGS_CONFIG_1_ADDR, 26, 5);
	        if( retriesPerformed < 5 ) logger.info("EEG Registers : Read registers OK", Logger.LOG_FILE_ON);
	        else logger.info("EEG Registers : Read registers FAIL", Logger.LOG_FILE_ON);
	        
	        
	        // ---------------------------------------------------
	        
	    }

	    long stopTime = System.currentTimeMillis();
	    long elapsedTime = stopTime - startTime;
	    logger.info( "Manager::_initEEGRegisters end " + elapsedTime + "[ms]", Logger.VISUAL_CONSOLE_ON | Logger.LOG_FILE_ON);
	}

	@Override
	public void reportBatteryLevel(int level) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reportFirmwareVersion(int firmwareVersion, int is1000sps) {

		String rateStr = (is1000sps==1)?"1000SPS":"500SPS";		
		logger.info("Current firmware version is " + firmwareVersion + " @" + rateStr , Logger.LOG_FILE_ON);
		
		//if( firmwareVersion < 1225 ){
		if( firmwareVersion < 1230 ){
			Toast.makeText(this, "Firmware version is not compatible with Android NIC!", Toast.LENGTH_LONG).show();
		}
		
		
	}

	@Override
	public void newAccelerometerData(ChannelData data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void newStimulationData(ChannelData data) {
		// TODO Auto-generated method stub
		
	}

	
	@Override
	public void newEEGData(ChannelData channelData) {
		// TODO Auto-generated method stub
		
		if( channelData == null ){
			logger.info("channelData is null ", Logger.LOG_FILE_ON);
			return;
		}
		        
		
	}

	@Override
	public void newImpedanceData(ChannelData data, long timeStamp) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * This class treats when a new device status is received
	 * @author sergi
	 *
	 */
	private class NewDeviceStatusThread extends Thread{

		int status;
		private NewDeviceStatusThread(int _status){ status = _status; }
		
		@Override
		public void run() {
			// Device is OFF
		    if (status == 255)
		    {
		        _isReceiverPresent = false;
		        //emit DeviceIsON(false);
		        return;
		    }
		    else
		    {
		    	
		        // Checking whether status bit for EEG
		        if ((status & DeviceManager.StatusByteBits.EEG_BIT.getVal()) != 0x00)
		        {
		            logger.info( "EEG-ON", Logger.LOG_FILE_ON);
		            //emit setLedOnStreaming(true);
		            _device.setDeviceStreaming(true);

		        }
		        else
		        {
		            logger.info( "EEG-OFF", Logger.LOG_FILE_ON);
		            //emit setLedOnStreaming(false);
		            _device.setDeviceStreaming(false);

//		            if (_device.getFirmwareVersion()>=DeviceManager.FWVERSION_SDCARD)
//		                if (_isSDCardRecordingPending)
//		                    startStreaming(_isAccelerometer(),true);
		        }
		    	
		        // Checking whether status bit for SDCARD Recording
		        if (_device.getFirmwareVersion()>=DeviceManager.FWVERSION_SDCARD)
		        {
		            if ( (status & DeviceManager.StatusByteBits.SDCARD_BIT.getVal()) != 0x00){
		            	logger.info( "SDCARD-ON", Logger.LOG_FILE_ON);
		            }else{
		            	logger.info("SDCARD-OFF", Logger.LOG_FILE_ON);
		            }
		                

		            //emit sdCardStatus(status & DeviceManager::SDCARD_BIT);
		        }

		        // Checking whether status bit for IMPEDANCE
		        if ( (status & DeviceManager.StatusByteBits.IMP_BIT.getVal()) != 0x00){	            
	            	logger.info( "IMP-ON", Logger.LOG_FILE_ON);
		        }else{
		        	logger.info( "IMP-OFF", Logger.LOG_FILE_ON);
		        }



		        // Checking whether status bit for STIMULATION
		        if ( (status & DeviceManager.StatusByteBits.STM_BIT.getVal()) != 0x00)
		        {
		            logger.info( "STM-ON", Logger.LOG_FILE_ON);
		        }
		        else
		        {
		            logger.info( "STM-OFF", Logger.LOG_FILE_ON);

		            //_isCheckingStimDataRate=false;

		            boolean aborted = false;

		            if ((_device.isDeviceStimulating())&&(_device.getFirmwareVersion()>=485))
		            {
		            	logger.info("Reading register status", Logger.LOG_FILE_ON);
		                //We read register 114 to check the reason for the stop
		                Byte[] stimResultREG = new Byte[1];
		                _device.readRegister(DeviceManager.StarStimRegisterFamily.STIM_REGISTERS, (byte) DeviceRegisters.STM_ERROR_ADDR, stimResultREG, (byte) 1);
		                byte stimResult = stimResultREG[0];
		                logger.info( "Register 114(STM_ERROR_ADDR) is " + stimResult, Logger.LOG_FILE_ON);

		                if (stimResult==1)
		                {
		                    //qDebug()<<"Stimulation was aborted due to an impedance problem.";
		                    //emit newMessage(5, "Stimulation was aborted by the NECBOX due to an impedance problem.");
		                    aborted=true;
		                }
		                else if (stimResult==2)
		                {
		                    //qDebug()<<"Stimulation was aborted by StarStim due to a communication problem.";
		                    //emit newMessage(5, "Stimulation was aborted by the NECBOX due to a communication problem.");
		                    aborted=true;
		                }
//		                else if (stimResult==3)
//		                {
//		                    //qDebug()<<"Stimulation was aborted due to a stop command sent from NIC.";
//		                    emit newMessage(5, "Stimulation was aborted due to a stop command sent from NIC.");
//		                }

		                //emit stimulationFinishedStatus(stimResult);
		            }
		            else
		                //emit stimulationFinishedStatus(0xFF);

		            _device.setDeviceStimulating(false);

		            //emit stimulationFinished(aborted);
		        }


		    }
			
		}
		
	}
	
	@Override
	public void newDeviceStatus(int status) {
		logger.info("New Device status received " + status , Logger.LOG_FILE_ON);
		
		
        // kick off the data generating thread:		
        new NewDeviceStatusThread(status).start();
        

	}

	@Override
	public void startBlinking() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stopBlinking() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void newPacketLossData(double percentage) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onScanNeighborhoodFinished(ArrayList<String> mPairedDevicesArrayAdapter, ArrayList<String> mNewDevicesArrayAdapter) {
		int i = 0;
		
    	logger.info("++ Paired devices ++", Logger.LOG_FILE_ON);
    	for( String str : mPairedDevicesArrayAdapter ){
    		str = str.replace('\n', ' ');
    		logger.info( " " + i + "." + " " + str,  Logger.LOG_FILE_ON);
    		i++;
    	}    	
    	if(mPairedDevicesArrayAdapter.size() == 0){
    		logger.info( "NO PAIRED DEVICES", Logger.VISUAL_CONSOLE_ON | Logger.LOG_FILE_ON);
    	}
    	
    	//
    	i = 0;
    	logger.info("++ Non-paired devices ++", Logger.VISUAL_CONSOLE_ON | Logger.LOG_FILE_ON);
    	for( String str : mNewDevicesArrayAdapter ){
    		str = str.replace('\n', ' ');
    		logger.info(" " + i + "." + " " + str, Logger.VISUAL_CONSOLE_ON | Logger.LOG_FILE_ON);
    		i++;
    	}
    	if(mNewDevicesArrayAdapter.size() == 0){
    		logger.info( "NO NON-PAIRED DEVICES", Logger.VISUAL_CONSOLE_ON | Logger.LOG_FILE_ON);
    	}
		
	}

	
	/**
	 * Handles the stimulation started event
	 */
	@Override
	public void notifyStimulationStarted() {
		
		
		Toast.makeText(this, "Stimulation started!", Toast.LENGTH_LONG).show();

		// task performed when the timer is ellapsed
		TimerTask timerTask = new TimerTask() {
    		int ellapsedSeconds = 0;
        	
      	  	@Override
      	  	public void run() {
      		  ellapsedSeconds ++;
      		  logger.info("Ellapsed "+ ellapsedSeconds  + " [s]", Logger.LOG_FILE_ON);
      		  plotManager.stimulationProgress(ellapsedSeconds);
      		  if (ellapsedSeconds > 20){
      			  logger.info("Cancelling Timer task", Logger.LOG_FILE_ON);
      			  this.cancel();
      		  }
      	  	}
      	  	
      	};
		
        // Set the timer to simulate a stimulation is taking place
        Timer timer = new Timer();
        timer.schedule( timerTask, 1000, 1000 );

		
		
	}
    

	
	
}
