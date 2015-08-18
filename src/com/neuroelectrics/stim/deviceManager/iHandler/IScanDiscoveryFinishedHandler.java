package com.neuroelectrics.stim.deviceManager.iHandler;

import java.util.ArrayList;

public interface IScanDiscoveryFinishedHandler {
	void onScanNeighborhoodFinished(ArrayList<String> mPairedDevicesArrayAdapter, ArrayList<String> mNewDevicesArrayAdapter);
}
