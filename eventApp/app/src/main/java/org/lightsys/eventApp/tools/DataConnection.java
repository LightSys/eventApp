package org.lightsys.eventApp.tools;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.lightsys.eventApp.R;
import org.lightsys.eventApp.data.ContactInfo;
import org.lightsys.eventApp.data.HousingInfo;
import org.lightsys.eventApp.data.Info;
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
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import javax.net.ssl.SSLHandshakeException;

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
    private String qrAddress;    //location of JSON file
    private String old_qrAddress;
    private final WeakReference<Context> dataContext; // Context that the DataConnection was executed in
    private final WeakReference<Activity> dataActivity;
    private ProgressBar loading_bar;
    private String action;
    private boolean loadAll; //specifies whether all info should be reloaded or only notifications
    private boolean connection;
    private String connectionResult;
    private CompletionInterface callback;
    private Runnable mainActivityRunnable;

    private static final String RELOAD_PAGE = "reload_page";

    private static final String Tag = "DPS";

    public DataConnection(Context context, Activity activity, String action, String QR, boolean loadAll, CompletionInterface my_callback,Runnable runnable) {
        super();
        dataContext = new WeakReference<>(context);
        dataActivity = new WeakReference<>(activity);
        qrAddress = QR;
        this.callback = my_callback;
        this.loadAll = loadAll;
        this.action = action;
        this.db = new LocalDB(dataContext.get());
        Log.d(TAG, "DataConnection: " + qrAddress);
        if (activity != null) {
            RelativeLayout main_layout = dataActivity.get().findViewById(R.id.main_layout);
            loading_bar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
            loading_bar.setBackgroundColor(Color.parseColor("#787878"));
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(600, 100);
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
            main_layout.addView(loading_bar, params);
        }
        mainActivityRunnable = runnable;
    }

    @Override
    protected String doInBackground(String... params) {
        Calendar calExpire = Calendar.getInstance();
        Calendar calNow = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        try{
            if(db.getGeneral("refresh_expire") != null) {
                calExpire.setTime(formatter.parse(db.getGeneral("refresh_expire")));
            }
            if (qrAddress == null){
                ((MainActivity)dataActivity.get()).gatherData(true);
            }else if(calNow.getTimeInMillis()<= calExpire.getTimeInMillis() || action.equals("new")){
                DataPull();
            }else{
                action="expired";
            }
        }
        catch(Exception e){
            Log.w(Tag, "The DataPull failed. (probably not connected to internet or vmPlayer): "
                    + e.getLocalizedMessage());
        }
        return null;
    }

    @Override
    protected void onPostExecute(String params) {
        if (callback != null)
            callback.onCompletion();

        // Dismiss progress bar to show data retrieval is done
        if (dataActivity != null && dataActivity.get() != null && loading_bar != null) {
            loading_bar.setVisibility(View.GONE);
            dataActivity.get().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
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
        reloadIntent.putExtra("action", action);
        if (dataContext != null) {
            LocalBroadcastManager.getInstance(dataContext.get())
                    .sendBroadcast(reloadIntent);
        }
        if(mainActivityRunnable != null){
            mainActivityRunnable.run();
        }

    }

    private boolean checkConnection(String address)  {
        String test;
        try {
            // Attempt to pull information from the API
            test = GET(address);
            // Unauthorized signals incorrect username or password
            // 404 not found signals invalid ID
            // Empty or null signals an incorrect server name
            if (test == null || test.equals("invalid web address")){
                dataActivity.get().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(dataContext.get(), "Server connection failed: invalid web address", Toast.LENGTH_LONG).show();
                    }
                });
                return false;
            }
            else if (test.equals("") || test.equals("Access Not Permitted")) {
                dataActivity.get().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(dataContext.get(), "Server connection failed", Toast.LENGTH_LONG).show();
                    }
                });
                return false;
            } else if (test.contains("<H1>Unauthorized</H1>")) {
                dataActivity.get().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(dataContext.get(), "Username/password invalid", Toast.LENGTH_LONG).show();
                    }
                });
                return false;
            } else if (test.contains("404 Not Found")) {
                dataActivity.get().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(dataContext.get(), "Invalid User ID", Toast.LENGTH_LONG).show();
                    }
                });
                return false;
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
            return false;
        }
        connectionResult = test;
        return true;
    }

    /**
     * Pulls all event data
     * Modified by Littlesnowman88 on 15 June 2018
     * Now imports custom Notifications nav title
     */
    private void DataPull()  {
        if (dataContext == null)
            return;
        db = new LocalDB(dataContext.get());

        //set loading bar as app collects data
        if (dataActivity != null && dataActivity.get() != null && loading_bar != null) {
            //TODO: SET TEXT HERE? OR SET PROGRESS TO ZERO
//                    setMessage("Gathering event info...");
            dataActivity.get().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loading_bar.setIndeterminate(true);
                    dataActivity.get().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    loading_bar.setVisibility(View.VISIBLE);

                }
            });
        }

        connection = checkConnection(qrAddress);

        //if connection error occurred, cancel spinner
        if (!connection && dataActivity != null && dataActivity.get() != null && loading_bar != null){
            loading_bar.setVisibility(View.GONE);
            dataActivity.get().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }

        if (connection) {
            Log.i(Tag, "pulling data");

            try {
                old_qrAddress = db.getGeneral("notifications_url");
                if(loadAll) {
                    loadInfoAndNavTitles();
                } else{
                    connection = checkConnection(db.getGeneral("notifications_url"));
                    loadNotifications(connectionResult);
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (e.getClass().equals(SocketTimeoutException.class)) {
                    Toast.makeText(dataContext.get(), "Server connection timed out", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(dataContext.get(), "Server connection failed", Toast.LENGTH_LONG).show();
                }
                return;
                //to here
            }

            // add new timestamp
            db.addTimeStamp("" + Calendar.getInstance().getTimeInMillis());

            db.close();
        }
    }

    /** loads event information into the database and sets navigation titles
     *  A refactor created by Littlesnowman88 on 15 June 2018
     * IMPORTANT: If the refresh button is ever deleted and updates become completely automatic,
     *            The if statement structure in this function will need to be deleted.
     *            readGeneralInfo and loadGeneralInfo will need to become void.
     */
    private void loadInfoAndNavTitles() {
        Resources string_resources = dataContext.get().getResources();
        if (readGeneralInfo(connectionResult)) {
            String notification_url = db.getGeneral("notifications_url");
            connection = checkConnection(notification_url);
            addNotificationTitle(connectionResult, db, string_resources);
            connection = checkConnection(qrAddress);
            loadEventInfo(connectionResult);
            setupAboutPage(db, string_resources);
        }
    }

    /**
     * Sets up the about page
     * @param db
     * @param string_resources
     *
     * Must be called by MainActivity's handleNoScannedEvent, thus this function is static.
     */
    public static void setupAboutPage(LocalDB db, Resources string_resources){
        db.addAboutPage(new Info(string_resources.getString(R.string.About_App_Header),string_resources.getString(R.string.About_App_Body)), "About");
        db.addAboutPage(new Info(string_resources.getString(R.string.Open_Source_Header),string_resources.getString(R.string.Open_Source_Body)), "About");
        db.addAboutPage(new Info(string_resources.getString(R.string.Barcode_Scanner_Header),string_resources.getString(R.string.Barcode_Scanner_Body)), "About");
        db.addAboutPage(new Info(string_resources.getString(R.string.Android_Open_Source_Proj_Header), string_resources.getString(R.string.Android_Open_Source_Proj_Body)), "About");
        ArrayList<Info> nav_titles = db.getNavigationTitles();
        String about_title = string_resources.getString(R.string.about_title);
        for (Info item : nav_titles) {
            if (item.getHeader().equals(about_title)) {
                return;
            }
        }
        db.addNavigationTitles(string_resources.getString(R.string.about_title), "ic_info", "About");

    }

    /** Loads General Info
     *  A refactor created by Littlesnowman88 on 15 June 2015
     * IMPORTANT: If the refresh button is ever deleted and updates become completely automatic,
     *            THIS WILL NEED TO RETURN VOID
     */
    private boolean readGeneralInfo(String result) {
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
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    /** acesses the notifications json and sets the notifications nav title
     *  Created by: Littlesnowman88 on 15 June 2018
     *
     *  Must be called by MainActivity's handleNoScannedEvent, thus this function is public static.
     */
    public static void addNotificationTitle(String result, LocalDB db, Resources string_resources) {
        String notifications_title = string_resources.getString(R.string.notifications_title);

        if (result != null) {
            try {
                JSONObject json = null;
                json = new JSONObject(result).getJSONObject("notifications");
                if (json != null) {
                    db.addNavigationTitles(json.getString("nav"), json.getString("icon"), "Notifications");
                } else {
                    preventRepeatTitle(notifications_title, db);
                }
            } catch (JSONException e1) {
                e1.printStackTrace();
                preventRepeatTitle(notifications_title, db);
            }
        } else {
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
     * @param result, result of the API query for the contact info
     * Slightly refactored by Littlesnowman88 on June 15 2018
     * loadGeneralInfo function call moved into readGeneralInfo;
     */
    private void loadEventInfo(String result) {
        JSONObject json = null;
        if (result != null) {
            try {
                json = new JSONObject(result);
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
            if (json == null) {
                return;
            }
            //separate JSON object and get individual parts to be stored in Local Database
            try { loadTheme(json.getJSONArray("theme")); } catch (JSONException e) { e.printStackTrace(); }
            try { loadContactPage(json.getJSONObject("contact_page")); } catch (JSONException e) { e.printStackTrace(); }
            try { loadSchedule(json.getJSONObject("schedule")); } catch (JSONException e) { e.printStackTrace(); }
            try { loadHousing(json.getJSONObject("housing")); } catch (JSONException e) { e.printStackTrace(); }
            try { loadPrayerPartners(json.getJSONArray("prayer_partners")); } catch (JSONException e) { e.printStackTrace(); }
            try { loadInformationalPage(json.getJSONObject("information_page")); } catch (JSONException e) { e.printStackTrace(); }
            try { loadContacts(json.getJSONObject("contacts")); } catch (JSONException e) { e.printStackTrace(); }
        }
    }


    /**
     * Loads Contact info
     * @param json, result of the API query for the contact info
     */
    private void loadContacts(JSONObject json) {
        if (json == null) {
            return;
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
            }
        }

    }

    /**
     * Loads Contact Page info
     * @param json, result of the API query for the contact page info
     */
    private void loadContactPage(JSONObject json) {
        if (json == null) {
            return;
        }
        JSONArray tempNames = json.names();

        //if valid, adds page to navigation
        try {
            if (json.getString("nav").equals("null")) {
                return;
            }
            db.addNavigationTitles(json.getString("nav"), json.getString("icon"), "Contacts");

        } catch (JSONException e) {
            e.printStackTrace();
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
            }
        }
    }

    /**
     * Loads schedule
     * @param json, result of API query for schedule
     */
    private void loadSchedule(JSONObject json) {
        if (json == null) {
            return;
        }
        JSONArray tempNames = json.names();

        //if valid, adds page to navigation
        try {
            if (json.getString("nav").equals("null")) {
                return;
            }
            db.addNavigationTitles(json.getString("nav"), json.getString("icon"), "Schedule");

        } catch (JSONException e) {
            e.printStackTrace();
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
            }
        }
    }

    /**
     * Loads general Information
     * @param json, result of API query for general information
     * Modified by Littlesnowman88 & TFMoo on 12 July 2018
     * IMPORTANT: If the refresh button is ever deleted and updates become completely automatic,
     *            THIS WILL NEED TO RETURN VOID
     */
    private boolean loadGeneralInfo(JSONObject json) {
        if (json == null) {
            return false;
        }

        JSONArray tempGeneral = json.names();

        int[] new_version = getVersionNumber(json, tempGeneral);
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
                    connection = checkConnection(db.getGeneral("notifications_url"));
                    loadNotifications(connectionResult);
                }
                return false;
            }


        } else { //if scanning/selecting new event
            db.clear();
            db.addGeneral("url", qrAddress);
            int[] notif_forced_update_version = {new_version[0], -1}; //-1 forces the notification to recognize a "version change" and update notifications.
            db.replaceJSONVersionNum(notif_forced_update_version);
            finishLoadGeneralInfo(json, tempGeneral);
            connection = checkConnection(db.getGeneral("notifications_url"));
            loadNotifications(connectionResult);
        }
        return true;
    }

    /** loads all of general info except for version info
     *  Created/Refactored by Littlesnowman88 on 12 July 2018
     */
    private void finishLoadGeneralInfo(JSONObject qrJSON, JSONArray tempGeneral) {
        int num_general_items = tempGeneral.length();
        //for backwards compatibility with old JSONs.
        try {
            String first_general_gategory = tempGeneral.getString(0);
            if (!first_general_gategory.equals("version_num")) {
                db.addGeneral(first_general_gategory, qrJSON.getString(first_general_gategory));
            }
        } catch (Exception e) {e.printStackTrace();}
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
            }
        }
    }

    /**
     * Loads notifications Information
     * @param result, result of API query for hq information
     * Modified by Littlesnowman88 & FTMoo on 12 July 2018
     * IMPORTANT: If the refresh button is ever deleted and updates become completely automatic,
     *            THIS WILL NEED TO RETURN VOID
     */
    private void loadNotifications(String result) {
        if (result != null) {
            JSONObject json = null;
            try {
                json = new JSONObject(result).getJSONObject("notifications");
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
            if (json == null) {
                return;
            }
            JSONArray tempNames = json.names();

            int[] new_version = getVersionNumber(json, tempNames);
            int[] old_version = db.getJSONVersionNum();
            boolean[] update_flags = dataNeedsUpdate(old_version, new_version);
            if (update_flags[0]) { // if config file needs update
                qrAddress = db.getGeneral("url");
                loadAll = true;
                connection = checkConnection(qrAddress);
                loadInfoAndNavTitles();
                //new DataConnection(dataContext.get(), dataActivity.get(), action, db.getGeneral("url"), true, null, mainActivityRunnable).execute("");
            } else { // if config file does not need update
                if (update_flags [1]) { //if notifications json needs update
                    int[] new_notif_version = {old_version[0], new_version[1]};
                    db.replaceJSONVersionNum(new_notif_version);
                    ArrayList<Info> notifications = db.getNotifications();
                    db.deleteNotifications();
                    boolean isSameURL = (qrAddress.equals(old_qrAddress));
                    int num_notif_items = tempNames.length();
                    for (int i = 1; i < num_notif_items; i++) {
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
                                } else {temp.setOld();}

                                db.addNotification(temp);

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    private static boolean notificationIsNewOrChanged(ArrayList<Info> notifications, Info new_notif) {
        for (Info notif : notifications){
            if (notif.getId() == new_notif.getId()) { //if the notification currently exists
                //but something significant about it has changed
                if (!notif.getBody().equals(new_notif.getBody())
                        || !notif.getHeader().equals(new_notif.getHeader())
                        || !notif.getDate().equals(new_notif.getDate()))
                { return true; }
                else { //the notification exists and nothing significant has changed
                    return false;
                }
            }
        } //else, the notification ID was not found, so it must be new.
        return true;
    }

    //Gets the version number from the JSON
    private static int[] getVersionNumber(JSONObject qrJSON, JSONArray json_categories) {
        //VERSION NUMBER MUST REMAIN THE FIRST THING IN THE JSON'S GENERAL SECTION
        try {
            String version_string = qrJSON.getString(json_categories.getString(0)); //TODO: Make this flexible in ordering?
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

    //compares version numbers to see if configuration files need to update
    private static boolean[] dataNeedsUpdate(int[] old_version, int[] new_version) {
        boolean[] update_flags = {false, false};
        if (old_version[0] != new_version[0] || new_version[0]==0) {
            update_flags[0] = true;
        }
        if (old_version[1] != new_version[1] || new_version[1]==0) {
            update_flags[1] = true;
        }
        return update_flags;
    }

    /**
     * Loads all housing assignments
     * @param json, result of API query for housing
     */
    private void loadHousing(JSONObject json) {
        if (json == null) {
            return;
        }
        JSONArray tempNames = json.names();

        try {
            if (json.getString("nav").equals("null")) {
                return;
            }
            db.addNavigationTitles(json.getString("nav"), json.getString("icon"), "Housing");

        } catch (JSONException e) {
            e.printStackTrace();
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
            }
        }
    }

    /**
     * Loads all Prayer Partner teams
     * @param json, result of API query for prayer partners
     */
    private void loadPrayerPartners(JSONArray json) {
        if (json == null) {
            return;
        }

        try {
            if (json.getJSONObject(0).getString("nav").equals("null")) {
                return;
            }
            db.addNavigationTitles(json.getJSONObject(0).getString("nav"), json.getJSONObject(0).getString("icon"), "Prayer Partners");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        for (int i = 1; i < json.length(); i++) {
            try {
                JSONObject PrayerPartnerObj = json.getJSONObject(i);

                String students = PrayerPartnerObj.getString("students");
                // add the Contact Object to db
                db.addPrayerPartners(students);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Loads colors for theme and schedule
     * @param json, result of API query for theme
     */
    private void loadTheme(JSONArray json) {
        if (json == null) {
            return;
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
            }
        }
    }

    /**
     * Loads Information pages
     * @param json, result of API query for information pages
     */
    private void loadInformationalPage(JSONObject json) {
        if (json == null) {
            return;
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
            }
        }
    }

    /**
     * Send a get request from the url
     *
     * @param urlString, url for get request.
     * @return string results of the query.
     */
    private String GET(String urlString) {
        String response = null;
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
