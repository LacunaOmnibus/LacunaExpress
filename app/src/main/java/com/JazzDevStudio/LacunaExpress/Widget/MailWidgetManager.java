package com.JazzDevStudio.LacunaExpress.Widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.widget.Toast;

import com.JazzDevStudio.LacunaExpress.R;

/**
 * This will configure the widget that is placed on the user's homescreen.
 */
public class MailWidgetManager extends AppWidgetProvider {

	/*
	TempService mservice;
	boolean status_service;
	*/

	//When the app is deleted, this will run, pop up window indicating it has been tossed
	public void onDeleted(Context context, int[] appWidgetIds) {
		super.onDeleted(context, appWidgetIds);
		//This is a mini dialog window that disappears after a short bit of time
		Toast.makeText(context, "Widget removed", Toast.LENGTH_SHORT).show();

		//Funny sound for when widget is removed
		MediaPlayer aww;
		aww = MediaPlayer.create(context, R.raw.aww_sound_effect);
		aww.start();
	}

	//For receiving data
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);

		/*
		Bundle getData =intent.getExtras();
		String received_Sync_Frequency_mail_widget = getData.getString("Sync_Frequency_mail_widget");
		String received_Username_mail_widget = getData.getString("Username_mail_widget");
		String received_Tag_mail_widget = getData.getString("Tag_mail_widget");
		sync_frequency = received_Sync_Frequency_mail_widget;
		username = received_Username_mail_widget;
		tag = received_Tag_mail_widget;

		//Log.d("Checking in MailWidgetManager Passing Vars", sync_frequency);
		//Log.d("Checking in MailWidgetManager Passing Vars", username);
		//Log.d("Checking in MailWidgetManager Passing Vars", tag);
		*/

	}

	/*
		 * When the widget updates (Using service to dictate refresh time) @Params:
		 *1) Context - Package name to refer to intents/ layouts
		 *2) Appwidgetmanager - Refer to for update
		 *3) AppwidgetIDs - Reference multiple IDs (IE in xml)
		 */
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);

		ComponentName thisWidget = new ComponentName(context, MailWidgetManager.class);

		//Checks the number of widgets implemented
		final int N = appWidgetIds.length;

		int[] allWidgetIDs = appWidgetManager.getAppWidgetIds(thisWidget);

		// Build the intent to call the service
		Intent intent = new Intent(context.getApplicationContext(),
				TempService.class);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIDs);

		// Update the widgets via the service
		context.startService(intent);

		/*
		//Loops through all of the widgets one by one
		for (int i = 0; i < N; i++){
			//Get the app ID of the widget bring worked on here
			int app_widget_ID = appWidgetIds[i];

			//Intent intent = new Intent(context, MailWidgetUpdateService.class);
			Intent intent = new Intent(context, TempService.class);
			intent.putExtra(EXTRA_APPWIDGET_ID, app_widget_ID);

			/*
			context.bindService(intent, sc, Context.BIND_AUTO_CREATE);
			status_service = true;
			Log.d("MailWidgetManager", "Service successfully binded");
			*/

			//context.startService(intent); /////////////////////////////////////
			//context.getApplicationContext().bindService(intent, )

			//TESTING FOR NOW

			/*
				//References the widget layout
				RemoteViews v = new RemoteViews(context.getPackageName(), R.layout.widget_mail_layout);
				//Updates the widget
				appWidgetManager.updateAppWidget(app_widget_ID, v);
			*/

		//}////////////////

	}

	/*
	//Service connection
	private ServiceConnection sc = new ServiceConnection() {
		public void onServiceConnected(ComponentName name, IBinder service) {
			TempService.LocalBinder binder = (TempService.LocalBinder) service;
			mservice = binder.getService();
			status_service = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {

		}
	};
	*/
}

