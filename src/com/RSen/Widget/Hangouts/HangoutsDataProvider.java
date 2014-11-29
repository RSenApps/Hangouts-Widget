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

package com.RSen.Widget.Hangouts;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

/**
 * A dummy class that we are going to use internally to store weather data.  Generally, this data
 * will be stored in an external and persistent location (ie. File, Database, SharedPreferences) so
 * that the data can persist if the process is ever killed.  For simplicity, in this sample the
 * data will only be stored in memory.
 */
class HangoutsDataPoint implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String sender;
    public String getSender() {
		return sender;
	}
	public void setSender(String sender) {
		this.sender = sender;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	private String message;
    private String time;
    HangoutsDataPoint(String s, String m, String t) {
        sender = s;
        message = m;
        time = t;
    }
}

/**
 * The AppWidgetProvider for our sample weather widget.
 */
public class HangoutsDataProvider extends ContentProvider {
    public static final Uri CONTENT_URI =
        Uri.parse("content://com.RSen.Widget.Hangouts.provider");
    public static class Columns {
        public static final String ID = "_id";
        public static final String SENDER = "sender";
        public static final String MESSAGE = "message";
        public static final String TIME = "time";
    }
    private static final String FILENAME = "messages";
    /**
     * Generally, this data will be stored in an external and persistent location (ie. File,
     * Database, SharedPreferences) so that the data can persist if the process is ever killed.
     * For simplicity, in this sample the data will only be stored in memory.
     */
    private static ArrayList<HangoutsDataPoint> sData = new ArrayList<HangoutsDataPoint>();

    @Override
    public boolean onCreate() {
        // We are going to initialize the data provider with some default values
    	loadMessages();
    	
       sData.add(new HangoutsDataPoint("Angelina Sparkovina", "Hi I'm back from vacation, we should catchup soon", "Sun 5:23PM"));
       sData.add(new HangoutsDataPoint("Hannah Cho", "Hi! You should go see this new store in lower east side. You would love it!", "Sun 5:21PM"));
       sData.add(new HangoutsDataPoint("(555) 555-5555", "Notification: Your package has arrived!", "Sat 5:14PM"));
       sData.add(new HangoutsDataPoint("Bob", "LOL", "Sat 5:12PM"));

       return true;
    }
    @Override
    public synchronized Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        assert(uri.getPathSegments().isEmpty());

        // In this sample, we only query without any parameters, so we can just return a cursor to
        // all the weather data.
        final MatrixCursor c = new MatrixCursor(
                new String[]{ Columns.ID, Columns.SENDER, Columns.MESSAGE, Columns.TIME });
        for (int i = 0; i < sData.size(); ++i) {
            final HangoutsDataPoint data = sData.get(i);
            c.addRow(new Object[]{ Integer.valueOf(i), data.getSender(), data.getMessage(), data.getTime()});
        }
        return c;
    }

    @Override
    public String getType(Uri uri) {
        return "vnd.android.cursor.dir/vnd.hangoutswidget.conversation";
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
    	
        final HangoutsDataPoint data = new HangoutsDataPoint(values.getAsString(Columns.SENDER), values.getAsString(Columns.MESSAGE), values.getAsString(Columns.TIME));
       ArrayList<HangoutsDataPoint> toRemove = new ArrayList<HangoutsDataPoint>();
       for (HangoutsDataPoint d : sData)
       {
    	   if(d.getSender().equals(values.getAsString(Columns.SENDER)))
    	   {
    		   toRemove.add(d);
    	   }
       }
       for(HangoutsDataPoint d : toRemove)
       {
    	   sData.remove(d);
       }
       sData.add(0, data);

       saveMessages();  
       getContext().getContentResolver().notifyChange(uri, null);
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // This example code does not support deleting
        return 0;
    }

    @Override
    public synchronized int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
    	return 0;
        
    }
    private void saveMessages()
    {
    	
    	try
    	{
	    	FileOutputStream fos = getContext().openFileOutput(FILENAME, Context.MODE_PRIVATE);
	    	ObjectOutputStream os = new ObjectOutputStream(fos);
	    	os.writeObject(sData);
	    	os.close();
    	}
    	catch(Exception e) {
    		e.printStackTrace();
    	}
    }
    private void loadMessages()
    {
    	FileInputStream fis;
		try {
			fis = getContext().openFileInput(FILENAME);
			ObjectInputStream is = new ObjectInputStream(fis);
	    	sData = (ArrayList<HangoutsDataPoint>) is.readObject();
	    	is.close();
		} catch (Exception e) {
    		e.printStackTrace();

		}
    	
    	
    }

}
