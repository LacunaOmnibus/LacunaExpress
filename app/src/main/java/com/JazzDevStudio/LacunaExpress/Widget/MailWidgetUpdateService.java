package com.JazzDevStudio.LacunaExpress.Widget;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import com.JazzDevStudio.LacunaExpress.AccountMan.AccountInfo;
import com.JazzDevStudio.LacunaExpress.AccountMan.AccountMan;
import com.JazzDevStudio.LacunaExpress.Database.TEMPDatabaseAdapter;
import com.JazzDevStudio.LacunaExpress.JavaLeWrapper.Inbox;
import com.JazzDevStudio.LacunaExpress.LEWrapperResponse.Response;
import com.JazzDevStudio.LacunaExpress.MISCClasses.Notifications;
import com.JazzDevStudio.LacunaExpress.R;
import com.JazzDevStudio.LacunaExpress.SelectMessageActivity2;
import com.JazzDevStudio.LacunaExpress.Server.AsyncServer;
import com.JazzDevStudio.LacunaExpress.Server.ServerRequest;
import com.JazzDevStudio.LacunaExpress.Server.serverFinishedListener;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * Useful information on this class setup @: http://programmerbruce.blogspot.com/2011/04/simple-complete-app-widget-part-1.html
 */
//This class manages the service for the Mail widget updating
public class MailWidgetUpdateService extends Service implements serverFinishedListener {
	private static final String LOG = "com.JazzDevStudio.LacunaExpress.Widget.TempService";


	//Account info
	AccountInfo selectedAccount;
	//For storing all account files
	ArrayList<AccountInfo> accounts;
	//ArrayList of display strings for the spinner
	ArrayList<String> user_accounts = new ArrayList<String>();

	//Messages Info
	ArrayList<Response.Messages> messages_array = new ArrayList<Response.Messages>();
	Boolean messagesReceived = false;
	private String tag_chosen = "All";

	private int awid;

	private String message_count_string, message_count_int, sync_frequency,
			notifications_option, message_count_received;

	//StatusBar and Notification Manager Stuff
	NotificationManager notificationManager;
	public static final int uniqueID = 8675309;

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		Log.i(LOG, "Called");


		//Begin pulling data:
		//This block populates user_accounts for values to display in the select account spinner
		//In case there is nothing in the file, this will error out
		try {
			ReadInAccounts();
			if (accounts.size() == 1) {
				selectedAccount = accounts.get(0);
				Log.d("SelectMessage.Initialize", "only 1 account setting as default" + selectedAccount.displayString);
				user_accounts.add(selectedAccount.displayString);
			} else {
				for (AccountInfo i : accounts) {
					Log.d("SelectMessage.Initialize", "Multiple accounts found, Setting Default account to selected account: " + i.displayString); //
					user_accounts.add(i.displayString);
				}
			}

		} catch (NullPointerException e){
			e.printStackTrace();
		}

		Log.d(LOG, "OnStart Called");
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this
				.getApplicationContext());

		int[] allWidgetIds = intent
				.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);

		ComponentName thisWidget = new ComponentName(getApplicationContext(),
				MailWidgetProvider.class);
		int[] allWidgetIds2 = appWidgetManager.getAppWidgetIds(thisWidget);
		Log.w(LOG, "From Intent " + String.valueOf(allWidgetIds.length));
		Log.w(LOG, "Direct " + String.valueOf(allWidgetIds2.length));

		int counter = 1;
		//All widget IDs are passed through this for loop. Run calculations / set text fields here
		for (int widgetId : allWidgetIds) {

			awid = widgetId;

			String str = Integer.toString(widgetId);

			RemoteViews remoteViews = new RemoteViews(this
					.getApplicationContext().getPackageName(),
					R.layout.widget_mail_layout);

			//Create a database object and set the values here
			TEMPDatabaseAdapter db = new TEMPDatabaseAdapter(this);

			//For the row ID
			String widget_id = Integer.toString(awid);

			//Extract the return data from the List and use it//

			//List to hold returned data
			List<String> db_data = new ArrayList<>();

			//Set the returned data = to the row's returned data
			db_data = db.getRow(widget_id);

			String user_name, color_background_choice, font_color_choice;

			user_name = "Loading...";
			tag_chosen = "All";
			color_background_choice = "white";
			font_color_choice = "black";
			message_count_int = "-1";
			message_count_string = "-1";
			sync_frequency = "15";

			// Register an onClickListener
			Intent clickIntent = new Intent(this.getApplicationContext(),
					SelectMessageActivity2.class);

			try {
				sync_frequency = db_data.get(1);
				user_name = db_data.get(2);
				Log.d("MailWidgetUpdateService Database username = ", user_name);
				tag_chosen = db_data.get(19);
				Log.d("MailWidgetUpdateService Database tag chosen = ", tag_chosen);
				color_background_choice = db_data.get(20);
				Log.d("MailWidgetUpdateService Database color background choice = ", color_background_choice);
				font_color_choice = db_data.get(21);
				Log.d("MailWidgetUpdateService Database font color choice = ", font_color_choice);
				message_count_int = db_data.get(3);
				Log.d("MailWidgetUpdateService Database message count int = ", message_count_int);

				//Determine which tag chosen parameter was passed and return the mail respective to that call
				if (tag_chosen.equalsIgnoreCase("All")) {
					message_count_string = db_data.get(4);
				} else if (tag_chosen.equalsIgnoreCase("Correspondence")) {
					message_count_string = db_data.get(4);
				} else if (tag_chosen.equalsIgnoreCase("Tutorial")) {
					message_count_string = db_data.get(5);
				} else if (tag_chosen.equalsIgnoreCase("Medal")) {
					message_count_string = db_data.get(6);
				} else if (tag_chosen.equalsIgnoreCase("Intelligence")) {
					message_count_string = db_data.get(7);
				} else if (tag_chosen.equalsIgnoreCase("Alert")) {
					message_count_string = db_data.get(8);
				} else if (tag_chosen.equalsIgnoreCase("Attack")) {
					message_count_string = db_data.get(9);
				} else if (tag_chosen.equalsIgnoreCase("Colonization")) {
					message_count_string = db_data.get(10);
				} else if (tag_chosen.equalsIgnoreCase("Complaint")) {
					message_count_string = db_data.get(11);
				} else if (tag_chosen.equalsIgnoreCase("Excavator")) {
					message_count_string = db_data.get(12);
				} else if (tag_chosen.equalsIgnoreCase("Mission")) {
					message_count_string = db_data.get(13);
				} else if (tag_chosen.equalsIgnoreCase("Parliament")) {
					message_count_string = db_data.get(14);
				} else if (tag_chosen.equalsIgnoreCase("Probe")) {
					message_count_string = db_data.get(15);
				} else if (tag_chosen.equalsIgnoreCase("Spies")) {
					message_count_string = db_data.get(16);
				} else if (tag_chosen.equalsIgnoreCase("Trade")) {
					message_count_string = db_data.get(17);
				} else if (tag_chosen.equalsIgnoreCase("Fissure")) {
					message_count_string = db_data.get(18);
				}
				Log.d("MailWidgetUpdateService Database message count string = ", message_count_string);

				notifications_option = db_data.get(24);

				message_count_received = db_data.get(25);

				Log.d("Database", "Has been queried");

				//contentValues.put(helper.COLUMN_SESSION_ID,  newData.get(22)); //May need...

				if (user_name.equalsIgnoreCase("Loading...")){
					//Do nothing as it is useless to pass loading...
				} else {
					//Add these 2 so that when clicked on, it will open up to that specific mail user and tag
					clickIntent.putExtra("chosen_account_string", user_name);
					Log.d("Service :", "Username passed via clickintent = " + user_name);
					clickIntent.putExtra("tag_chosen", tag_chosen);
				}

			} catch (Exception e){
				Log.d("Database", "Has NOT been queried");
			}

			selectedAccount = AccountMan.GetAccount(user_name);

			try {

				//Depending on tag chosen, different URI request sent in JSON
				if (tag_chosen.equalsIgnoreCase("All")){
					Log.d("SelectMessage.onItemSelected", "Second Spinner Tag All Calling View Inbox");
					String request = Inbox.ViewInbox(selectedAccount.sessionID);
					Log.d("SelectMessage.OnSelectedItem Request to server", request);
					Log.d("Select Message Activity, SelectedAccount", user_name);
					ServerRequest sRequest = new ServerRequest(selectedAccount.server, Inbox.url, request);
					AsyncServer s = new AsyncServer();

					UpdateRemoteViewsViaAsync s1 = new UpdateRemoteViewsViaAsync(MailWidgetUpdateService.this,
							remoteViews, widgetId, tag_chosen, appWidgetManager, user_name, color_background_choice,
							font_color_choice, sync_frequency, notifications_option, message_count_received);

					s1.addListener(this);
					s1.execute(sRequest);

					try {
						//Log.d("Waiting 5 Seconds", ".");
						//wait(1000 * 5);
						//Log.d("Done Waiting 5 Seconds", ".");
					} catch (Exception e){
						e.printStackTrace();
					}
				} else {
					Log.d("SelectMessage.onItemSelected", "Second Spinner word in spinner All Calling View Inbox");
					String request = Inbox.ViewInbox(selectedAccount.sessionID, tag_chosen);
					Log.d("SelectMessage.OnSelectedItem Request to server", request);
					Log.d("Select Message Activity, SelectedAccount", user_name);
					ServerRequest sRequest = new ServerRequest(selectedAccount.server, Inbox.url, request);
					AsyncServer s = new AsyncServer();

					UpdateRemoteViewsViaAsync s1 = new UpdateRemoteViewsViaAsync(MailWidgetUpdateService.this,
							remoteViews, widgetId, tag_chosen, appWidgetManager, user_name, color_background_choice,
							font_color_choice, sync_frequency, notifications_option, message_count_received);

					s1.addListener(this);
					s1.execute(sRequest);

					try {
						Log.d("Waiting 5 Seconds", ".");
						wait(1000*5);
						Log.d("Done Waiting 5 Seconds", ".");
					} catch (InterruptedException e){
						e.printStackTrace();
					}
				}

				Log.d("Widget ID in Service: ", Integer.toString(widgetId));
				Log.d("Counter is at: ", Integer.toString(counter));
				counter++;

			} catch (Exception e){
				e.printStackTrace();
			}

			//Set the Tag choice
			String tag_chosen_v1 = "Tag Chosen:\n" + tag_chosen;
			remoteViews.setTextViewText(R.id.widget_mail_tag_choice, tag_chosen_v1);

			Log.d("Background choice is: ", color_background_choice);
			Log.d("Font color is: ", font_color_choice);

			//Set the background color of the widget
			remoteViews.setInt(R.id.widget_mail_layout, "setBackgroundColor", android.graphics.Color.parseColor(color_background_choice));

			//Set the font color of the widget text
			remoteViews.setInt(R.id.widget_mail_username, "setTextColor", android.graphics.Color.parseColor(font_color_choice));
			remoteViews.setInt(R.id.widget_mail_message_count, "setTextColor", android.graphics.Color.parseColor(font_color_choice));
			remoteViews.setInt(R.id.widget_mail_tag_choice, "setTextColor", android.graphics.Color.parseColor(font_color_choice));

			remoteViews.setFloat(R.id.widget_mail_tag_choice, "setTextSize", 10);

			int messages_with_tag = -1;

			try {
				if (tag_chosen.equalsIgnoreCase("All")) {
					Log.d("Message count string is at:", message_count_int);
					remoteViews.setTextViewText(R.id.widget_mail_message_count, message_count_int);
					messages_with_tag = Integer.parseInt(message_count_int);
				} else {
					Log.d("Message count string is at:", message_count_string);
					remoteViews.setTextViewText(R.id.widget_mail_message_count, message_count_string);
					messages_with_tag = Integer.parseInt(message_count_string);
				}

			} catch (NullPointerException e){
				e.printStackTrace();
			}
			//Set the remote views dependent upon new data received
			//Check the number of messages and adjust the font size of the number of messages displayed. Prevents out of bounds on screen
			Log.d("Num messages", Integer.toString(messages_with_tag));
			if (messages_with_tag < 10){
				remoteViews.setFloat(R.id.widget_mail_message_count, "setTextSize", 32);
			} else if (messages_with_tag >=10 && messages_with_tag <100){
				remoteViews.setFloat(R.id.widget_mail_message_count, "setTextSize", 28);
			} else if (messages_with_tag >= 100 && messages_with_tag <999){
				remoteViews.setFloat(R.id.widget_mail_message_count, "setTextSize", 24);
			} else {
				remoteViews.setFloat(R.id.widget_mail_message_count, "setTextSize", 20);
			}

			//Set the username
			remoteViews.setTextViewText(R.id.widget_mail_username, user_name);

			/* Finished setting remoteViews */

			if (user_name.equalsIgnoreCase("Loading...")){
				//Do nothing, on wrong loop through
			} else {
				clickIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
				clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
						allWidgetIds);

				PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, clickIntent, 0);
				//Set the onClickListener the TEXTVIEW. If the click the textview, it opens up the SelectMessageActivity2
				remoteViews.setOnClickPendingIntent(R.id.widget_mail_message_count, pendingIntent);
			}



			//PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, clickIntent,
			//PendingIntent.FLAG_UPDATE_CURRENT);
			//remoteViews.setOnClickPendingIntent(R.id.widget_mail_message_count, pendingIntent);
			appWidgetManager.updateAppWidget(widgetId, remoteViews);

			try{
				db.close();
				Log.d("Database", "Has been closed");
			} catch (Exception e){
				Log.d("Database", "Could not be closed!");
			}
		}
		stopSelf();
		Log.d("Service", "Has been Stopped");

		super.onStart(intent, startId);
	}

	//Reads in the accounts from the existing objects
	private void ReadInAccounts() {
		Log.d("SelectAccountActivity.ReadInAccounts", "checking for file" + AccountMan.CheckForFile());
		//accounts.clear(); //Clear any leftover accounts
		accounts = AccountMan.GetAccounts();
		Log.d("SelectAccountActivity.ReadInAccounts", String.valueOf(accounts.size()));
	}

	//When a response is received from the server
	public void onResponseReceived(String reply) {

		if(!reply.equals("error")) {
			Log.d("Deserializing Response", "Creating Response Object");
			messagesReceived = true;
			//Getting new messages, clearing list first.
			Response r = new Gson().fromJson(reply, Response.class);
			messages_array.clear();
			messages_array = r.result.messages;

			int message_count_int_received = r.result.status.empire.has_new_messages;
			String message_count_string_received = r.result.message_count;

		} else {
			Log.d("Error with Reply", "Error in onResponseReceived()");
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.d(LOG, "IBinder Called");
		return null;
	}

	//Async task to both run the server code and adjust the remoteviews when finished
	private class UpdateRemoteViewsViaAsync extends AsyncTask<ServerRequest, Void, String> {
		List<serverFinishedListener> listeners = new ArrayList<serverFinishedListener>();
		private String output = "";

		private Context context;
		private RemoteViews rv1;
		private int appwid;
		private String tag_chosen_async;
		private AppWidgetManager appWidgetManager;
		private String user_name;
		private int messages_in_display;
		private String async_message_count_string;
		private int async_message_count_int;
		int total_num_messages;
		private String async_font_color_choice, async_color_background_choice, async_sync_frequency,
				async_notifications_option, async_message_count_received;

		public UpdateRemoteViewsViaAsync(Context c, RemoteViews rv, int appwid, String aTag,
		                                 AppWidgetManager aAppWidgetManager, String username,
		                                 String async_color_background_choice1,
		                                 String async_font_color_choice1, String async_sync_frequency1,
		                                 String notifications_option1, String message_count_received1){
			this.context = c;
			this.appwid = appwid;
			this.async_notifications_option = notifications_option1;
			this.rv1 = rv;
			this.async_sync_frequency = async_sync_frequency1;
			this.tag_chosen_async = aTag;
			this.appWidgetManager = aAppWidgetManager;
			this.user_name = username;
			this.async_message_count_received = message_count_received1;
			this.async_color_background_choice = async_color_background_choice1;
			this.async_font_color_choice = async_font_color_choice1;
			Log.d("Output from my Async, username is ", user_name);
		}
		public void addListener(serverFinishedListener toAdd) {
			listeners.add(toAdd);
		}

		protected String doInBackground(ServerRequest... a) {
			output = ServerRequest(a[0].server, a[0].methodURL, a[0].json);
			convertData(output);
			Log.d("Output from my Async", output);
			//ResponseReceived();
			return output;
		}

		private void ResponseReceived() {
			//Log.d("Firing Event", "Sending out response to listeners");
			for (serverFinishedListener i : listeners) {
				i.onResponseReceived(output);
			}
			listeners.clear();
		}

		@Override
		protected void onPostExecute(String r) {
			//Log.d("OnPostExecute", "Firing On Post Execute");
			ResponseReceived();

			String messages_with_tag = "-1";

			try {
				if (tag_chosen_async.equalsIgnoreCase("All")) {
					Log.d("Message count string is at:", Integer.toString(async_message_count_int));
					rv1.setTextViewText(R.id.widget_mail_message_count, Integer.toString(async_message_count_int));
					messages_with_tag = Integer.toString(async_message_count_int);
				} else {
					Log.d("Message count string is at:", async_message_count_string);
					rv1.setTextViewText(R.id.widget_mail_message_count, async_message_count_string);
					messages_with_tag = async_message_count_string;
				}
			} catch (NullPointerException e){
				e.printStackTrace();
			}

			//Set the remote views dependent upon new data received
			//Check the number of messages and adjust the font size of the number of messages displayed. Prevents out of bounds on screen
			total_num_messages = Integer.parseInt(messages_with_tag); //Conversion is needed, cannot use old one here as diff if statement is in effect
			Log.d("Num messages", messages_with_tag);
			if (total_num_messages < 10){
				rv1.setFloat(R.id.widget_mail_message_count, "setTextSize", 32);
			} else if (total_num_messages >=10 && total_num_messages <100){
				rv1.setFloat(R.id.widget_mail_message_count, "setTextSize", 28);
			} else if (total_num_messages >= 100 && total_num_messages <999){
				rv1.setFloat(R.id.widget_mail_message_count, "setTextSize", 24);
			} else {
				rv1.setFloat(R.id.widget_mail_message_count, "setTextSize", 20);
			}

			//Set the username
			rv1.setTextViewText(R.id.widget_mail_username, user_name);

			//Check the number of messages last time versus the number of message this time.
			//If the number is higher this time and they have the notifications button checked, proceed
			int old_total_num_messages = Integer.parseInt(async_message_count_received);
			if (total_num_messages > old_total_num_messages){
				if (async_notifications_option.equalsIgnoreCase("true")){

					int x = total_num_messages - old_total_num_messages;
					//Create a notification icon
					String body = "You have " + x + " new messages " + user_name;
					String title = "New " + tag_chosen_async;
					notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
					Notifications.addNotification(context, notificationManager, uniqueID,
							body, title, "SelectMessageActivity2", user_name, tag_chosen_async);
				}
			}

			try {
				//Add the items here to a list
				List<String> passed_data = new ArrayList<>();

				passed_data = CreateListForDB.CreateList(
						Integer.toString(appwid), async_sync_frequency, user_name,
						Integer.toString(async_message_count_int), async_message_count_string, tag_chosen_async,
						async_color_background_choice, async_font_color_choice, selectedAccount.sessionID,
						selectedAccount.homePlanetID, notifications_option, Integer.toString(total_num_messages)
				);

				//Add the list to the database
				putDataInDatabase(context, passed_data, appwid);

			} catch (Exception e){
				e.printStackTrace();
				Log.d("Data not put into database ", "due to null pointer error on Selected account");
			}

			//Finally, update the widget
			appWidgetManager.updateAppWidget(appwid, rv1);
		}

		public String ServerRequest(String gameServer, String methodURL, String JsonRequest) {
			Log.d("Output from my Async, ServerRequest:", "Here");
			try {
				Log.d("AsyncServer.ServerRequest URL", (gameServer + "/" + methodURL));
				Log.d("AsyncServer.ServerRequest", "Request string " + JsonRequest);
				URL url = new URL(gameServer + "/" + methodURL);
				URLConnection connection = url.openConnection();
				connection.setDoOutput(true);
				OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
				out.write(JsonRequest);
				out.close();
				BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				output = in.readLine();
				Log.d("AsyncServer.ServerRequest", "Reply string " + output);
			} catch (java.net.MalformedURLException e) {
				Log.d("Server Error", "Malformed URL Exception");
				output = "error";
			} catch (java.io.IOException e) {
				Log.d("Server Error", "Malformed IO Exception");
				output = "error";
			}
			return output;

		}

		public void convertData(String reply) {

			if(!reply.equals("error")) {
				Log.d("Deserializing Response", "Creating Response Object");
				messagesReceived = true;
				//Getting new messages, clearing list first.
				Response r = new Gson().fromJson(reply, Response.class);
				messages_array.clear();
				messages_array = r.result.messages;

				async_message_count_int = r.result.status.empire.has_new_messages;
				async_message_count_string = r.result.message_count;
			}
		}

		private void putDataInDatabase(Context context, List<String> passed_list, int WidgetID) {
			//Create a database object and set the values here
			TEMPDatabaseAdapter db = new TEMPDatabaseAdapter(context);

			//For the row ID
			String widget_id = Integer.toString(WidgetID);

			try {
				db.updateRow(widget_id, passed_list);
			} catch (Exception e){
				e.printStackTrace();
				Log.d("WidgetService", "Error in insertData() method");
			}

			try{
				db.close();
				Log.d("WidgetService Database", "Has been closed");
			} catch (Exception e){
				Log.d("WidgetService Database", "Could not be closed!");
			}
		}
	}


}