package com.neuroelectrics.stim;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;


import com.neuroelectrics.stim.util.Logger;

public class ProgressPopUpManager {

	
	//  ----------------
	//  -- ATTRIBUTES --
	//  ----------------
    
	// UI logger
	private Logger logger;
	
	// PopUpWindow
	public PopupWindow popUpWindow;
	
	// Current activity
	public Activity activity;
	
	//  ----------------
	//  -- METHODS    --
	//  ----------------
	
	/**
	 * Public constructor
	 */
	public ProgressPopUpManager(Activity _activity){
		logger = Logger.getInstance();
		
		activity = _activity;
	}
	
	
	/**
	 * Show progress popup
	 */
	public void show(){
		
        UIElement.backgroundForPopup.setVisibility(View.VISIBLE);

        LinearLayout linear = (LinearLayout) UIElement.inflater.inflate(R.layout.progress_popup, null );                
        popUpWindow = new PopupWindow(linear, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        popUpWindow.showAtLocation( UIElement.launchStimButton, Gravity.CENTER, 0, 0);
	
        // Initialise Android Elements
        UIElement.okprogressPopUpButton  = (Button)   popUpWindow.getContentView().findViewById(R.id.okpopupbutton);
        //UIElement.popupProgressEditText  = (TextView) popUpWindow.getContentView().findViewById(R.id.popupProgressText);
	}
	
	/**
	 * Hide progress pop up
	 */
	public void hide(){
		UIElement.backgroundForPopup.setVisibility( View.GONE );
        popUpWindow.dismiss();
	}
	
	/**
	 * enableOkButtonAfterTime
	 */
	public void enableOkButtonAfterTime(int ms_seconds){
		
		// task performed when the timer is ellapsed
		TimerTask timerTask = new TimerTask() {
        	
      	  	@Override
      	  	public void run() {
      	  		enableOkButton();
      	  		      	  		
      	  	}
      	  	
      	};
		
        // Set the timer to simulate a stimulation is taking place
        Timer timer = new Timer();
        timer.schedule( timerTask, ms_seconds );
	
	
	}
	
	
	public void enableOkButton(){
		
		
  	  	activity.runOnUiThread(
				new Thread(new Runnable() {
			      public void run(){	    			    	  
			          UIElement.okprogressPopUpButton.setVisibility(View.VISIBLE);
			          
			      }
			    })
		);
	}
	
	
}
