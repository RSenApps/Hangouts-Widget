package com.RSen.Widget.Hangouts;
/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.TransactionTooLargeException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.Toast;

/**
 * Our data observer just notifies an update for all weather widgets when it detects a change.
 */
class HangoutsDataProviderObserver extends ContentObserver {
    private AppWidgetManager mAppWidgetManager;
    private ComponentName mComponentName;

    HangoutsDataProviderObserver(AppWidgetManager mgr, ComponentName cn, Handler h) {
        super(h);
        mAppWidgetManager = mgr;
        mComponentName = cn;
    }

    @Override
    public void onChange(boolean selfChange) {
        // The data has changed, so notify the widget that the collection view needs to be updated.
        // In response, the factory's onDataSetChanged() will be called which will requery the
        // cursor for the new data.
        mAppWidgetManager.notifyAppWidgetViewDataChanged(
                mAppWidgetManager.getAppWidgetIds(mComponentName), R.id.message_list);
    }
}

/**
 * The weather widget's AppWidgetProvider.
 */
public class HangoutsWidgetProvider extends AppWidgetProvider {
    public static String OPEN_ACTION = "com.RSen.Widget.Hangouts.OPEN";
    public static String ADD_ACTION = "com.RSen.Widget.Hangouts.ADD";
    public static String EXTRA_SENDER_ID = "com.RSen.Widget.Hangouts.sender";

    private static HandlerThread sWorkerThread;
    private static Handler sWorkerQueue;
    private static HangoutsDataProviderObserver sDataObserver;

    public HangoutsWidgetProvider() {
        // Start the worker thread
        sWorkerThread = new HandlerThread("HangoutsWidgetProvider-worker");
        sWorkerThread.start();
        sWorkerQueue = new Handler(sWorkerThread.getLooper());
    }

    // XXX: clear the worker queue if we are destroyed?
 
    @Override
    public void onEnabled(Context context) {
        // Register for external updates to the data to trigger an update of the widget.  When using
        // content providers, the data is often updated via a background service, or in response to
        // user interaction in the main app.  To ensure that the widget always reflects the current
        // state of the data, we must listen for changes and update ourselves accordingly.
    	
        final ContentResolver r = context.getContentResolver();
        if (sDataObserver == null) {
            final AppWidgetManager mgr = AppWidgetManager.getInstance(context);
            final ComponentName cn = new ComponentName(context, HangoutsWidgetProvider.class);
            sDataObserver = new HangoutsDataProviderObserver(mgr, cn, sWorkerQueue);
            r.registerContentObserver(HangoutsDataProvider.CONTENT_URI, true, sDataObserver);
        }
        super.onEnabled(context);
    }

    @Override
    public void onReceive(Context ctx, Intent intent) {
        final String action = intent.getAction();
        KeyguardManager kM = (KeyguardManager)ctx.getSystemService(Context.KEYGUARD_SERVICE);
        if (action.equals(ADD_ACTION)) {
        	Intent i = new Intent(Intent.ACTION_SEND);
        	i.setPackage("com.google.android.talk");
        	i.setType("text/plain");
        	i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        	ctx.startActivity(i);
        	if(kM.isKeyguardLocked())
        	{
	           Intent o = new Intent(ctx, BlankActivity.class);
	           o.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
	           ctx.startActivity(o);
        	}
        	
        }
        else if (action.equals(OPEN_ACTION))
        {
        	Intent i = new Intent(Intent.ACTION_MAIN);
        	i.addCategory(Intent.CATEGORY_LAUNCHER);
        	
        	i.setPackage("com.google.android.talk");
        	i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        	ctx.startActivity(i);
        	if(kM.isKeyguardLocked())
        	{
	        	Intent o = new Intent(ctx, BlankActivity.class);
	        	o.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
	            ctx.startActivity(o);
        	}
        }
     

        super.onReceive(ctx, intent);
    }

    public static RemoteViews buildLayout(Context context, int appWidgetId) {
    	
        RemoteViews rv;
        
            // Specify the service to provide data for the collection widget.  Note that we need to
            // embed the appWidgetId via the data otherwise it will be ignored.
            final Intent intent = new Intent(context, HangoutsWidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            if(context.getSharedPreferences("hangouts", Context.MODE_MULTI_PROCESS).getBoolean("black"+appWidgetId, false))
            {
            	rv = new RemoteViews(context.getPackageName(), R.layout.black_widget_layout);

            }
            else
            {
            	rv = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            }
            rv.setRemoteAdapter(R.id.message_list, intent);
            rv.setEmptyView(R.id.message_list, R.id.empty_view);
            int transparencyPercentage = context.getSharedPreferences("hangouts", Context.MODE_MULTI_PROCESS).getInt("transparency"+appWidgetId, 80);
            int transparency = 255 * transparencyPercentage/100;
            Log.d("transparency", transparency + "");
            rv.setInt(R.id.background, "setAlpha", transparency);

            // Set the empty view to be displayed if the collection is empty.  It must be a sibling
            // view of the collection view.
            

            // Bind a click listener template for the contents of the weather list.  Note that we
            // need to update the intent's data if we set an extra, since the extras will be
            // ignored otherwise.
            final Intent openIntent = new Intent(context, HangoutsWidgetProvider.class);
            openIntent.setAction(HangoutsWidgetProvider.OPEN_ACTION);
            final PendingIntent openPendingIntent = PendingIntent.getBroadcast(context, 0,
                    openIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setPendingIntentTemplate(R.id.message_list, openPendingIntent);

            // Bind the click intent for the refresh button on the widget
            final Intent addIntent = new Intent(context, HangoutsWidgetProvider.class);
            addIntent.setAction(HangoutsWidgetProvider.ADD_ACTION);
            final PendingIntent addPendingIntent = PendingIntent.getBroadcast(context, 0,
                    addIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setOnClickPendingIntent(R.id.add, addPendingIntent);

           
            rv.setOnClickPendingIntent(R.id.icon, openPendingIntent);
            // Restore the minimal header
            //rv.setTextViewText(R.id.city_name, context.getString(R.string.city_name));
      
        return rv;
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Update each of the widgets with the remote adapter
    	updateAppWidget(context, appWidgetManager, appWidgetIds);
       
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }
    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
    	 for (int i = 0; i < appWidgetIds.length; ++i) {
             RemoteViews layout = buildLayout(context, appWidgetIds[i]);
             appWidgetManager.updateAppWidget(appWidgetIds[i], layout);
         }
    }
  

}