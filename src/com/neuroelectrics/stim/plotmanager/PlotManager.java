package com.neuroelectrics.stim.plotmanager;

import java.text.DecimalFormat;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

import android.graphics.Color;

import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYStepMode;
import com.neuroelectrics.stim.util.Logger;

public class PlotManager {
	
	/**
	 * This class redraws a plot whenever an update is received 
	 * @author sergi
	 *
	 */
    private class PlotUpdater implements Observer {
    	
        private XYPlot plot;
        private Datasource datasource;
        public PlotUpdater(XYPlot _plot) {
            this.plot = _plot;
        }
        @Override
        public void update(Observable o, Object arg) {
        	// Redraw the graph
        	plot.redraw( );
        	        	
        }               
        
        public void clearPlot(){
        	plot.clear();
        }
        
    }

    
	//  ----------------
	//  -- ATTRIBUTES --
	//  ----------------
    
	// UI logger
	private Logger logger;	
    
	// EEG Plot
    private XYPlot stimPlot;    
    public  StimDatasource stimDataSource;
    private StimDatasource progressStimDatasource;
    private PlotUpdater stimPlotUpdater;
    
    // Controls the current stimulation
    int stimulationProgress;
    int rampUp, duration, rampDown;
    
	/**
	 * Public constructor
	 */
    public PlotManager(XYPlot _stimPlot){
    	
    	logger = Logger.getInstance();

    	
		// Get the plot for EEG and its FFT
        stimPlot = _stimPlot;

        // creates plot updater object handling
        stimPlotUpdater = new PlotUpdater(stimPlot);
               

       
        // Set configuration
        stimulationProgress = 0;
        rampUp = duration = rampDown = 0;
    }
    
    /**
     * Configures the stimulation
     */
    public void configureStimulation(int _rampUp, int _duration, int _rampDown){
    	rampDown = _rampDown;
    	duration = _duration;
    	rampUp   = _rampUp;
    	
    	stimulationProgress = 0;
    
        // Configure plot        
        this.configureStimPlot( rampUp, duration,  rampDown );
    	
    }
    
    /**
     * Set new stimulation progress
     * \param progress number of seconds ellapsed
     */
    public void stimulationProgress( int ellapsedSeconds ){
    	progressStimDatasource.setProgress(ellapsedSeconds);    	
    }
    
    /**
     * Configures both eeg and fft plots
     */
    public void configureStimPlot(int rampUp, int duration, int rampDown){
    	
    	// Clear Stim plot
		stimPlotUpdater.clearPlot();

		// Title for the EEG Plot
		stimPlot.setTitle("Stimulation Progress");
		
        // only display whole numbers in domain labels
		stimPlot.getGraphWidget().setDomainValueFormat(new DecimalFormat("0"));
        
        // Initialise datasource with number of points
		stimDataSource         = new StimDatasource(rampUp, duration, rampDown);
		progressStimDatasource = new StimDatasource(rampUp, duration, rampDown);
		
		// Add the Stimulation contour
        LineAndPointFormatter lineStim = new LineAndPointFormatter(Color.parseColor("#CD6600"), null, null, null) ;
        lineStim.getLinePaint().setStrokeWidth(5);
        stimPlot.addSeries(stimDataSource, lineStim);

		// Add the Stimulation contour
        LineAndPointFormatter progressAreaStim = new LineAndPointFormatter( Color.TRANSPARENT, null, Color.parseColor("#CD6600"), null);    
        progressAreaStim.getFillPaint().setAlpha(100);
        stimPlot.addSeries( progressStimDatasource, progressAreaStim );
        progressStimDatasource.setProgress(stimulationProgress);
        
        // Create a series using a formatter with some transparency applied
        stimPlot.setGridPadding(5, 0, 5, 0);

        // hook up the plotUpdater to the data model:
        stimDataSource.addObserver(stimPlotUpdater);
        progressStimDatasource.addObserver(stimPlotUpdater);




        
        stimPlot.setDomainStepMode(XYStepMode.SUBDIVIDE);
        stimPlot.setDomainStepValue(stimDataSource.size());

        // Set the range Domain (X-values)
        stimPlot.setDomainStepValue(16);
        stimPlot.setTicksPerDomainLabel(5);
        stimPlot.setDomainBoundaries(0, stimDataSource.size() -1, BoundaryMode.FIXED);
        
        
        // Sets grid and ticks for Range (Y-values)
        stimPlot.setRangeStepValue(10);        
        stimPlot.setTicksPerRangeLabel(3);
        stimPlot.setRangeBoundaries(0, 2, BoundaryMode.FIXED);         // Set the range boundaries (Y values)                        

        // Remove ticks
        stimPlot.getGraphWidget().getRangeLabelPaint().setColor(Color.TRANSPARENT);
        stimPlot.getGraphWidget().getRangeOriginLabelPaint().setColor(Color.TRANSPARENT);
        
        // Remove legend
        stimPlot.getLayoutManager().remove(stimPlot.getLegendWidget());
        stimPlot.getLayoutManager().remove(stimPlot.getDomainLabelWidget());

        // Leave some space for the axis to fit in
        stimPlot.getGraphWidget().setMarginRight(10);
        stimPlot.getGraphWidget().setMarginTop(10);
        
        // Changes the color
        // -------------------
        
        // This gets rid of the black border (up to the graph) there is no black border around the labels
        stimPlot.getBackgroundPaint().setColor(Color.TRANSPARENT);

        // This gets rid of the black behind the graph
        stimPlot.getGraphWidget().getBackgroundPaint().setColor(Color.TRANSPARENT);
                
        // Remove border
        stimPlot.getBorderPaint().setColor(Color.TRANSPARENT);
    	

        stimPlot.redraw();
        
        
                        
    }
    

	/**
	 * Starts plotting procedure for EEG
	 */
	public void start(){
        		

		
        // kick off the data generating thread:
        new Thread(stimDataSource).start();
	}
    
	
	
	/**
	 * Stops the plot
	 */
	public void stop(){
		
		// Terminates the plot thread
		stimDataSource.terminateThread();
		
	}
	

}
