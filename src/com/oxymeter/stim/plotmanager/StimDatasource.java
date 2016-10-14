package com.icognos.stim.plotmanager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Observable;
import java.util.Observer;

import org.apache.commons.math3.stat.regression.SimpleRegression;

import com.androidplot.xy.XYSeries;
import com.icognos.stim.util.Logger;

public class StimDatasource implements Runnable, XYSeries, Datasource{

    // encapsulates management of the observers watching this datasource for update events:
    class MyObservable extends Observable {
	    public void notifyObservers() {
	        setChanged();
	        super.notifyObservers();
	    }
    }

	//  ----------------
	//  -- ATTRIBUTES --
	//  ----------------
 
    private Logger logger;

    private MyObservable notifier;

    // Data Containers
    private ArrayList<Number> sampleArray;
    private Integer rampUp, duration, rampDown;
    private Integer totalStim;
    
    // Controls
    double ellapsedSeconds;
    
    // Thread control
    boolean terminate;
    
	//  -------------
	//  -- METHODS --
	//  -------------
    
    public StimDatasource(Integer _rampUp, Integer _duration, Integer _rampDown){
    	logger = Logger.getInstance();
    	
    	// instantiate objects
    	notifier       = new MyObservable();    	
    	sampleArray    = new ArrayList<Number>();
    	
    	// Configure Stimulation
    	rampUp = _rampUp; rampDown = _rampDown;
    	duration = _duration;

    	// Generate array
    	totalStim = rampUp + duration + rampDown +1;    	    	
    	for( int i = 0; i < totalStim; i++ ){

    		// RampUp case
    		if( i < rampUp ){
    			sampleArray.add( (double) i / rampUp );
    			continue;
    		}
    		
    		// RampDown case
    		if( i >= (duration + rampUp ) ){
    			int removeOffset = i - (duration + rampUp );
    			sampleArray.add( ( 1-( (double) removeOffset / rampDown)) );    	
    			continue;
    		}
    		
    		// 1 Otherwise
    		sampleArray.add( 1 );
    	}

    	
    	// Indicates terminte the current thread
    	terminate = false;
    	
//    	// this is what we store
//    	for( int i = 0; i < totalStim; i++ ){
//    		logger.info("Value : " + sampleArray.get(i).doubleValue(), Logger.LOG_FILE_ON);
//    	}
    	
    	// Current progress
    	ellapsedSeconds = totalStim;
    	
    }
    
    //@Override
    public void run() {

    	logger.info( "Initialise Plot thread (EEG)", Logger.LOG_FILE_ON);
    	
    	try{
	    	while( !terminate ){
	            Thread.sleep(1000); // decrease or remove to speed up the refresh rate.
	            notifier.notifyObservers();
	    	}
	    } catch (InterruptedException e) {
	        e.printStackTrace();
	    }


    	logger.info( "Terminate Plot thread", Logger.LOG_FILE_ON);
    	
    }

    public void addObserver(Observer observer) {
        notifier.addObserver(observer);
    }

    public void removeObserver(Observer observer) {
        notifier.deleteObserver(observer);
    }

    
      
	@Override
	public String getTitle() {
		return "Stimulation Progress";
	}

	@Override
	public Number getX(int index) {
		return index;
	}

	@Override
	public Number getY(int index) {		
		return sampleArray.get(index);
	}

	@Override
	public int size() {		
		return (int) ellapsedSeconds;
	}

	/**
	 * Terminates the current thread
	 */
	public void terminateThread(){
		terminate = true;
	}

	@Override
	public int getMax() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getMin() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isDataReady() {
		// TODO Auto-generated method stub
		return false;
	}
		
	/**
	 * Sets the progress of the stimulation
	 * @return
	 */
	public void setProgress( int _ellapsedSeconds ){
		if( _ellapsedSeconds > totalStim ) ellapsedSeconds = totalStim;
		else ellapsedSeconds = _ellapsedSeconds;		
		
		// Repainting
		if( ellapsedSeconds <= totalStim ) notifier.notifyObservers();
	}
	
}
