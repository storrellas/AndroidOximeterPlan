package com.neuroelectrics.stim.stimulation;

import java.util.Arrays;

import android.util.Log;


import com.neuroelectrics.stim.deviceManager.DeviceManager;
import com.neuroelectrics.stim.deviceManager.DeviceRegisters;
import com.neuroelectrics.stim.stimulation.iHandler.IStimulationHandler;
import com.neuroelectrics.stim.util.Logger;
import com.neuroelectrics.stim.util.Reference;

public class StimulationManager {

	/**
	 * ! \enum StimulationType
	 * 
	 * Enumeration of the bit positions in the status byte.
	 */
	public enum StimulationType {
		NOT_USED(0), EEG_RECORDING(1), STIMULATION_ELECTRODE(2), RETURN_ELECTRODE(3);

		private int numVal;

		StimulationType(int numVal) {
			this.numVal = numVal;
		}

		public int getVal() {
			return numVal;
		}

	};
	
	/**
	 * This class holds the stimulation parameters for a given channel 
	 * @author sergi
	 *
	 */
    public class StimulationConfigBean  {
    	
    	public StimulationType    stimType;
    	public int                Atdcs;
    	public int                Atacs;
    	public double             Ftacs;
		public int                Ptacs;
    	public int                Atrns;
        public int                posIndex;
        
        /**
         * Public constructor
         */
        public StimulationConfigBean(){
        	stimType = StimulationType.NOT_USED;
        	Atdcs = Atacs = 0;
        	Ftacs = Ptacs = 0;
        	Atrns = 0;
        	posIndex = 0;
        }
    }

	
	//  ----------------
	//  -- ATTRIBUTES --
	//  ----------------
    
	// UI logger
	private Logger logger;
	
	// DeviceManager
	public DeviceManager _device;
	
	// Configuration of the stimulation
	private StimulationConfigBean[] stimConfigBean;
	
    private int _stimulationDuration;
    private int _stimulationRampDownDuration;
    private int _stimulationRampUpDuration;
	
	// Number of stimulation channels
	private static final int NUM_STIM_CHANNELS = 8;
	
	// IStimulationHandler
	private IStimulationHandler handler;
	
	//  ----------------
	//  -- METHODS    --
	//  ----------------	
	
	/**
	 * Public constructor
	 */
	public StimulationManager( DeviceManager device, IStimulationHandler _handler ){
		logger = Logger.getInstance();
		
		// Initialise configuration with 8 channel
		stimConfigBean = new StimulationConfigBean[8];
		for( int i = 0; i < NUM_STIM_CHANNELS; i++){
			stimConfigBean[i] = new StimulationConfigBean();
		}

		// Stores DeviceManager
		_device = device;
		
		// Configure Stimulation
	    _stimulationRampDownDuration = 5;
	    _stimulationDuration         = 10;
	    _stimulationRampUpDuration   = 5;
		
		// Configures the Fake - Stimulation
		configureStimulation();
		
		// Stimulation handler
		handler = _handler;
		
	}
	
	/**
	 * Getters and setters for stimulationDuration, stimulationRampUpDuration, stimulationRampDownDuration
	 * @return
	 */
	public int get_stimulationDuration() {
		return _stimulationDuration;
	}

	public int get_stimulationRampDownDuration() {
		return _stimulationRampDownDuration;
	}

	public int get_stimulationRampUpDuration() {
		return _stimulationRampUpDuration;
	}

	public void set_stimulationDuration(int _stimulationDuration) {
		this._stimulationDuration = _stimulationDuration;
	}

	public void set_stimulationRampDownDuration(int _stimulationRampDownDuration) {
		this._stimulationRampDownDuration = _stimulationRampDownDuration;
	}

	public void set_stimulationRampUDuration(int _stimulationRampUpDuration) {
		this._stimulationRampUpDuration = _stimulationRampUpDuration;
	}
	
	
	/**
	 * Configure stimulation
	 */
	public void configureStimulation(){
		
	    // Channel 0
		stimConfigBean[0].stimType = StimulationType.RETURN_ELECTRODE;  // Return Electrode - Cathodal
		stimConfigBean[0].Atdcs = 0;
		stimConfigBean[0].Atacs = 0;
		stimConfigBean[0].Ftacs = 0;
		stimConfigBean[0].Ptacs = 180;
		stimConfigBean[0].Atrns = 0;
		stimConfigBean[0].posIndex = 12;


	    // Channel 1
		stimConfigBean[1].stimType = StimulationType.STIMULATION_ELECTRODE;  // Stimulation Electrode - Anodal
		stimConfigBean[1].Atdcs = -2000;
		stimConfigBean[1].Atacs = 0;
		stimConfigBean[1].Ftacs = 0;
		stimConfigBean[1].Ptacs = 0;
		stimConfigBean[1].Atrns = 0;
		stimConfigBean[1].posIndex = 3;

	    // Channel 2
		stimConfigBean[2].stimType = StimulationType.EEG_RECORDING;  // EEG Measuring
		stimConfigBean[2].Atdcs = 0;
		stimConfigBean[2].Atacs = 0;
		stimConfigBean[2].Ftacs = 0;
		stimConfigBean[2].Ptacs = 0;
		stimConfigBean[2].Atrns = 0;
		stimConfigBean[2].posIndex = 2;

	    // Channel 3
		stimConfigBean[3].stimType = StimulationType.EEG_RECORDING;  // EEG Measuring
		stimConfigBean[3].Atdcs = 0;
		stimConfigBean[3].Atacs = 0;
		stimConfigBean[3].Ftacs = 0;
		stimConfigBean[3].Ptacs = 0;
		stimConfigBean[3].Atrns = 0;
		stimConfigBean[3].posIndex = 16;

	    // Channel 4
		stimConfigBean[4].stimType = StimulationType.EEG_RECORDING;  // EEG Measuring
		stimConfigBean[4].Atdcs = 0;
		stimConfigBean[4].Atacs = 0;
	    stimConfigBean[4].Ftacs = 0;
	    stimConfigBean[4].Ptacs = 0;
	    stimConfigBean[4].Atrns = 0;
	    stimConfigBean[4].posIndex = 19;

	    // Channel 5
	    stimConfigBean[5].stimType = StimulationType.EEG_RECORDING;  // EEG Measuring
	    stimConfigBean[5].Atdcs = 0;
	    stimConfigBean[5].Atacs = 0;
	    stimConfigBean[5].Ftacs = 0;
	    stimConfigBean[5].Ptacs = 0;
	    stimConfigBean[5].Atrns = 0;
	    stimConfigBean[5].posIndex = 21;

	    // Channel 6
	    stimConfigBean[6].stimType = StimulationType.EEG_RECORDING;  // EEG Measuring
	    stimConfigBean[6].Atdcs = 0;
	    stimConfigBean[6].Atacs = 0;
	    stimConfigBean[6].Ftacs = 0;
	    stimConfigBean[6].Ptacs = 0;
	    stimConfigBean[6].Atrns = 0;
	    stimConfigBean[6].posIndex = 25;

	    // Channel 7
	    stimConfigBean[7].stimType = StimulationType.EEG_RECORDING;  // EEG Measuring
	    stimConfigBean[7].Atdcs = 0;
	    stimConfigBean[7].Atacs = 0;
	    stimConfigBean[7].Ftacs = 0;
	    stimConfigBean[7].Ptacs = 0;
	    stimConfigBean[7].Atrns = 0;
	    stimConfigBean[7].posIndex = 27;
		
	}


//	/**
//	 * Checks that the registers were properly written
//	 * @return
//	 */
//	public int checkRegWritten(Byte[] reg, int address, int size, int nRetries){
//
//	    int i = 0;
//
//	    // Read register
//	    Byte[] regRead = new Byte[size];
//	    _device.readRegister(DeviceManager.StarStimRegisterFamily.STIM_REGISTERS, (byte) address, regRead, (byte) size);
//
//	    String regReadStr = "regRead: [" + address + "] = ";
//	    for( i = 0; i < size; i ++){
//	        regReadStr += regRead[i] + " ";
//	    }
//	    logger.info(regReadStr, Logger.LOG_FILE_ON);
//
//
//	    // Compare whether retry is necessary
//	    i = 0;
//	    if(Arrays.equals(reg, regRead) == false)
//	    {
//	        logger.info("regName : Read registers FAIL", Logger.LOG_FILE_ON);
//	        i++;
//	        while ( i< nRetries ){ // We make up to 5 retries of writing registers
//
//	            // Write register again
//	            _device.writeRegister(DeviceManager.StarStimRegisterFamily.STIM_REGISTERS, (byte) address, reg, (byte) size);
//
//	            // Read register
//	            _device.readRegister(DeviceManager.StarStimRegisterFamily.STIM_REGISTERS, (byte) address, regRead, (byte) size);
//
//	            // Print regRead
//	            int j = 0;
//	            regReadStr = "regRead: [" +  address + "] = ";
//	            for(j = 0; j < size; j ++){
//	                regReadStr += regRead[j] + " ";
//	            }
//	            logger.info( "Iteration: " + i + " " + regReadStr, Logger.LOG_FILE_ON);
//
//	            if(Arrays.equals(reg, regRead) == false){ break;}
//	            i++;
//	        }
//
////	        stopStimulation();
////	        emit abortCurrentStimulationProtocol();
////	        emit newMessage(5, "Error writing registers.");
//	    }
//
//	    return i;
//
//	}
	
	/**
	 * Converts a double number into a fraction
	 * @param x
	 * @param num
	 * @param den
	 */
	void dec2frac(double x, Reference<Integer> num, Reference<Integer> den)
	{
	  int sign;
	  double  xa;
	  double  z;

	  double  prev_den;
	  double  frac_den;
	  double  frac_num;
	  double  scratch_value;

	  int ind;


	  /* Check sign */
	  sign = (x < 0.0) ? -1 : 1;

	  xa   = Math.abs(x);

	  //printf("xa = %lf   -   floor(xa) = %lf\n", xa, floor(xa));


	  if (xa == Math.floor(xa)) {
	    /* Integer number */
	    num.set((int) (xa*sign) );
	    den.set(1);
	  } else if (xa < 0.00392156862745098) {
	    /* Too low number */
	    num.set( sign );
	    den.set( 255 );
	  } else if (xa > 255.0) {
	    num.set( 255*sign );
	    den.set( 1 );
	  } else {
	    z = xa;

	    prev_den = 0.0;
	    frac_den = 1.0;

	    ind = 0;
	    do {
	      z             = 1.0/(z - Math.floor(z));
	      scratch_value = frac_den;
	      frac_den      = frac_den * Math.floor(z) + prev_den;
	      prev_den      = scratch_value;
	      frac_num      = Math.floor(xa*frac_den + 0.5);

	      ind++;
	    } while ((( Math.abs(xa - (frac_num/frac_den)) > 0.001) && !(z == Math.floor(z))) && (ind < 20));

	    num.set( (int) (frac_num)*sign );
	    den.set( (int) frac_den );
	  }
	}

	
	
	/**
	 * Configures the stimulation with the current template
	 */
	public void initStimulation(){
		long startTime = System.currentTimeMillis();
		
		int errorCount = 0;
	    int numRegistersWritten = 0;
	    int i;
	    int val;
	    boolean isTACSorTRNS = false;
		
//		 // Start the stimulatin of the device
//		 _device.startStimulation();	
//		 if(true) return;
	    
	    // Write STM_REGS_TIME_0_ADDR, STM_REGS_RAMP_UP_0_ADDR, STM_REGS_RAMP_DN_0_ADDR
	    // ----------------------------------------------------------------------------
		Byte[] reg = new Byte[6];
	    reg[0] = (byte)(_stimulationRampUpDuration%256);
	    reg[1] = (byte)(_stimulationRampUpDuration/256);	    	    
	    reg[2] = (byte)(_stimulationRampDownDuration%256);
	    reg[3] = (byte)(_stimulationRampDownDuration/256);
	    reg[4] = (byte)(_stimulationDuration%256);
	    reg[5] = (byte)(_stimulationDuration/256);
	    _device.writeRegister(DeviceManager.StarStimRegisterFamily.STIM_REGISTERS, (byte) DeviceRegisters.STM_REGS_RAMP_UP_0_ADDR, reg, (byte) 6);
	    logger.info("Ramp Up     Duration: " + _stimulationRampUpDuration +  " " + reg[0] + " " + reg[1], Logger.LOG_FILE_ON);
	    logger.info("Ramp Down   Duration: " + _stimulationRampDownDuration +  " " + reg[2] + " " + reg[3], Logger.LOG_FILE_ON);
	    logger.info("Stimulation Duration: " + _stimulationDuration +  " " + reg[4] + " " + reg[5], Logger.LOG_FILE_ON);

	    int retriesPerformed = _device.checkRegWritten(reg, DeviceManager.StarStimRegisterFamily.STIM_REGISTERS, DeviceRegisters.STM_REGS_RAMP_UP_0_ADDR, 6, 5);
	    logger.info("Retries:" + retriesPerformed, Logger.LOG_FILE_ON);
	    
	    
	    if( retriesPerformed < 5 ) logger.info("_stimulationRampUPDuration _stimulationRampDownDuration _stimulationDuration : Read registers OK", Logger.LOG_FILE_ON);
	    else logger.info("_stimulationRampUPDuration _stimulationRampDownDuration _stimulationDuration : Read registers FAIL", Logger.LOG_FILE_ON);
	    errorCount += retriesPerformed;

	    long ellapsed = System.currentTimeMillis() - startTime;
	    logger.info( "Elapsed time " +  ellapsed + " ms" , Logger.LOG_FILE_ON);
	    numRegistersWritten += 3;
		/**/
		
	    long stopTime = System.currentTimeMillis();
	    long elapsedTime = stopTime - startTime;
	    logger.info( "StimulationManager::startStimulation " + elapsedTime + "[ms]", Logger.LOG_FILE_ON);
		

	    
	    // Write Atdcs Register Set (DC WAVEFORM GENERATOR)
	    // ------------------------------------------------
	    logger.info("Write Atdcs Register set (DC WAVEFORM GENERATOR)", Logger.LOG_FILE_ON);
	    Byte[] regAdcs = new Byte[NUM_STIM_CHANNELS*2];
	    for( i = 0; i < NUM_STIM_CHANNELS; i++){

	        // Atdcs Register
	        val=0;
	        if(stimConfigBean[i].stimType == StimulationType.STIMULATION_ELECTRODE) //If it's anodal
	        {
	            logger.info( "Atdcs anodal", Logger.LOG_FILE_ON);
	            val = (int) ( (65536/4)*(double)((stimConfigBean[i].Atdcs/1000.0)) );
	        }
	        if(stimConfigBean[i].stimType == StimulationType.RETURN_ELECTRODE) //If it's cathodal
	        {
	            logger.info( "Atdcs cathodal", Logger.LOG_FILE_ON);

	            //We put the numbers as two's complement 2^n bits-number
	            //We are using 16 bits

	            val = (int) ( (65536/4)*(double)((stimConfigBean[i].Atdcs/1000.0)) );
	            val = 65536-val;
	        }

	        regAdcs[i*2+0] = (byte)(val%256);
	        regAdcs[i*2+1] = (byte)(val/256);

	    }
	    _device.writeRegister(DeviceManager.StarStimRegisterFamily.STIM_REGISTERS, (byte) DeviceRegisters.STM_REGS_CH0_DC_OFF_0_ADDR, regAdcs, (byte) (NUM_STIM_CHANNELS*2));

	    retriesPerformed = _device.checkRegWritten(regAdcs, DeviceManager.StarStimRegisterFamily.STIM_REGISTERS, (byte) DeviceRegisters.STM_REGS_CH0_DC_OFF_0_ADDR, NUM_STIM_CHANNELS*2, 5);
	    if( retriesPerformed < 5 ) logger.info("stimConfig[i].Atdcs : Read registers OK", Logger.LOG_FILE_ON);
	    else logger.info("stimConfig[i].Atdcs : Read registers FAIL", Logger.LOG_FILE_ON);
	    errorCount += retriesPerformed;

	    stopTime = System.currentTimeMillis();
	    elapsedTime = stopTime - startTime;
	    logger.info( "StimulationManager::startStimulation " + elapsedTime + "[ms]", Logger.LOG_FILE_ON);
	    numRegistersWritten += NUM_STIM_CHANNELS*2;
	    

	    
	    // Write Ftacs,Ptacs,Atacs Register set (Sinusoidal Waveform Generator)
	    // --------------------------------------------------------------------

	    logger.info("Write Ftacs,Ptacs,Atacs Register set (Sinusoidal Waveform Generator)", Logger.LOG_FILE_ON);
        Reference<Integer> num = new Reference<Integer>(0);
        Reference<Integer> den = new Reference<Integer>(0);
	    
	    Byte[] regSinusoidal = new Byte[NUM_STIM_CHANNELS*6];
	    for( i = 0; i < NUM_STIM_CHANNELS; i++){


	        // Ftacs Register
	        // FLOOR(Fsin(Hz)*65536/1000)
	        val = (int) Math.floor(stimConfigBean[i].Ftacs*65536/1000+0.5);
	        regSinusoidal[0+i*6] = (byte)(val%256);
	        regSinusoidal[1+i*6] = (byte)(val/256);

	        logger.info("Ftacs:  val" + val + " " + stimConfigBean[i].Ftacs + " " + regSinusoidal[0+i*6]  + " " + regSinusoidal[1+i*6], Logger.LOG_FILE_ON);


	        // Ptacs Register (input in degrees)
	        int originalValue;
	        float valFloat;
	        originalValue = stimConfigBean[i].Ptacs;
	        if(stimConfigBean[i].stimType == StimulationType.RETURN_ELECTRODE) //If it's cathodal
	        {
	            if (originalValue>=180)
	                originalValue-=180;
	            else
	                originalValue+=180;
	        }
	        valFloat = originalValue;
	        valFloat = (float) ((valFloat*Math.PI)/180); //We convert to radians

	        val = (int) Math.floor((valFloat/Math.PI)*32768+0.5);
	        logger.info( "val " + val, Logger.LOG_FILE_ON);
	        regSinusoidal[3+i*6] = (byte) (val%256);
	        regSinusoidal[2+i*6] = (byte)(val/256);

	        logger.info("Ptacs:  " + originalValue + " " + regSinusoidal[2+i*6] + " " + regSinusoidal[3+i*6], Logger.LOG_FILE_ON);


	        // Atacs Register
	        if( stimConfigBean[i].Atacs == 0 )
	        {
	            regSinusoidal[4+i*6]=0;
	            regSinusoidal[5+i*6]=1;
	            logger.info( "Atacs: " + regSinusoidal[4+i*6] + " " + regSinusoidal[5+i*6], Logger.LOG_FILE_ON);

	        }else{
	            isTACSorTRNS=true;
	            val = stimConfigBean[i].Atacs;
	            double aux2 = val/2000.0;

	            // Added dec2frac function from Antonio
	            dec2frac(aux2, num, den);
	            //dec2frac(aux2,&regSinusoidal[4+i*6],&regSinusoidal[5+i*6]);	            
	            regSinusoidal[4+i*6] = (byte) (num.get()&0xFF);
	            regSinusoidal[5+i*6] = (byte) (den.get()&0xFF);

	            logger.info( "Atacs:  " + stimConfigBean[i].Atacs + "dec2frac" + aux2 + ":" + regSinusoidal[4+i*6] + "/" + regSinusoidal[5+i*6], Logger.LOG_FILE_ON);

	        }



//	        // Write Registers
//	        _device.writeRegister(DeviceManager::STIM_REGISTERS, STM_REGS_CH0_SIN_FREQ_0_ADDR+(i)*6, &(regSinusoidal[0+i*6]), 6);

//	        // Check correctness
//	        retriesPerformed = checkRegWritten(&(regSinusoidal[0+i*6]), STM_REGS_CH0_SIN_FREQ_0_ADDR+(i)*6, 6, 5);
//	        if( retriesPerformed < 5 ) loggerMacroDebug("stimConfig[i].Ftacs,Ptacs,Atacs : Read registers OK")
//	        else loggerMacroDebug("stimConfig[i].Ftacs,Ptacs,Atacs : Read registers FAIL")
//	        errorCount += retriesPerformed;

//	        loggerMacroDebug ( "Elapsed time " +  QString::number(myTimer.elapsed()/1000.0) );
//	        numRegistersWritten += 6;


	    }

	    // Write Registers
	    _device.writeRegister(DeviceManager.StarStimRegisterFamily.STIM_REGISTERS, (byte) DeviceRegisters.STM_REGS_CH0_SIN_FREQ_0_ADDR, regSinusoidal, (byte) (6*NUM_STIM_CHANNELS));

	    // Check correctness
	    retriesPerformed = _device.checkRegWritten(regSinusoidal, DeviceManager.StarStimRegisterFamily.STIM_REGISTERS, DeviceRegisters.STM_REGS_CH0_SIN_FREQ_0_ADDR, 6*NUM_STIM_CHANNELS, 5);
	    if( retriesPerformed < 5 ) logger.info("stimConfig[i].Ftacs,Ptacs,Atacs : Read registers OK", Logger.LOG_FILE_ON);
	    else logger.info("stimConfig[i].Ftacs,Ptacs,Atacs : Read registers FAIL", Logger.LOG_FILE_ON);
	    
	    errorCount += retriesPerformed;

	    stopTime = System.currentTimeMillis();
	    elapsedTime = stopTime - startTime;
	    logger.info( "StimulationManager::startStimulation " + elapsedTime + "[ms]", Logger.LOG_FILE_ON);
	    numRegistersWritten += 6*NUM_STIM_CHANNELS;
    
	    
	    // Write Trns (GAUSSIAN WAVEFORM GENERATOR)
	    // --------------------------------------------------------------------
	    Byte[] regGaussian = new Byte[NUM_STIM_CHANNELS*2];
	    for( i = 0; i < NUM_STIM_CHANNELS; i++){

	        // Atrns Register
	       if( stimConfigBean[i].Atrns == 0 )
	       {
	           regGaussian[i*2+0]=0;
	           regGaussian[i*2+1]=1;

	          logger.info("Atrns: " + regGaussian[i*2+0] + " " + regGaussian[i*2+1], Logger.LOG_FILE_ON);

	       }
	       else
	       {
	           isTACSorTRNS=true;
	           val = stimConfigBean[i].Atrns;
	           double aux2 = val/640.0;


	          
			  // Added dec2frac function from Antonio
			  dec2frac(aux2, num, den);
			  //dec2frac(aux2,&regGaussian[i*2+0],&regGaussian[i*2+1]);	            
			  regGaussian[i*2+0] = (byte) (num.get()&0xFF);
			  regGaussian[i*2+1] = (byte) (den.get()&0xFF);
	          
	          
	          //logger.info( "Atrns:  " + stimConfigBean[i].Atrns + " previous: " + 1 + " / " + aux, Logger.LOG_FILE_ON);
	          logger.info( "Atrns:  " + stimConfigBean[i].Atrns + "dec2frac" + aux2 + ":" + regGaussian[i*2+0] + "/" + regGaussian[i*2+1], Logger.LOG_FILE_ON );

	       }


	    }

	    // Write register
	    _device.writeRegister(DeviceManager.StarStimRegisterFamily.STIM_REGISTERS, (byte) DeviceRegisters.STM_REGS_CH0_GAUSS_GAIN_N_ADDR, regGaussian, (byte) (NUM_STIM_CHANNELS*2));

	    retriesPerformed = _device.checkRegWritten(regGaussian, DeviceManager.StarStimRegisterFamily.STIM_REGISTERS, DeviceRegisters.STM_REGS_CH0_GAUSS_GAIN_N_ADDR, NUM_STIM_CHANNELS*2, 5);
	    if( retriesPerformed < 5 ) logger.info("stimConfig[i].Trns : Read registers OK", Logger.LOG_FILE_ON);
	    else logger.info("stimConfig[i].Trns : Read registers FAIL", Logger.LOG_FILE_ON);
	    errorCount += retriesPerformed;

	    stopTime = System.currentTimeMillis();
	    elapsedTime = stopTime - startTime;
	    logger.info( "StimulationManager::startStimulation " + elapsedTime + "[ms]", Logger.LOG_FILE_ON);
	    numRegistersWritten += NUM_STIM_CHANNELS*2;

	    /*
	    // This updates the label in the GUI
	    emit stimulationStateLabel("Writing StarStim Registers "+QString::number(numRegistersWritten*100/numTotalRegistersToWrite)+"%");

	    if (_gui->_isStimulationAborted)
	        return false;
	    /**/

	    
	    // Write CH_INFO and CH_FREE
	    // ------------------------------------------------
	    
	    Byte[] REG_CH_INFO = new Byte[1];
	    REG_CH_INFO[0] = 0x00;
	    Byte[] REG_CH_FREE = new Byte[1];
	    REG_CH_FREE[0] = 0x00;
	    for ( i = 0; i <NUM_STIM_CHANNELS; i++ )
	    {
	    	
	    	String str = "Configuration of the channel: " + i;
	        

	        if( stimConfigBean[i].stimType == StimulationType.STIMULATION_ELECTRODE )
	        {
	            str += " Stimulation Electrode";
	            REG_CH_INFO[0] =  (byte) (( REG_CH_INFO[0] | ( 1<<(i)) ) & 0xFF);
	            //REG_CH_INFO=REG_CH_INFO|(1<<(i));
	        }
	        else if( stimConfigBean[i].stimType == StimulationType.RETURN_ELECTRODE)
	        {
	            str += " Return Electrode";
	            REG_CH_INFO[0] =  (byte) (( REG_CH_INFO[0] | ( 1<<(i)) ) & 0xFF);
	            REG_CH_FREE[0] =  (byte) (( REG_CH_FREE[0] | ( 1<<(i)) ) & 0xFF);
	            
	            //REG_CH_INFO=REG_CH_INFO|(1<<(i));
	            //REG_CH_FREE=REG_CH_FREE|(1<<(i));
	        }
	        else if(stimConfigBean[i].stimType == StimulationType.EEG_RECORDING)
	        {
	            str += " Not used Electrode";
	        }
	        else
	        {
	            str += " EEG Measuring Electrode";
	        }

	        val=0;
	        logger.info( str, Logger.LOG_FILE_ON);
	        
	     } // END: for (int i = 0; i <NUM_STIM_CHANNELS; i++)
	    
	    
	     logger.info("REG_CH_FREE: " + String.format("0x%02X", REG_CH_FREE[0]), Logger.LOG_FILE_ON);
	     _device.writeRegister(DeviceManager.StarStimRegisterFamily.STIM_REGISTERS, (byte) DeviceRegisters.STM_REGS_CH_FREE_ADDR, REG_CH_FREE, (byte) 1);
	     retriesPerformed = _device.checkRegWritten(REG_CH_FREE, DeviceManager.StarStimRegisterFamily.STIM_REGISTERS, DeviceRegisters.STM_REGS_CH_FREE_ADDR, 1, 5);
		 if( retriesPerformed < 5 ) logger.info("REG_CH_FREE : Read registers OK", Logger.LOG_FILE_ON);
		 
		 		 
		 numRegistersWritten++;
		 
	     logger.info("REG_CH_INFO: " + String.format("0x%02X", REG_CH_INFO[0]), Logger.LOG_FILE_ON);
	     _device.writeRegister(DeviceManager.StarStimRegisterFamily.STIM_REGISTERS, (byte) DeviceRegisters.STM_REGS_CH_INFO_ADDR, REG_CH_INFO, (byte) 1);
	     retriesPerformed = _device.checkRegWritten(REG_CH_INFO, DeviceManager.StarStimRegisterFamily.STIM_REGISTERS, DeviceRegisters.STM_REGS_CH_INFO_ADDR, 1, 5);
		 if( retriesPerformed < 5 ) logger.info("REG_CH_INFO : Read registers OK", Logger.LOG_FILE_ON);

		 numRegistersWritten++;
		 
		 stopTime = System.currentTimeMillis();
		 elapsedTime = stopTime - startTime;
		 logger.info( "StimulationManager::startStimulation " + elapsedTime + "[ms]", Logger.LOG_FILE_ON);
		 		 
		 
		 stopTime = System.currentTimeMillis();
		 elapsedTime = stopTime - startTime;
		 logger.info( "StimulationManager::startStimulation Finished Ellapsed time -> " + elapsedTime + "[ms]", Logger.LOG_FILE_ON);
		 
	}
	
	/**
	 * Sends a command to the device to start the stimulation
	 */
	public void startStimulation(){
		 // Start the stimulatin of the device
		 _device.startStimulation();
	}
	
}
