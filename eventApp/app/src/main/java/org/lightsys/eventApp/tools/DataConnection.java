package org.lightsys.eventApp.tools;

import android.app.Activity;
import android.content.Intent;
import android.app.ProgressDialog;
import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.lightsys.eventApp.data.Info;
import org.lightsys.eventApp.views.MainActivity;
import org.lightsys.eventApp.R;
import org.lightsys.eventApp.data.ContactInfo;
import org.lightsys.eventApp.data.HousingInfo;
import org.lightsys.eventApp.data.ScheduleInfo;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
    private final String qrAddress;    //location of JSON file
    private final Context dataContext; // Context that the DataConnection was executed in
    private final Activity dataActivity;
    private ProgressDialog spinner;
    private String action;
    private final boolean loadAll; //specifies whether all info should be reloaded or only notifications
    private boolean connection;
    private String connectionResult;
    private CompletionInterface callback;

    private static final String RELOAD_PAGE = "reload_page";

    private static final String Tag = "DPS";

    public DataConnection(Context context, Activity activity, String action, String QR, boolean loadAll, CompletionInterface my_callback) {
        super();
        dataContext = context;
        dataActivity = activity;
        qrAddress = QR;
        this.callback = my_callback;
        this.loadAll = loadAll;
        this.action = action;
        this.db = new LocalDB(dataContext);
        Log.d(TAG, "DataConnection: " + qrAddress);
        if (activity != null) {
            spinner = new ProgressDialog(dataContext, R.style.MySpinnerStyle);
        }
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
                ((MainActivity)dataActivity).gatherData(true);
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

        // Dismiss spinner to show data retrieval is done
        if (dataActivity != null) {
            spinner.dismiss();
        }
        if (dataContext.getClass() == MainActivity.class && connection && !action.equals("auto_update")) {
            Toast.makeText(dataContext, "data successfully imported", Toast.LENGTH_SHORT).show();
            if (action.equals("new")) {
                try {
                    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    Ringtone r = RingtoneManager.getRingtone(dataContext, notification);
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
        LocalBroadcastManager.getInstance(dataContext)
                .sendBroadcast(reloadIntent);
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
                dataActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(dataContext, "Server connection failed: invalid web address", Toast.LENGTH_LONG).show();
                    }
                });
                return false;
            }
            else if (test.equals("") || test.equals("Access Not Permitted")) {
                dataActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(dataContext, "Server connection failed", Toast.LENGTH_LONG).show();
                    }
                });
                return false;
            } else if (test.contains("<H1>Unauthorized</H1>")) {
                dataActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(dataContext, "Username/password invalid", Toast.LENGTH_LONG).show();
                    }
                });
                return false;
            } else if (test.contains("404 Not Found")) {
                dataActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(dataContext, "Invalid User ID", Toast.LENGTH_LONG).show();
                    }
                });
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            // GET function throws an Exception if server not found
            if(e.getClass()==SocketTimeoutException.class){
                dataActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(dataContext, "Server connection timed out", Toast.LENGTH_LONG).show();
                    }
                });
            }else if(e.getClass()!=SSLHandshakeException.class) {
                dataActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(dataContext, "Server connection failed", Toast.LENGTH_LONG).show();
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
     */
    private void DataPull()  {
        db = new LocalDB(dataContext);

        //set spinner as app collects data
        if (dataActivity != null) {
            spinner.setMessage("Gathering event info...");
            dataActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    spinner.setIndeterminate(true);
                    spinner.setCancelable(false);
                    spinner.show();
                }
            });
        }

        connection = checkConnection(qrAddress);

        //if connection error occurred, cancel spinner
        if (!connection && dataActivity != null){
            spinner.dismiss();
        }

        if (connection) {
            Log.i(Tag, "pulling data");

            try {
                if(loadAll) {
                    db.clear();
                    db.addGeneral("url",qrAddress);
                    db.addNavigationTitles("Notifications", "ic_bell");
                    loadEventInfo(connectionResult);
                    db.addNavigationTitles("About", "ic_info");

                    if (action.equals("new")){
                        connection = checkConnection(db.getGeneral("notifications_url"));
                        loadNotifications(connectionResult);
                    }

                    //add about page
                    db.addInformationPage(new Info("LightSys Events (Android App)","Copyright © 2017-2018 LightSys Technology Services, Inc.  This app was created for the use of distributing event information for ministry events.\n\nThis app's source code is also available under the GPLv3 open-source license at:\nhttps://github.com/LightSys/eventApp"), "About");
                    db.addInformationPage(new Info("Open Source","This app includes the following open source libraries:"), "About");
                    db.addInformationPage(new Info("Mobile Vision Barcode Scanner","Copyright (c) 2016 Nosakhare Belvi\nLicense: MIT License\nWebsite: https://github.com/KingsMentor/MobileVisionBarcodeScanner"), "About");
                }else{
                    loadNotifications(connectionResult);
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (e.getClass().equals(SocketTimeoutException.class)) {
                    Toast.makeText(dataContext, "Server connection timed out", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(dataContext, "Server connection failed", Toast.LENGTH_LONG).show();
                }
                return;
                //to here
            }

            // add new timestamp
            db.addTimeStamp("" + Calendar.getInstance().getTimeInMillis());

            db.close();
        }
    }

    /**
     * Loads Event info
     * @param result, result of the API query for the contact info
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
            try {
                //separate JSON object and get individual parts to be stored in Local Database
                loadContactPage(json.getJSONObject("contact_page"));
                loadSchedule(json.getJSONObject("schedule"));
                loadHousing(json.getJSONObject("housing"));
                loadPrayerPartners(json.getJSONArray("prayer_partners"));
                loadInformationalPage(json.getJSONObject("information_page"));
                loadGeneralInfo(json.getJSONObject("general"));
                loadTheme(json.getJSONArray("theme"));
                loadContacts(json.getJSONObject("contacts"));

            } catch (JSONException e) {
                e.printStackTrace();
            }
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

        for (int i = 0; i < tempNames.length(); i++) {
            try {
                //@id signals a new object, but contains no information on that line
                if (!tempNames.getString(i).equals("@id")) {
                    JSONObject ContactObj = json.getJSONObject(tempNames.getString(i));

                    String contact_name = tempNames.getString(i);
                    String contact_address = (ContactObj.getString("address").equals("null")) ? null : ContactObj.getString("address");
                    String contact_phone = (ContactObj.getString("phone").equals("null")) ? null : ContactObj.getString("phone");

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
            db.addNavigationTitles(json.getString("nav"), json.getString("icon"));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < tempNames.length(); i++) {
            try {
                //@id signals a new object, but contains no information on that line
                if (!tempNames.getString(i).equals("@id") && !tempNames.get(i).equals("nav") && !tempNames.get(i).equals("icon")) {
                    JSONObject ContactObj = json.getJSONObject(tempNames.getString(i));

                    // add the Contact Page Object to db
                    Info temp = new Info();
                    temp.setHeader(ContactObj.getString("header"));
                    temp.setBody(ContactObj.getString("content"));
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
            db.addNavigationTitles(json.getString("nav"), json.getString("icon"));

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
     */
    private void loadGeneralInfo(JSONObject json) {
        if (json == null) {
            return;
        }
        JSONArray tempGeneral = json.names();

        for (int i = 0; i < tempGeneral.length(); i++) {
            try {
                //@id signals a new object, but contains no information on that line
                if (!tempGeneral.getString(i).equals("@id")) {
                    //only loads 'refresh' info if it is not already specified by the user
                    if (!tempGeneral.getString(i).equals("refresh") || db.getGeneral("refresh") == null) {
                        db.addGeneral(tempGeneral.getString(i), json.getString(tempGeneral.getString(i)));
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
     */
    private void loadNotifications(String result) {
        ArrayList<Integer> currentNotifications = db.getCurrentNotifications();

        db.deleteNotifications();
        boolean loadData = false;

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

            for (int i = 0; i < tempNames.length(); i++) {
                try {
                    //@id signals a new object, but contains no information on that line
                    if (!tempNames.getString(i).equals("@id") && !tempNames.get(i).equals("nav") && !tempNames.get(i).equals("icon")) {
                        JSONObject notificationObj = json.getJSONObject(tempNames.getString(i));
                        Info temp = new Info();
                        temp.setId(Integer.parseInt(tempNames.getString(i)));
                        temp.setHeader(notificationObj.getString("title"));
                        temp.setBody(notificationObj.getString("body"));
                        temp.setDate(notificationObj.getString("date"));
                        if (!currentNotifications.contains(Integer.parseInt(tempNames.getString(i)))) {
                            // This one is new
                            temp.setNew();
                        }

                        db.addNotification(temp);

                        //if notification refresh is true, has not already been loaded, and isn't a new event, reload all app data
                        if (notificationObj.getBoolean("refresh") && !action.equals("new") &&!currentNotifications.contains(Integer.parseInt(tempNames.getString(i)))) {
                            loadData = true;
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if(loadData){
                new DataConnection(dataContext, dataActivity, action, db.getGeneral("url"), true, null).execute("");

            }
        }

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
            db.addNavigationTitles(json.getString("nav"), json.getString("icon"));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < tempNames.length(); i++) {
            try {
                //@id signals a new object, but contains no information on that line
                if (!tempNames.getString(i).equals("@id") && !tempNames.get(i).equals("nav") && !tempNames.get(i).equals("icon")) {
                    JSONObject HousingObj = json.getJSONObject(tempNames.getString(i));

                    String host_name = tempNames.getString(i);
                    String driver = HousingObj.getString("driver");
                    String students = HousingObj.getString("students");

                    // add the Contact Object to db
                    HousingInfo temp = new HousingInfo();
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
            db.addNavigationTitles(json.getJSONObject(0).getString("nav"), json.getJSONObject(0).getString("icon"));

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

                    String title = InfoArray.getJSONObject(0).getString("nav");

                    db.addNavigationTitles(title, InfoArray.getJSONObject(0).getString("icon"));

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
