package org.lightsys.eventApp.tools;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.lightsys.eventApp.R;
import org.lightsys.eventApp.data.ContactInfo;
import org.lightsys.eventApp.data.HousingInfo;
import org.lightsys.eventApp.data.Info;
import org.lightsys.eventApp.data.MapInfo;
import org.lightsys.eventApp.data.ScheduleInfo;
import org.lightsys.eventApp.views.MainActivity;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import javax.net.ssl.SSLHandshakeException;

import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


import static android.content.ContentValues.TAG;

/**
 * This class is used to pull JSON files (from the API URLs)
 * for an event and then format and store the data into the
 * local SQLite database
 *
 * @author otter57
 *
 */
public class DataConnection extends AsyncTask<String, Void, String> {

    private LocalDB db;
    private String qrAddress = null;    //location of JSON file
    private static String old_qrAddress = null; // QR address last time we did a connection
    private final WeakReference<Context> dataContext; // Context that the DataConnection was executed in
    private final WeakReference<Activity> dataActivity;
    private String action;
    private boolean isOnRefresh = false;
    private boolean connection;
    private CompletionInterface callback;
    private JSONObject json = null;
    public static final int UPD_EVENT = 0;
    public static final int UPD_NOTIFICATIONS = 1;

    private static final String RELOAD_PAGE = "reload_page";

    private static final String Tag = "DPS";

    public DataConnection(Context context, Activity activity, String action, String QR, CompletionInterface my_callback) {
        super();
        dataContext = new WeakReference<>(context);
        dataActivity = new WeakReference<>(activity);
        qrAddress = QR;
        this.callback = my_callback;
        this.action = action;
        if (action!= null && action.equals("refresh")) isOnRefresh = true;
        this.db = new LocalDB(dataContext.get());
        Log.d(TAG, "DataConnection: " + qrAddress);
    }

    @Override
    protected String doInBackground(String... params) {
        Calendar calExpire = Calendar.getInstance();
        Calendar calNow = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy", Locale.US);

        try {
            if(db.getGeneral("refresh_expire") != null) {
                calExpire.setTime(formatter.parse(db.getGeneral("refresh_expire")));
            }
            if (qrAddress == null) {
                ((MainActivity)dataActivity.get()).gatherData(true);
            } else if (qrAddress.equals("No_Event")) {
                throw new Exception("Refresh could not load any data because no event is scanned.");
            } else if (calNow.getTimeInMillis()<= calExpire.getTimeInMillis() || action.equals("new")) {
                DataPull();
            } else {
                action="expired";
            }
        }
        catch(Exception e) {
            Log.w(Tag, "The DataPull failed. (probably not connected to internet or vmPlayer): "
                    + e.getLocalizedMessage());
        }
        return null;
    }

    @Override
    protected void onPostExecute(String params) {
        if (callback != null)
            callback.onCompletion();

        if (dataContext != null && dataContext.get().getClass() == MainActivity.class && connection && !action.equals("auto_update")) {
            Toast.makeText(dataContext.get(), "data successfully imported", Toast.LENGTH_SHORT).show();
            if (action.equals("new")) {
                try {
                    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    Ringtone r = RingtoneManager.getRingtone(dataContext.get(), notification);
                    r.play();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if (!connection && !action.equals("expired")){
            action = action.equals("auto_update")?"auto_update_error":"retry";
        }
        Log.d(TAG, "onPostExecute: " + action);
        Intent reloadIntent = new Intent(RELOAD_PAGE);
        reloadIntent.putExtra("action", action).putExtra("received_url", qrAddress)
                    .putExtra("action_refresh", isOnRefresh);
        if (dataContext != null) {
            LocalBroadcastManager.getInstance(dataContext.get())
                    .sendBroadcast(reloadIntent);
        }
    }

    private String fetchJSON(String address)  {
        String jsontext;

        try {
            // Attempt to pull information from the API
            jsontext = GET(address);
            // Unauthorized signals incorrect username or password
            // 404 not found signals invalid ID
            // Empty or null signals an incorrect server name
            if (jsontext == null || jsontext.equals("invalid web address")){
                dataActivity.get().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(dataContext.get(), "Server connection failed: invalid web address", Toast.LENGTH_LONG).show();
                    }
                });
                return null;
            } else if (jsontext.equals("http request")) {
                dataActivity.get().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(dataContext.get(), "Server connection failed: HTTP requests not supported on Android 9.0+", Toast.LENGTH_LONG).show();
                    }
                });
                return null;
            }
            else if (jsontext.equals("") || jsontext.equals("Access Not Permitted")) {
                dataActivity.get().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(dataContext.get(), "Server connection failed", Toast.LENGTH_LONG).show();
                    }
                });
                return null;
            } else if (jsontext.contains("<H1>Unauthorized</H1>")) {
                dataActivity.get().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(dataContext.get(), "Username/password invalid", Toast.LENGTH_LONG).show();
                    }
                });
                return null;
            } else if (jsontext.contains("404 Not Found")) {
                dataActivity.get().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(dataContext.get(), "Invalid User ID", Toast.LENGTH_LONG).show();
                    }
                });
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            // GET function throws an Exception if server not found
            if(e.getClass()==SocketTimeoutException.class && dataContext != null && dataActivity != null){
                dataActivity.get().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(dataContext.get(), "Server connection timed out", Toast.LENGTH_LONG).show();
                    }
                });
            }else if(e.getClass()!=SSLHandshakeException.class && dataContext != null && dataActivity != null) {
                dataActivity.get().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(dataContext.get(), "Server connection failed", Toast.LENGTH_LONG).show();
                    }
                });
            }
            return null;
        }

        // Parse it
        try {
            json = new JSONObject(jsontext);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return jsontext;
    }

    /**
     * Pulls all event data
     * Modified by Littlesnowman88 on 15 June 2018
     * Now imports custom Notifications nav title
     */
    private void DataPull()  {
        String myURL;
        String result;

        if (dataContext == null)
            return;
        db = new LocalDB(dataContext.get());

        // Build the URL to include our current version numbers.
        int[] cur_version = db.getJSONVersionNum();
        if (qrAddress.indexOf("?") >= 0)
            myURL = qrAddress + "&";
        else
            myURL = qrAddress + "?";
        myURL = myURL + "config=" + cur_version[0] + "&notify=" + cur_version[1];

        // Fetch the data from the server.
        result = fetchJSON(myURL);
        connection = (result != null);

        if (connection) {
            Log.i(Tag, "pulling data");

            try {
                loadAllData(result);
            } catch (Exception e) {
                e.printStackTrace();
                if (e.getClass().equals(SocketTimeoutException.class)) {
                    Toast.makeText(dataContext.get(), "Server connection timed out", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(dataContext.get(), "Server connection failed", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // add new timestamp
            db.addTimeStamp("" + Calendar.getInstance().getTimeInMillis());
            db.close();

            // Make a note of the URL we fetched.
            old_qrAddress = qrAddress;
        }
    }

    /**
     * Loads all data, including general, event, and notifications.
     * @param result - the HTTP textual JSON data returned by the server
     */
    private void loadAllData(String result) {
        if (result != null) {
            int[] new_version = getVersionNumber(json /*, tempNames*/);
            int[] old_version = db.getJSONVersionNum();
            boolean[] update_flags = dataNeedsUpdate(old_version, new_version);
            boolean updated_notifications = false;
            boolean updated_event = false;
            Resources string_resources = dataContext.get().getResources();

            // Get the current name of the notifications menu entry
            String ntitle = "Notifications";
            String nicon = "ic_bell";
            ArrayList<Info> nav_titles = db.getNavigationTitles();
            for (Info item : nav_titles) {
                if (item.getName().equals("Notifications")) {
                    ntitle = item.getHeader();
                    nicon = item.getBody();
                    break;
                }
            }

            // if event config was updated
            if (update_flags[UPD_EVENT]) {
                db.clear();
                Log.d("DataConnection", "database cleared");
                Log.d("DataConnection", "database JSON version: " + db.getJSONVersionNum()[0]);
                Log.d("DataConnection", "old version num: " + old_version[0]);
                Log.d("DataConnection", "new version num: " + new_version[0]);
                db.addGeneral("url", qrAddress);

                // Add a notifications menu entry first, since we just cleared the DB
                JSONObject njson;
                njson = json.optJSONObject("notifications");
                if (njson != null) {
                    addNotificationTitle(njson, db, string_resources);
                } else {
                    db.addNavigationTitles(ntitle, nicon, "Notifications");
                }

                // General JSON event data
                JSONObject gjson;
                gjson = json.optJSONObject("general");
                if (gjson != null){
                    if (loadGeneralInfo(gjson)) {
                        updated_event = true;
                    }
                }

                // Update most event information
                if (loadEventInfo(json)) {
                    updated_event = true;
                }

                // About page
                setupAboutPage(db, string_resources);
            }

            // if notifications were updated
            if (update_flags[UPD_NOTIFICATIONS]) {
                // Find the notifications section -- if updated, it "should" be there.
                JSONObject njson;
                njson = json.optJSONObject("notifications");
                if (njson != null) {
                    loadNotifications(njson);
                    updated_notifications = true;
                } else {
                    // Special case where notifications need an update, but no notifications
                    // object was included in the JSON (this may mean we're working with an
                    // older style QR code / updates link).
                    String nURL = db.getGeneral("notifications_url");
                    if (nURL != null) {
                        String nText = fetchJSON(nURL);
                        if (nText != null) {
                            try {
                                njson = new JSONObject(nText);
                                if (njson != null) {
                                    loadNotifications(njson);
                                    updated_notifications = true;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }

            // Update the database with the new version numbers.
            int[] new_notif_version = {
                    updated_event?new_version[UPD_EVENT]:old_version[UPD_EVENT],
                    updated_notifications?new_version[UPD_NOTIFICATIONS]:old_version[UPD_NOTIFICATIONS]
            };
            db.replaceJSONVersionNum(new_notif_version);
        }
    }

    /** loads event information into the database and sets navigation titles
     *  A refactor created by Littlesnowman88 on 15 June 2018
     * IMPORTANT: If the refresh button is ever deleted and updates become completely automatic,
     *            The if statement structure in this function will need to be deleted.
     *            readGeneralInfo and loadGeneralInfo will need to become void.
     */
    /*private void loadInfoAndNavTitles() {
        Resources string_resources = dataContext.get().getResources();
        if (readGeneralInfo(connectionResult)) {
            //String notification_url = db.getGeneral("notifications_url");
            //connection = fetchJSON(notification_url);
            addNotificationTitle(connectionResult, db, string_resources);
            //connection = fetchJSON(qrAddress);
            loadEventInfo(connectionResult);
            setupAboutPage(db, string_resources);
        }
    }*/

    /**
     * Sets up the about page
     * @param db
     * @param string_resources
     *
     * Must be called by MainActivity's handleNoScannedEvent, thus this function is static.
     */
    public static void setupAboutPage(LocalDB db, Resources string_resources){
        ArrayList<Info> nav_titles = db.getNavigationTitles();
        String about_title = string_resources.getString(R.string.about_title);
        for (Info item : nav_titles) {
            if (item.getHeader().equals(about_title)) {
                return;
            }
        }

        db.addNavigationTitles(string_resources.getString(R.string.about_title), "ic_info", "About");
        db.addAboutPage(new Info(string_resources.getString(R.string.About_App_Header) + " " + MainActivity.version, string_resources.getString(R.string.About_App_Body)), "About");
        db.addAboutPage(new Info(string_resources.getString(R.string.Open_Source_Header),string_resources.getString(R.string.Open_Source_Body)), "About");
        db.addAboutPage(new Info(string_resources.getString(R.string.Barcode_Scanner_Header),string_resources.getString(R.string.Barcode_Scanner_Body)), "About");
        db.addAboutPage(new Info(string_resources.getString(R.string.Android_Open_Source_Proj_Header), string_resources.getString(R.string.Android_Open_Source_Proj_Body)), "About");

    }

    /** Loads General Info
     *  A refactor created by Littlesnowman88 on 15 June 2015
     * IMPORTANT: If the refresh button is ever deleted and updates become completely automatic,
     *            THIS WILL NEED TO RETURN VOID
     */
    /*private boolean readGeneralInfo(String result) {
        JSONObject json = null;
        if (result != null) {
            try {
                json = new JSONObject(result);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (json == null) {
                return false;
            }
            try {
                    return loadGeneralInfo(json.getJSONObject("general"));
            } catch (Exception e) {
                //e.printStackTrace();
                return false;
            }
        }
        return false;
    }*/

    /** acesses the notifications json and sets the notifications nav title
     *  Created by: Littlesnowman88 on 15 June 2018
     *
     *  Must be called by MainActivity's handleNoScannedEvent, thus this function is public static.
     */
    public static void addNotificationTitle(JSONObject njson, LocalDB db, Resources string_resources) {
        String notifications_title = string_resources.getString(R.string.notifications_title);

        try {
            if (njson != null) {
                db.addNavigationTitles(njson.getString("nav"), njson.getString("icon"), "Notifications");
            } else {
                preventRepeatTitle(notifications_title, db);
            }
        } catch (JSONException e1) {
            e1.printStackTrace();
            preventRepeatTitle(notifications_title, db);
        }
    }

    /**
     * Used to prevent navigation titles from repeating
     * @param notifications_title
     * @param db
     *
     * Static because it is called from static addNotificationTitle, also this function exists to reduce
     * code duplication within addNotificationTitle
     */
    private static void preventRepeatTitle(String notifications_title, LocalDB db){
        ArrayList<Info> nav_titles = db.getNavigationTitles();
        for (Info item : nav_titles) {
            if (item.getHeader().equals(notifications_title)) {
                return;
            }
        }
        db.addNavigationTitles(notifications_title, "ic_bell", "Notifications");
    }

    /**
     * Loads Event info
     * Slightly refactored by Littlesnowman88 on June 15 2018
     * loadGeneralInfo function call moved into readGeneralInfo;
     */
    private boolean loadEventInfo(JSONObject ejson) {
        boolean updated = false;

        //separate JSON object and get individual parts to be stored in Local Database
        if (loadTheme(ejson.optJSONArray("theme"))) updated=true;
        if (loadContactPage(ejson.optJSONObject("contact_page"))) updated=true;
        if (loadSchedule(ejson.optJSONObject("schedule"))) updated=true;
        if (loadHousing(ejson.optJSONObject("housing"))) updated=true;
        if (loadPrayerPartners(ejson.optJSONArray("prayer_partners"))) updated=true;
        if (loadInformationalPage(ejson.optJSONObject("information_page"))) updated=true;
        if (loadContacts(ejson.optJSONObject("contacts"))) updated=true;
        if (loadMaps(ejson.optJSONObject("maps"))) updated=true;

        return updated;
    }


    /**
     * Loads Contact info
     * @param json, result of the API query for the contact info
     */
    private boolean loadContacts(JSONObject json) {
        if (json == null) {
            return false;
        }
        JSONArray tempNames = json.names();

        Resources string_resources = dataContext.get().getResources();

        for (int i = 0; i < tempNames.length(); i++) {
            try {
                //@id signals a new object, but contains no information on that line
                if (!tempNames.getString(i).equals("@id")) {
                    JSONObject ContactObj = json.getJSONObject(tempNames.getString(i));

                    String contact_name = tempNames.getString(i);
                    String contact_address = ContactObj.getString("address");
                    String contact_phone = (ContactObj.getString("phone"));
                    if (contact_name == null || contact_name.equals("")) contact_name = string_resources.getString(R.string.no_name);
                    if (contact_address.equals("null")) contact_address = "";
                    if (contact_phone.equals("null")) contact_phone = "";

                    // add the Contact Object to db
                    ContactInfo temp = new ContactInfo();
                    temp.setName(contact_name);
                    temp.setAddress(contact_address);
                    temp.setPhone(contact_phone);

                    db.addContact(temp);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    /**
     * Loads Contact Page info
     * @param json, result of the API query for the contact page info
     */
    private boolean loadContactPage(JSONObject json) {
        if (json == null) {
            return false;
        }
        JSONArray tempNames = json.names();

        //if valid, adds page to navigation
        try {
            if (json.getString("nav").equals("null")) {
                return false;
            }
            db.addNavigationTitles(json.getString("nav"), json.getString("icon"), "Contacts");

        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }

        for (int i = 0; i < tempNames.length(); i++) {
            try {
                //@id signals a new object, but contains no information on that line
                if (!tempNames.getString(i).equals("@id") && !tempNames.get(i).equals("nav") && !tempNames.get(i).equals("icon")) {
                    JSONObject ContactObj = json.getJSONObject(tempNames.getString(i));
                    String header = ContactObj.getString("header");
                    String body = ContactObj.getString("content");
                    // add the Contact Page Object to db
                    if (header == null || header.equals("null")) header = "";
                    if (body == null || body.equals("null")) body = "";
                    Info temp = new Info();
                    temp.setHeader(header);
                    temp.setBody(body);
                    temp.setId(ContactObj.getInt("id"));

                    db.addContactPage(temp);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    /**
     * Loads schedule
     * @param json, result of API query for schedule
     */
    private boolean loadSchedule(JSONObject json) {
        if (json == null) {
            return false;
        }
        JSONArray tempNames = json.names();

        //if valid, adds page to navigation
        try {
            if (json.getString("nav").equals("null")) {
                return false;
            }
            db.addNavigationTitles(json.getString("nav"), json.getString("icon"), "Schedule");

        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }

        for (int i = 0; i < tempNames.length(); i++) {
            try {
                //@id signals a new object, but contains no information on that line
                if (!tempNames.getString(i).equals("@id") && !tempNames.get(i).equals("nav") && !tempNames.get(i).equals("icon")) {
                    JSONArray DayArray = json.getJSONArray(tempNames.getString(i));

                    for (int n = 0; n < DayArray.length(); n++) {
                        JSONObject Event = DayArray.getJSONObject(n);

                        String sch_day = tempNames.getString(i);
                        int sch_time_start = Event.getInt("start_time");
                        int sch_time_length = Event.getInt("length");
                        String sch_description = Event.getString("description");
                        String sch_location = Event.getString("location");
                        String sch_category = Event.getString("category");

                        // add the Schedule Object to db
                        ScheduleInfo temp = new ScheduleInfo();
                        temp.setDay(sch_day);
                        temp.setTimeStart(sch_time_start);
                        temp.setTimeLength(sch_time_length);
                        temp.setDesc(sch_description);
                        temp.setLocationName(sch_location);
                        temp.setCategory(sch_category);

                        db.addSchedule(temp);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    /**
     * Loads general Information
     * @param json, result of API query for general information
     * Modified by Littlesnowman88 & TFMoo on 12 July 2018
     * IMPORTANT: If the refresh button is ever deleted and updates become completely automatic,
     *            THIS WILL NEED TO RETURN VOID
     */
    /*private boolean loadGeneralInfo(JSONObject json) {
        if (json == null) {
            return false;
        }

        JSONArray tempGeneral = json.names();

        int[] new_version = getVersionNumber(json);
        int[] old_version = db.getJSONVersionNum();
        if (qrAddress.equals(db.getGeneral("url")) || qrAddress.equals(old_qrAddress)) { //if updating same event
            boolean[] update_flags = dataNeedsUpdate(old_version, new_version);

            if (update_flags[0]) { //config update needed == true
                int[] new_config_version = {new_version[0], old_version[1]};
                db.clear();
                db.addGeneral("url", qrAddress);
                db.replaceJSONVersionNum(new_config_version);
                finishLoadGeneralInfo(json, tempGeneral);
            } else { //config update needed = false
                if (update_flags[1]) { //notifications update needed == true
                    //connection = fetchJSON(db.getGeneral("notifications_url"));
                    loadNotifications(connectionResult);
                }
                return false;
            }


        } else { //if scanning/selecting new event
            db.clear();
            db.addGeneral("url", qrAddress);
            int[] notif_forced_update_version = {new_version[0], 0}; // 0 forces the notification to recognize a "version change" and update notifications.
            db.replaceJSONVersionNum(notif_forced_update_version);
            finishLoadGeneralInfo(json, tempGeneral);
            //connection = fetchJSON(db.getGeneral("notifications_url"));
            loadNotifications(connectionResult);
        }
        return true;
    }*/

    /** loads all of general info except for version info
     *  Created/Refactored by Littlesnowman88 on 12 July 2018
     */
    private boolean loadGeneralInfo(JSONObject qrJSON) {
        JSONArray tempGeneral = qrJSON.names();
        int num_general_items = tempGeneral.length();

        //for backwards compatibility with old JSONs.
        try {
            String first_general_gategory = tempGeneral.getString(0);
            if (!first_general_gategory.equals("version_num")) {
                db.addGeneral(first_general_gategory, qrJSON.getString(first_general_gategory));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        for (int i = 1; i < num_general_items; i++) {
            try {
                //@id signals a new object, but contains no information on that line
                String general_category_title = tempGeneral.getString(i);
                if (!general_category_title.equals("@id")) {
                    //only loads refresh rate into database if rate is not already specified by the user
                    if (!general_category_title.equals("refresh_rate") || db.getGeneral("refresh_rate") == null) {
                        db.addGeneral(general_category_title, qrJSON.getString(general_category_title));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
        }

        return true;
    }

    /**
     * Loads notifications Information
     * @param json, result of API query for hq information
     * Modified by Littlesnowman88 & FTMoo on 12 July 2018
     * IMPORTANT: If the refresh button is ever deleted and updates become completely automatic,
     *            THIS WILL NEED TO RETURN VOID
     */
    private void loadNotifications(JSONObject json) {
        JSONArray tempNames = json.names();
        Log.d("Notifications", "JSON object: " + json);
        ArrayList<Info> notifications = db.getNotifications();
        db.deleteNotifications();
        boolean isSameURL = (qrAddress.equals(old_qrAddress) || old_qrAddress == null);

        //Weird formatting issues with notifications wrapper not being taken off
        try {
            if (tempNames.length() == 1 && tempNames.getString(0).equals("notifications")) {
                json = json.getJSONObject("notifications");
                tempNames = json.names();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        int num_notif_items = tempNames.length();
        for (int i = 0; i < num_notif_items; i++) {
            try {
                String json_item = tempNames.getString(i);
                //@id signals a new object, but contains no information on that line
                if (! json_item.equals("version_num") && !json_item.equals("@id") && !json_item.equals("nav") && !json_item.equals("icon")) {
                    JSONObject notificationObj = json.getJSONObject(json_item);
                    Info temp = new Info();
                    temp.setId(Integer.parseInt(json_item));
                    temp.setHeader(notificationObj.getString("title"));
                    temp.setBody(notificationObj.getString("body"));
                    temp.setDate(notificationObj.getString("date"));
                    if (isSameURL && notificationIsNewOrChanged(notifications,temp)) {
                        temp.setNew();
                    } else {
                        temp.setOld();
                    }
                    Log.d("Notifications", "temp: " + temp);
                    db.addNotification(temp);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private static boolean notificationIsNewOrChanged(ArrayList<Info> notifications, Info new_notif) {
        for (Info notif : notifications){
            // if the notification currently exists
            if (notif.getId() == new_notif.getId()) {
                // but something significant about it has changed
                if (!notif.getBody().equals(new_notif.getBody())
                        || !notif.getHeader().equals(new_notif.getHeader())
                        || !notif.getDate().equals(new_notif.getDate())) {
                    return true;
                } else {
                    // the notification exists and nothing significant has changed
                    return false;
                }
            }
        }

        // else, the notification ID was not found, so it must be new.
        return true;
    }

    // Gets the version number from the JSON.  For compatibility with older JSON data that
    // does not contain a version_num, we assume version 0,0 on those.
    private static int[] getVersionNumber(JSONObject qrJSON) {
        try {
            String version_string = qrJSON.getString("version_num");
            //String version_string = qrJSON.getString(json_categories.getString(0)); //make this flexible in ordering?
            String[] version_tokens = version_string.split(",");
            if (version_tokens.length == 2) {
                int[] version = {Integer.parseInt(version_tokens[0]), Integer.parseInt(version_tokens[1])};
                return version;
            } else {
                int[] version = {0,0};
                return version;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            int[] version = {0,0};
            return version;
        }
    }

    /**
     * compares version numbers to see if configuration files need to update.  If version numbers
     * are 0, i.e. they do not exist in the data, we always update (this is for legacy json data
     * compatibility).
     * @result boolean[] - returns a two-element array of booleans indicating update requirement
     *                     for [0] Event Data, and [1] Notifications.
     */
    private static boolean[] dataNeedsUpdate(int[] old_version, int[] new_version) {
        boolean[] update_flags = {false, false};
        if (old_version[UPD_EVENT] != new_version[UPD_EVENT] || new_version[UPD_EVENT] == 0) {
            update_flags[UPD_EVENT] = true;
        }
        if (old_version[UPD_NOTIFICATIONS] != new_version[UPD_NOTIFICATIONS] || new_version[UPD_NOTIFICATIONS] == 0) {
            update_flags[UPD_NOTIFICATIONS] = true;
        }
        return update_flags;
    }

    /**
     * Loads all housing assignments
     * @param json, result of API query for housing
     */
    private boolean loadHousing(JSONObject json) {
        if (json == null) {
            return false;
        }
        JSONArray tempNames = json.names();

        try {
            if (json.getString("nav").equals("null")) {
                return false;
            }
            db.addNavigationTitles(json.getString("nav"), json.getString("icon"), "Housing");

        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }

        Resources string_resources = dataContext.get().getResources();

        for (int i = 0; i < tempNames.length(); i++) {
            try {
                String json_item = tempNames.getString(i);
                //@id signals a new object, but contains no information on that line
                if (!json_item.equals("@id") && !json_item.equals("nav") && !json_item.equals("icon")) {
                    JSONObject HousingObj = json.getJSONObject(json_item);
                    HousingInfo temp = new HousingInfo();
                    String host_name, driver, students;

                    host_name = (!json_item.equals(""))? json_item : string_resources.getString(R.string.no_host);
                    driver = HousingObj.getString("driver");
                    students = HousingObj.getString("students");

                    if(driver == null || driver.equals("")){driver = string_resources.getString(R.string.no_driver);}
                    if(students == null || students.equals("")) {students = string_resources.getString(R.string.no_guest);}

                    // add the Housing Info Object to db
                    temp.setName(host_name);
                    temp.setDriver(driver);
                    temp.setStudents(students);

                    db.addHousing(temp);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    /**
     * Loads all Prayer Partner teams
     * @param json, result of API query for prayer partners
     */
    private boolean loadPrayerPartners(JSONArray json) {
        if (json == null) {
            return false;
        }

        try {
            if (json.getJSONObject(0).getString("nav").equals("null")) {
                return false;
            }
            db.addNavigationTitles(json.getJSONObject(0).getString("nav"), json.getJSONObject(0).getString("icon"), "Prayer Partners");

        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }

        for (int i = 1; i < json.length(); i++) {
            try {
                JSONObject PrayerPartnerObj = json.getJSONObject(i);

                String students = PrayerPartnerObj.getString("students");
                // add the Contact Object to db
                db.addPrayerPartners(students);
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    /**
     * Loads colors for theme and schedule
     * @param json, result of API query for theme
     */
    private boolean loadTheme(JSONArray json) {
        if (json == null) {
            return false;
        }

        for (int i = 0; i < json.length(); i++) {
            try {
                JSONObject ColorObj = json.getJSONObject(i);

                String name = ColorObj.names().getString(0);
                String color = ColorObj.getString(name);
                // add the Contact Object to db
                if (color.equals("null")) {
                    switch (name) {
                        case "themeDark":
                            color = "#304166";
                            break;
                        case "themeMedium":
                            color = "#364871";
                            break;
                        case "themeColor":
                            color = "#4E69B8";
                            break;
                    }
                }
                db.addThemeColor(name, color);
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    /**
     * Loads Information pages
     * @param json, result of API query for information pages
     */
    private boolean loadInformationalPage(JSONObject json) {
        if (json == null) {
            return false;
        }
        JSONArray tempNames = json.names();

        for (int i = 0; i < tempNames.length(); i++) {
            try {
                //@id signals a new object, but contains no information on that line
                if (!tempNames.getString(i).equals("@id")) {
                    JSONArray InfoArray = json.getJSONArray(tempNames.getString(i));

                    String title = InfoArray.getJSONObject(0).getString("nav") + " ";

                    String icon = InfoArray.getJSONObject(0).getString("icon");
                    icon = icon.equals("")?"ic_clipboard":icon;
                    db.addNavigationTitles(title, icon, title);

                    for (int n = 1; n < InfoArray.length(); n++) {

                        JSONObject information = InfoArray.getJSONObject(n);

                        // add the Information Object to db
                        Info temp = new Info();
                        temp.setHeader(information.getString("title"));
                        temp.setBody(information.getString("description"));

                        db.addInformationPage(temp, title);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }


    /**
     * Loads all maps
     * @param json, result of API query for maps
     */
    private boolean loadMaps(JSONObject json) {
        if (json == null) {
            return false;
        }
        JSONArray tempNames = json.names();

        try {
            if (json.getString("nav").equals("null")) {
                return false;
            }
            db.addNavigationTitles(json.getString("nav"), json.getString("icon"), "Maps");

        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }

        Resources string_resources = dataContext.get().getResources();

        for (int i = 0; i < tempNames.length(); i++) {
            try {
                String json_item = tempNames.getString(i);
                //@id signals a new object, but contains no information on that line
                if (!json_item.equals("@id") && !json_item.equals("nav") && !json_item.equals("icon")) {
                    JSONObject MapObj = json.getJSONObject(json_item);
                    MapInfo temp = new MapInfo();
                    String map_name;
                    HashMap<ArrayList<String>, ArrayList<Double>> POIs = new HashMap<>();
                    Double topLeftLat, topLeftLong, botRightLat, botRightLong;

                    map_name = (!json_item.equals(""))? json_item : string_resources.getString(R.string.no_map);

                    JSONArray JSONpois = MapObj.getJSONArray("POIs");
                    ArrayList<String> POIstring = new ArrayList<>();
                    ArrayList<Double> POIdouble = new ArrayList<>();
                    for (int j = 0; j < JSONpois.length(); j++) {
                        JSONObject tempObj = JSONpois.getJSONObject(j);
                        POIstring.add(tempObj.getString("name"));
                        POIstring.add(tempObj.getString("icon"));
                        POIdouble.add(tempObj.getDouble("lat"));
                        POIdouble.add(tempObj.getDouble("long"));
                        POIs.put(POIstring, POIdouble);
                    }

                    topLeftLat = Double.parseDouble(MapObj.getString("topLeftLat"));
                    topLeftLong = Double.parseDouble(MapObj.getString("topLeftLong"));
                    botRightLat = Double.parseDouble(MapObj.getString("botRightLat"));
                    botRightLong = Double.parseDouble(MapObj.getString("botRightLong"));



//                    if(driver == null || driver.equals("")){driver = string_resources.getString(R.string.no_driver);}
//                    if(students == null || students.equals("")) {students = string_resources.getString(R.string.no_guest);}

                    // add the Map Info Object to db
                    temp.setName(map_name);
                    temp.setJSONPoi(JSONpois.toString());
                    temp.setPOIs(POIs);
                    temp.setTopLeftLat(topLeftLat);
                    temp.setTopLeftLong(topLeftLong);
                    temp.setBotRightLat(botRightLat);
                    temp.setTopLeftLong(botRightLong);

                    db.addMaps(temp);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    /**
     * Send a get request from the url
     *
     * @param urlString, url for get request.
     * @return string results of the query.
     */
    private String GET(String urlString) {
        String response = null;
        String http = urlString.substring(0,5);

        // Reject connection if scanning http with phone of Android 9.0+
        if (android.os.Build.VERSION.SDK_INT >= 28 && !http.equals("https")) {
            return "http request";
        }
        try{
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(6000);
            conn.setRequestMethod("GET");
            // read the response
            InputStream in = new BufferedInputStream(conn.getInputStream());
            response = convertStreamToString(in);
        } catch (ConnectException ce){
            ce.printStackTrace();
            return "invalid web address";
        } catch (IOException e) {
            // Writing exception to log
            e.printStackTrace();
        }
        return response;
    }

    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

}
