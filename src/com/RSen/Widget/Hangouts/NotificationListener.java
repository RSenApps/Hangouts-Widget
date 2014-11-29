package com.RSen.Widget.Hangouts;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.content.ContentValues;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

public class NotificationListener extends NotificationListenerService {
	public NotificationListener() {
	}

	

	@Override
	public void onNotificationPosted(StatusBarNotification arg0) {
		if(!arg0.getPackageName().equals("com.google.android.talk"))
		{
			return;
		}
		String ticker = (String) arg0.getNotification().tickerText;
		String senderOrNewMessageCount = ticker.split(": ")[0];
		if(senderOrNewMessageCount.endsWith("new messages"))
		{
			//multiple senders
			String senders = ticker.split(": ")[1];
			String[] sendersArray = senders.split(", ");
			for (String sender : sendersArray)
			{
				ContentValues values = new ContentValues();
				values.put("sender", sender);
				values.put("message", "New Message");
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(arg0.getPostTime());
				SimpleDateFormat format = new SimpleDateFormat("EEE h:mm a");
				values.put("time", format.format(cal.getTime()));
			
				getContentResolver().insert(HangoutsDataProvider.CONTENT_URI, values);
			}
		}
		else
		{
			//1 sender
			ContentValues values = new ContentValues();
			values.put("sender", senderOrNewMessageCount);
			values.put("message", ticker.split(": ")[1]);
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(arg0.getPostTime());
			SimpleDateFormat format = new SimpleDateFormat("EEE h:mm a");
			values.put("time", format.format(cal.getTime()));
		
			getContentResolver().insert(HangoutsDataProvider.CONTENT_URI, values);
		}
		
	}

	@Override
	public void onNotificationRemoved(StatusBarNotification arg0) {
		// TODO Auto-generated method stub
		
	}
}
