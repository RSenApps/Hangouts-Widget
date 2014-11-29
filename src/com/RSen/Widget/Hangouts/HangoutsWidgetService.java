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



import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

/**
 * This is the service that provides the factory to be bound to the collection service.
 */
public class HangoutsWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new StackRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

/**
 * This is the factory that will provide data to the collection widget.
 */
class StackRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private Context mContext;
    private Cursor mCursor;
    private int mAppWidgetId;

    public StackRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    public void onCreate() {
        // Since we reload the cursor in onDataSetChanged() which gets called immediately after
        // onCreate(), we do nothing here.
    }

    public void onDestroy() {
        if (mCursor != null) {
            mCursor.close();
        }
    }

    public int getCount() {
        return mCursor.getCount();
    }

    public RemoteViews getViewAt(int position) {
        // Get the data for this position from the content provider
        String sender = "Unknown Sender";
        String message = "";
        String time = "NA";
        if (mCursor.moveToPosition(position)) {
            final int senderColIndex = mCursor.getColumnIndex(HangoutsDataProvider.Columns.SENDER);
            final int messageColIndex = mCursor.getColumnIndex(
                    HangoutsDataProvider.Columns.MESSAGE);
            final int timeColIndex = mCursor.getColumnIndex(HangoutsDataProvider.Columns.TIME);

            sender = mCursor.getString(senderColIndex);
            message = mCursor.getString(messageColIndex);
            time = mCursor.getString(timeColIndex);
        }

        // Return a proper item with the proper day and temperature
        //final String formatStr = mContext.getResources().getString(R.string.item_format_string);
         int itemId = R.layout.widget_item;
        if(mContext.getSharedPreferences("hangouts", Context.MODE_MULTI_PROCESS).getBoolean("black"+mAppWidgetId, false))
        {
        	itemId =R.layout.black_widget_item;
        }
        RemoteViews rv = new RemoteViews(mContext.getPackageName(), itemId);
        rv.setTextViewText(R.id.sender, sender);
        rv.setTextViewText(R.id.Message, message);
        rv.setTextViewText(R.id.time, time);
        // Set the click intent so that we can handle it and show a toast message
        final Intent fillInIntent = new Intent();
        final Bundle extras = new Bundle();
        extras.putString(HangoutsWidgetProvider.EXTRA_SENDER_ID, sender);
        fillInIntent.putExtras(extras);
        rv.setOnClickFillInIntent(R.id.layout, fillInIntent);

        return rv;
    }
    public RemoteViews getLoadingView() {
        // We aren't going to return a default loading view in this sample
        return null;
    }

    public int getViewTypeCount() {
        // Technically, we have two types of views (the dark and light background views)
        return 1;
    }

    public long getItemId(int position) {
        return position;
    }

    public boolean hasStableIds() {
        return true;
    }

    public void onDataSetChanged() {
        // Refresh the cursor
        if (mCursor != null) {
            mCursor.close();
        }
        mCursor = mContext.getContentResolver().query(HangoutsDataProvider.CONTENT_URI, null, null,
                null, null);
    }
}
