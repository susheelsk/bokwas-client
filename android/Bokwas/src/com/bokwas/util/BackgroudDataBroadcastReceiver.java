package com.bokwas.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BackgroudDataBroadcastReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent dailyUpdater = new Intent(context, BackgroundDataService.class);
	    context.startService(dailyUpdater);
	}

}
