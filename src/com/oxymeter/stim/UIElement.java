package com.icognos.stim;

import com.androidplot.xy.XYPlot;


import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


/**
 * This class provides access to all the elements of the interface
 * @author sergi
 *
 */
public class UIElement {

	// Inflater
	public static LayoutInflater inflater;
	
	// Commands Panel
	public static EditText macAddressText;
	
	// XYPlot - Stimulation progress
	public static XYPlot stimPlot;

	// LinearLayout - SessionContainer
	public static LinearLayout sessionsContainer;

	// RelativeLayout - backgroundForPopup
	public static RelativeLayout backgroundForPopup; 
		
	// Button - launchStimButton
	public static Button launchStimButton;
	
	// PopUp Window
	public static Button   okprogressPopUpButton;
	public static TextView popupProgressEditText;
	
	// View indicating the status of the device
	public static View     device_status;
	
}
