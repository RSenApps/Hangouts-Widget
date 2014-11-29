package com.RSen.Widget.Hangouts;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;

public class MainActivity extends Activity {
	int widgetID = AppWidgetManager.INVALID_APPWIDGET_ID;
@Override
protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	 setContentView(R.layout.main);
	 Intent intent = getIntent();
     Bundle extras = intent.getExtras();
     
     if (extras != null)
        {
        widgetID = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, 
                                     AppWidgetManager.INVALID_APPWIDGET_ID);
        }
     
     //No valid ID, so bail out.
     if (widgetID == AppWidgetManager.INVALID_APPWIDGET_ID)
         finish();
    
     //Return the original widget ID, found in onCreate().
     Intent resultValue = new Intent();
     resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
     setResult(RESULT_OK, resultValue);
}
@Override
	protected void onPause() {
		super.onPause();
		
	   getSharedPreferences("hangouts", MODE_MULTI_PROCESS).edit().putInt("transparency"+widgetID, ((SeekBar) findViewById(R.id.transparency)).getProgress()).commit();
	   getSharedPreferences("hangouts", MODE_MULTI_PROCESS).edit().putBoolean("black" + widgetID, ((Switch) findViewById(R.id.dark)).isChecked()).commit();
	   AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());

	// This is equivalent to your ChecksWidgetProvider.updateAppWidget()    
	appWidgetManager.updateAppWidget(widgetID,
	                                 HangoutsWidgetProvider.buildLayout(getApplicationContext(),
	                                                                       widgetID));

	// Updates the collection view, not necessary the first time
	appWidgetManager.notifyAppWidgetViewDataChanged(widgetID, R.id.message_list);
			Toast.makeText(this, "Make sure that notification listening is enabled...",Toast.LENGTH_LONG).show();
		    this.startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
		    
		    
		      finish();
	}

}
