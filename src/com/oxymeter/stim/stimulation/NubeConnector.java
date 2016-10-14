package com.icognos.stim.stimulation;

import java.util.ArrayList;

import android.view.View;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

import com.icognos.stim.R;
import com.icognos.stim.UIElement;
import com.icognos.stim.util.Logger;

public class NubeConnector {

	//  ----------------
	//  -- ATTRIBUTES --
	//  ----------------
	
	// UI logger
	private Logger logger;	
	
	
	//  -------------
	//  -- METHODS --
	//  -------------
	/**
	 * Public Constructor
	 */
	public NubeConnector( ){
        logger = Logger.getInstance();                    
                
	}
	
	/**
	 * Load stimulation parameters
	 */
	public void loadStimParameters(){
		
		// Create ArrayList        
		ArrayList <View> arrayListView = new ArrayList<View>();        
		arrayListView.add(  UIElement.inflater.inflate(R.layout.stim_session, null ) );
		arrayListView.add(  UIElement.inflater.inflate(R.layout.stim_session, null ) );
		arrayListView.add(  UIElement.inflater.inflate(R.layout.stim_session, null ) );
		arrayListView.add(  UIElement.inflater.inflate(R.layout.stim_session, null ) );
		arrayListView.add(  UIElement.inflater.inflate(R.layout.stim_session, null ) );
		arrayListView.add(  UIElement.inflater.inflate(R.layout.stim_session, null ) );
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,  50);
		
		View stimSession;
		TextView stimulationNameTextView, stimulationDateTextView, stimulationStatusTextView;
		
		// Create fake session 0
		stimSession = arrayListView.get(0);
		stimSession.setLayoutParams(params);
		
		stimulationNameTextView  = (TextView) stimSession.findViewById(R.id.StimulationName);
		stimulationNameTextView.setText("Stimulation Anodal tDCS 2000.0mA C3/Fp2");
		
		stimulationDateTextView  = (TextView) stimSession.findViewById(R.id.StimulationDate);
		stimulationDateTextView.setText("23/02/2014");
		
		stimulationStatusTextView = (TextView) stimSession.findViewById(R.id.StimulationStatus);
		stimulationStatusTextView.setText("DONE");
		
		UIElement.sessionsContainer.addView( stimSession );
		
		
		
		// Create fake session 1
		stimSession = arrayListView.get(1);
		stimSession.setLayoutParams(params);
		
		stimulationNameTextView  = (TextView) stimSession.findViewById(R.id.StimulationName);
		stimulationNameTextView.setText("Stimulation Anodal tDCS 200.0mA P3/Pz ");
		
		stimulationDateTextView  = (TextView) stimSession.findViewById(R.id.StimulationDate);
		stimulationDateTextView.setText("04/03/2014");
		
		stimulationStatusTextView = (TextView) stimSession.findViewById(R.id.StimulationStatus);
		stimulationStatusTextView.setText("DONE");
		
		UIElement.sessionsContainer.addView( stimSession );
		
		// Create fake session 2
		stimSession = arrayListView.get(2);
		stimSession.setLayoutParams(params);
		
		stimulationNameTextView  = (TextView) stimSession.findViewById(R.id.StimulationName);
		stimulationNameTextView.setText("Stimulation Anodal tDCS 1000.0mA Fp1/Cz");
		
		stimulationDateTextView  = (TextView) stimSession.findViewById(R.id.StimulationDate);
		stimulationDateTextView.setText("16/03/2014");
		
		stimulationStatusTextView = (TextView) stimSession.findViewById(R.id.StimulationStatus);
		stimulationStatusTextView.setText("PENDING");
		
		UIElement.sessionsContainer.addView( stimSession );
		
		
		// Create fake session 3
		stimSession = arrayListView.get(3);
		stimSession.setLayoutParams(params);
		
		stimulationNameTextView  = (TextView) stimSession.findViewById(R.id.StimulationName);
		stimulationNameTextView.setText("Stimulation Anodal tDCS 2000.0mA O2/Fp1");
		
		stimulationDateTextView  = (TextView) stimSession.findViewById(R.id.StimulationDate);
		stimulationDateTextView.setText("18/03/2014");
		
		stimulationStatusTextView = (TextView) stimSession.findViewById(R.id.StimulationStatus);
		stimulationStatusTextView.setText("PENDING");
		
		UIElement.sessionsContainer.addView( stimSession );
		
		// Create fake session 4
		stimSession = arrayListView.get(4);
		stimSession.setLayoutParams(params);
		
		stimulationNameTextView  = (TextView) stimSession.findViewById(R.id.StimulationName);
		stimulationNameTextView.setText("Stimulation Anodal tACS 1000.0mA 25Hz O1/Fp2");
		
		stimulationDateTextView  = (TextView) stimSession.findViewById(R.id.StimulationDate);
		stimulationDateTextView.setText("22/03/2014");
		
		stimulationStatusTextView = (TextView) stimSession.findViewById(R.id.StimulationStatus);
		stimulationStatusTextView.setText("PENDING");
		
		UIElement.sessionsContainer.addView( stimSession );
		
	}
}
