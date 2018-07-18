package org.lightsys.eventApp.tools;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.lightsys.eventApp.data.ContactInfo;
import org.lightsys.eventApp.data.HousingInfo;
import org.lightsys.eventApp.data.Info;
import org.lightsys.eventApp.data.ScheduleInfo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by otter57 on 3/30/17.
 * Modified by Littlesnowman88 7 June 2018
 * SQLite Database to store event information
 */

public class LocalDB extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 12;
    private static final String DATABASE_NAME = "SBCaT.db";
    //SCANNED EVENTS TABLE
    private static final String TABLE_SCANNED_EVENTS = "scanned_events";
    private static final String COLUMN_URL = "url";
    private static final String COLUMN_EVENT_NAME = "event_name";
    //VERSION NUM TABLE
    private static final String TABLE_VERSION_NUM = "version_num";
    private static final String COLUMN_CONFIG_VER = "config_ver";
    private static final String COLUMN_NOTIF_VER = "notif_ver";
    //GENERAL INFO TABLE
    private static final String TABLE_GENERAL_INFO = "general_info";
    private static final String COLUMN_TYPE = "info_type";
    //TIME_STAMP
    private static final String TABLE_TIMESTAMP = "timestamp";
    private static final String COLUMN_DATE = "date";
    //CONTACTS TABLE
    private static final String TABLE_CONTACTS = "contacts";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_ADDRESS = "address";
    private static final String COLUMN_PHONE = "phone";
    //SCHEDULE TABLE
    private static final String TABLE_SCHEDULE = "schedule";
    private static final String COLUMN_DAY = "sch_day";
    private static final String COLUMN_TIME_START = "sch_time_start";
    private static final String COLUMN_TIME_LENGTH = "sch_time_length";
    private static final String COLUMN_DESC = "sch_desc";
    private static final String COLUMN_CATEGORY = "sch_category";
    //SCHEDULE VARIABLES
    private static ArrayList<String> allDays;
    private static ArrayList<Integer> scheduleTimeRange;
    //SCHEDULE PREFERENCES
    private SharedPreferences sharedPreferences;
    //INFORMATION PAGE TABLE
    private static final String TABLE_INFORMATION_PAGE = "information_page";
    private static final String COLUMN_HEADER = "header";
    private static final String COLUMN_INFO = "info";
    private static final String COLUMN_PAGE = "page";
    //ABOUT PAGE TABLE
    // uses the same columns as the information page
    private static final String TABLE_ABOUT_PAGE = "about_page";
    //HOUSING TABLE
    private static final String TABLE_HOUSING = "housing";
    private static final String COLUMN_DRIVER = "driver";
    private static final String COLUMN_STUDENTS = "students";
    //PRAYER PARTNER TABLE
    private static final String TABLE_PRAYER_PARTNERS = "prayer_partners";
    //NOTIFICATIONS TABLE
    private static final String  TABLE_NOTIFICATIONS = "notifications";
    private static final String COLUMN_NEW = "isnew";
    private static final String COLUMN_ID = "id";
    //NAVIGATION TITLES TABLE
    private static final String TABLE_NAVIGATION_TITLES = "navigation_titles";
    private static final String COLUMN_ICON = "icon";
    private static final String COLUMN_NAV_ID = "nav_id";
    //COLOR TABLE
    private static final String TABLE_THEME = "theme";
    private static final String COLUMN_HEX_CODE = "hex_code";
    //CONTACT PAGE
    private static final String TABLE_CONTACT_PAGE = "contact_page";


	/* ************************* Creation of Database and Tables ************************* */

    /**
     * Creates an instance of the database
     */
    public LocalDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Creates all the tables used to store accounts and donor information
     * Called only when database is first created
     */
    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_TABLE_SCANNED_EVENTS = "CREATE TABLE " + TABLE_SCANNED_EVENTS + "("
                + COLUMN_URL + " TEXT," + COLUMN_EVENT_NAME + " TEXT)";
        db.execSQL(CREATE_TABLE_SCANNED_EVENTS);

        String CREATE_TABLE_VERSION_NUM = "CREATE TABLE " + TABLE_VERSION_NUM + "("
                + COLUMN_CONFIG_VER+ " INTEGER," + COLUMN_NOTIF_VER + " INTEGER)";
        db.execSQL(CREATE_TABLE_VERSION_NUM);

        String CREATE_TABLE_GENERAL_INFO = "CREATE TABLE " + TABLE_GENERAL_INFO + "("
                + COLUMN_TYPE + " TEXT," + COLUMN_INFO + " TEXT)";
        db.execSQL(CREATE_TABLE_GENERAL_INFO);

        String CREATE_TABLE_TIMESTAMP = "CREATE TABLE " + TABLE_TIMESTAMP + "("
                + COLUMN_DATE + " TEXT)";
        db.execSQL(CREATE_TABLE_TIMESTAMP);

        String CREATE_TABLE_CONTACTS = "CREATE TABLE " + TABLE_CONTACTS + "("
                + COLUMN_NAME + " TEXT," + COLUMN_ADDRESS + " TEXT," + COLUMN_PHONE + " TEXT)";
        db.execSQL(CREATE_TABLE_CONTACTS);

        String CREATE_TABLE_SCHEDULE = "CREATE TABLE " + TABLE_SCHEDULE + "("
                + COLUMN_DAY + " TEXT," + COLUMN_TIME_START + " INTEGER," + COLUMN_TIME_LENGTH + " INTEGER,"
                + COLUMN_DESC + " TEXT," + COLUMN_NAME + " TEXT," + COLUMN_CATEGORY + " TEXT)";
        db.execSQL(CREATE_TABLE_SCHEDULE);

        String CREATE_TABLE_INFORMATION_PAGE = "CREATE TABLE " + TABLE_INFORMATION_PAGE + "("
                + COLUMN_HEADER + " TEXT," + COLUMN_INFO + " TEXT," + COLUMN_PAGE + " TEXT)";
        db.execSQL(CREATE_TABLE_INFORMATION_PAGE);

        String CREATE_TABLE_ABOUT_PAGE = "CREATE TABLE " + TABLE_ABOUT_PAGE + "(" + COLUMN_HEADER
                + " TEXT," + COLUMN_INFO + " TEXT," + COLUMN_PAGE + " TEXT)";
        db.execSQL(CREATE_TABLE_ABOUT_PAGE);

        String CREATE_TABLE_NOTIFICATIONS = "CREATE TABLE " + TABLE_NOTIFICATIONS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY," + COLUMN_HEADER + " TEXT," + COLUMN_INFO + " TEXT," + COLUMN_DATE + " TEXT," + COLUMN_NEW + " INTEGER)";
        db.execSQL(CREATE_TABLE_NOTIFICATIONS);

        String CREATE_TABLE_HOUSING = "CREATE TABLE " + TABLE_HOUSING + "("
                + COLUMN_NAME + " TEXT," + COLUMN_DRIVER + " TEXT," + COLUMN_STUDENTS + " TEXT)";
        db.execSQL(CREATE_TABLE_HOUSING);

        String CREATE_TABLE_PRAYER_PARTNERS = "CREATE TABLE " + TABLE_PRAYER_PARTNERS + "("
                + COLUMN_STUDENTS + " TEXT)";
        db.execSQL(CREATE_TABLE_PRAYER_PARTNERS);

        String CREATE_TABLE_NAVIGATION_TITLES = "CREATE TABLE " + TABLE_NAVIGATION_TITLES + "("
                + COLUMN_NAME + " TEXT," + COLUMN_ICON + " TEXT," + COLUMN_NAV_ID + " TEXT)";
        db.execSQL(CREATE_TABLE_NAVIGATION_TITLES);

        String CREATE_TABLE_THEME = "CREATE TABLE " + TABLE_THEME + "("
                + COLUMN_NAME + " TEXT," + COLUMN_HEX_CODE + " TEXT)";
        db.execSQL(CREATE_TABLE_THEME);

        String CREATE_TABLE_CONTACT_PAGE = "CREATE TABLE " + TABLE_CONTACT_PAGE + "("
                + COLUMN_HEADER + " TEXT," + COLUMN_INFO + " TEXT," + COLUMN_ID + " INTEGER)";
        db.execSQL(CREATE_TABLE_CONTACT_PAGE);
    }

    /**
     * Since the SQLite Database is meant to stay local, it will never use this call
     * and should never use this call, or else all accounts will be erased.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //nothing. The database model won't be changed.
    }

	/* ************************* Clear Queries ************************* */

	//delete event data
	public void clear(){
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_CONTACTS, null, null);
        db.delete(TABLE_SCHEDULE, null, null);
        db.delete(TABLE_INFORMATION_PAGE, null, null);
        db.delete(TABLE_HOUSING, null, null);
        db.delete(TABLE_PRAYER_PARTNERS, null, null);
        db.delete(TABLE_TIMESTAMP, null, null);
        db.delete(TABLE_GENERAL_INFO, null, null);
        db.delete(TABLE_NAVIGATION_TITLES, null, null);
        db.delete(TABLE_THEME, null, null);
        db.delete(TABLE_CONTACT_PAGE, null, null);
        db.delete(TABLE_ABOUT_PAGE, null, null);
    }

    //delete all notifications
    public void deleteNotifications(){
        SQLiteDatabase db = this.getReadableDatabase();
        db.delete(TABLE_NOTIFICATIONS, null, null);
        db.close();
    }

	/* ************************* Add Queries ************************* */

    /**
     * Adds a timestamp to the database
     * @param date, date in standard millisecond form to be added
     */
    public void addTimeStamp(String date){
        ContentValues values = new ContentValues();
        values.put(COLUMN_DATE, date);

        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TIMESTAMP, null, null);
        db.insert(TABLE_TIMESTAMP, null, values);
        db.close();
    }

    /**
     * Add theme colors into the database
     * @param name, name of color
     * @param color, Hex code
     */
    public void addThemeColor (String name, String color){
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_HEX_CODE, color);

        SQLiteDatabase db = this.getWritableDatabase();
        db.insert(TABLE_THEME, null, values);
        db.close();
    }

    /**
     * Add contact page info to database
     * @param contactInfo, contact info object to be added
     * id 0=textView, 1=listView
     */
    public void addContactPage (Info contactInfo){
        ContentValues values = new ContentValues();
        values.put(COLUMN_HEADER, contactInfo.getHeader());
        values.put(COLUMN_INFO, contactInfo.getBody());
        values.put(COLUMN_ID, contactInfo.getId());

        SQLiteDatabase db = this.getWritableDatabase();
        db.insert(TABLE_CONTACT_PAGE, null, values);
        db.close();
    }

    /**
     *
     * @param url, the url of the event
     * @param name, the name of the event
     */
    public void addEvent (String url, String name){
        ContentValues values = new ContentValues();
        values.put(COLUMN_URL, url);
        values.put(COLUMN_EVENT_NAME, name);

        SQLiteDatabase db = this.getWritableDatabase();
        db.insert(TABLE_SCANNED_EVENTS, null, values);
        db.close();
    }

    /**
     * This will add a version number and url to the database
     * @param version, the int array of version number for the config url and notification url
     */
    private void addVersionNum(int[] version){
        ContentValues values = new ContentValues();
        values.put(COLUMN_CONFIG_VER, version[0]);
        values.put(COLUMN_NOTIF_VER, version[1]);

        SQLiteDatabase db = this.getWritableDatabase();
        db.insert(TABLE_VERSION_NUM, null, values);
        db.close();
    }

    /**
     * @param type, name information is stored under
     * @param content, information to be stored
     * adds code-a-thon general info (url, refresh rate, refresh expire, time zone, welcome message, notification url, logo)
     */
    public void addGeneral(String type, String content) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_TYPE, type);
        values.put(COLUMN_INFO, content);

        SQLiteDatabase db = this.getWritableDatabase();
        db.insert(TABLE_GENERAL_INFO, null, values);
        db.close();
    }

    /**
     * adds titles for navigation
     * @param title, navigation name
     * @param icon, name of icon
     */
    public void addNavigationTitles(String title, String icon, String navID) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, title);
        values.put(COLUMN_ICON, icon);
        values.put(COLUMN_NAV_ID, navID);

        SQLiteDatabase db = this.getWritableDatabase();
        db.insert(TABLE_NAVIGATION_TITLES, null, values);
        db.close();
    }

    /**
     * adds contact info
     * @param contact, contact info to be stored
     */
    public void addContact(ContactInfo contact) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, contact.getName());
        values.put(COLUMN_ADDRESS, contact.getAddress());
        values.put(COLUMN_PHONE, contact.getPhone());

        SQLiteDatabase db = this.getWritableDatabase();
        db.insert(TABLE_CONTACTS, null, values);
        db.close();
    }

    /**
     * adds schedule info
     * if schedule item occurs multiple days separate with comma
     */
    public void addSchedule(ScheduleInfo scheduleInfo) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_DAY, scheduleInfo.getDay());
        values.put(COLUMN_TIME_START, scheduleInfo.getTimeStart());
        values.put(COLUMN_TIME_LENGTH, scheduleInfo.getTimeLength());
        values.put(COLUMN_DESC, scheduleInfo.getDesc());
        values.put(COLUMN_NAME, scheduleInfo.getLocationName());
        values.put(COLUMN_CATEGORY, scheduleInfo.getCategory());

        SQLiteDatabase db = this.getWritableDatabase();
        db.insert(TABLE_SCHEDULE, null, values);
        db.close();
    }

    /**
     * adds informational page info
     */
    public void addInformationPage(Info informationPage, String Page) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_HEADER, informationPage.getHeader());
        values.put(COLUMN_INFO, informationPage.getBody());
        values.put(COLUMN_PAGE, Page);

        SQLiteDatabase db = this.getWritableDatabase();
        db.insert(TABLE_INFORMATION_PAGE, null, values);
        db.close();
    }

    /**
     * adds about page information
     */
    public void addAboutPage(Info aboutPage, String Page){
        ContentValues values = new ContentValues();
        values.put(COLUMN_HEADER, aboutPage.getHeader());
        values.put(COLUMN_INFO, aboutPage.getBody());
        values.put(COLUMN_PAGE, Page);

        SQLiteDatabase db = this.getWritableDatabase();
        db.insert(TABLE_ABOUT_PAGE,null,values);
        db.close();
    }

    /**
     * adds Notifications info
     */
    public void addNotification(Info notification) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_HEADER, notification.getHeader());
        values.put(COLUMN_INFO, notification.getBody());
        values.put(COLUMN_DATE, notification.getDate());
        values.put(COLUMN_ID, notification.getId());
        values.put(COLUMN_NEW, notification.getNew()?1:0);

        SQLiteDatabase db = this.getWritableDatabase();
        db.insert(TABLE_NOTIFICATIONS, null, values);
        db.close();
    }

    /**
     * adds housing info
     */
    public void addHousing(HousingInfo housing) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, housing.getName());
        values.put(COLUMN_DRIVER, housing.getDriver());
        values.put(COLUMN_STUDENTS, housing.getStudents());

        SQLiteDatabase db = this.getWritableDatabase();
        db.insert(TABLE_HOUSING, null, values);
        db.close();
    }

    /**
     * adds prayer partner info
     */
    public void addPrayerPartners(String students) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_STUDENTS, students);

        SQLiteDatabase db = this.getWritableDatabase();
        db.insert(TABLE_PRAYER_PARTNERS, null, values);
        db.close();
    }

	/* ************************* Get Queries ************************* */

    /**
     * get scanned event
     *
     * @param url, title under which the name is stored
     * @return event name
     */
    public String getEvent(String url){
        String event=null;
        String queryString = "SELECT " + COLUMN_EVENT_NAME + " FROM " + TABLE_SCANNED_EVENTS
                +" WHERE " + COLUMN_URL + " = " + "?";
        SQLiteDatabase db = this.getReadableDatabase();
        String[] selectionArgs = {url};
        Cursor c = db.rawQuery(queryString, selectionArgs);

        while (c.moveToNext()) {
            event = c.getString(0);

        }
        c.close();
        db.close();
        return event;
    }

    /**
     *
     * @return returns and ArrayList with the name and url of all events in the database
     */
    public ArrayList<String[]> getAllEvents() {
        ArrayList<String[]> allEvents = new ArrayList<>();
        String urlQuery = "SELECT * FROM " + TABLE_SCANNED_EVENTS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(urlQuery,null);

        while (c.moveToNext()) {
            String[] values = {c.getString(c.getColumnIndex(COLUMN_EVENT_NAME)),c.getString(c.getColumnIndex(COLUMN_URL))};
            allEvents.add(0,values);
        }
        return allEvents;
    }

    /**
     *
     * @return the currently saved version number from the database
     */
    public int[] getVersionNum(){
        int[] version_num = {0,0};
        String queryString = "SELECT * FROM " + TABLE_VERSION_NUM;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(queryString,null);

        while (c.moveToNext()){
            version_num[0] = c.getInt(0);
            version_num[1] = c.getInt(1);
        }
        c.close();
        db.close();
        return version_num;
    }

    /**
     * get general info
     * modified by Littlesnowman88 to protect against special characters causing problems
     * @param type, title under which info is stored
     * @return general info (year, url, logo, etc.)
     */
    public String getGeneral(String type) {
        String general=null;
        String queryString = "SELECT " + COLUMN_INFO + " FROM " + TABLE_GENERAL_INFO + " WHERE "
                + COLUMN_TYPE + " = " + "?";
        SQLiteDatabase db = this.getReadableDatabase();
        String[] selectionArgs = {type};
        Cursor c = db.rawQuery(queryString, selectionArgs);

        while (c.moveToNext()) {
            general = c.getString(0);

        }
        c.close();
        db.close();
        return general;
    }

    /**
     * @return color
     * modified by Littlesnowman88 to protect against special characters causing problems
     */
    public String getThemeColor(String name) {
        String color="#000000";
        String queryString = "SELECT " + COLUMN_HEX_CODE + " FROM " + TABLE_THEME + " WHERE "
                + COLUMN_NAME + " = " + "?";

        SQLiteDatabase db = this.getReadableDatabase();
        String[] selectionArgs = {name};
        Cursor c = db.rawQuery(queryString, selectionArgs);

        while (c.moveToNext()) {
            color = c.getString(0);

        }
        c.close();
        db.close();
        return color;
    }

    /**
     * @return navigation titles
     */
    public ArrayList<Info> getNavigationTitles() {
        ArrayList<Info> titles = new ArrayList<>();
        String queryString = "SELECT * FROM " + TABLE_NAVIGATION_TITLES;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(queryString, null);

        while (c.moveToNext()) {
            Info temp = new Info();

            temp.setHeader(c.getString(0));
            temp.setBody(c.getString(1));
            temp.setName(c.getString(2));
//            Log.d("HERE", "getNavigationTitles: " + temp.getBody() + temp.getHeader() + temp.getName());
            titles.add(temp);

        }

        c.close();
        db.close();
        return titles;
    }

    /**
     * @return Contacts
     * modified by Littlesnowman88 to protect against special characters causing problems
     */
    public ContactInfo getContactByName(String name) {
        ContactInfo contact = new ContactInfo();
        String queryString = "SELECT * FROM " + TABLE_CONTACTS + " WHERE " + COLUMN_NAME + " = " + "?";
        SQLiteDatabase db = this.getReadableDatabase();
        String[] selectionArgs = {name};
        Cursor c = db.rawQuery(queryString, selectionArgs);

        while (c.moveToNext()) {
            contact.setName(c.getString(0));
            contact.setAddress(c.getString(1));
            contact.setPhone(c.getString(2));

        }
        c.close();
        db.close();
        return contact;
    }

    /** a setter method for shared preferences. Used by ScheduleView.java
     * Created by Littlesnowman88
     * shared preferences used when reading schedule info getFullSchedule
     */
    public void setSharedPreferences(SharedPreferences sharedPrefs) {
        sharedPreferences = sharedPrefs;
    }

    /**
     * getFullSchedule assembles an ArrayList with a properly ordered schedule, taking time zone adjustment into account
     * Created by Littlesnowman88 19 June 2018
     * @return: fullSchedule, an ArrayList<ScheduleInfo> of chronologically ordered activities, taking time zone adjustment into account.
     */
    /** IMPORTANT NOTE BY LITTLESNOWMAN88:
     *  getFullSchedule and its subsequently called methods DO NOT HANDLE Schedule item lengths
     *  greater than 24 hours as of this version.
     */
    public ArrayList<ScheduleInfo> getFullSchedule() {
        ArrayList<String> days = determineUniqueDays(getJSONDays());
        ArrayList<ScheduleInfo> schedule;
        if (sharedPreferences.getString("selected_time_setting", "").equals("on-site")) {
            schedule = getDailySchedule(days);
        } else {
            schedule = getAdjustedDailySchedule(days);
        }
        allDays = days;
        scheduleTimeRange = getScheduleEdges(schedule);
        return schedule;
    }

    /** getDailySchedule returns daily schedule info for an on-site timezone
     * * Now creates a schedule for the entire event, not just one day.
     * Created/Refactored by Littlesnowman88 19 June 2018
     * Created by: Littlesnowman88 20 June 2018

     * @param days, an array of string dates--formatted MM/dd/yyyy
     * @return a chronologically ordered ArrayList with ScheduleInfo items (events) in it.
     */
    private ArrayList<ScheduleInfo> getDailySchedule(ArrayList<String> days) {
        ArrayList<ScheduleInfo> schedule = new ArrayList<>();
        SQLiteDatabase db;
        String queryString;
        String today, tomorrow;
        int timeStart, length, timeEnd;
        Cursor c;
        //make a deep copy of days so that queryString indexing does not get messed up as days changes below.
        ArrayList<String> daysCopy = new ArrayList<>();
        for (String day : days) {
            daysCopy.add(day);
        }
        int daysCopySize = daysCopy.size();
        for (int d=0; d < daysCopySize; d++) {
            db = this.getReadableDatabase();
            queryString = "SELECT * FROM " + TABLE_SCHEDULE + " WHERE " + COLUMN_DAY
                    + " LIKE \"%" + daysCopy.get(d) + "%\"" + " ORDER BY " + COLUMN_TIME_START;
            c = db.rawQuery(queryString, null);
            while (c.moveToNext()) {
                today = c.getString(0);
                timeStart = c.getInt(1);
                length = c.getInt(2);
                timeEnd = TimeAdjuster.addTimes(TimeAdjuster.minutesToTime(length), timeStart); //can result in < 0 or > 2359
                if (timeEnd > 2400) { //if length causes overlap with next day
                    //save today's "leftover" portion (> 0 minutes in length)
                    int today_length = TimeAdjuster.timeToMinutes(TimeAdjuster.addTimes(2400, -1 * timeStart)); //today_length = 2400 - time start
                    saveScheduleItem(c, schedule, c.getString(0), timeStart, today_length);
                    int tomorrow_length = length - today_length;
                    // if today is not the final day, use the next day
                    if (days.contains(today) && (!today.equals(days.get(days.size() - 1)))) {
                        //save the tomorrow part
                        tomorrow = days.get(days.indexOf(today) + 1);
                        saveScheduleItem(c, schedule, tomorrow, 0, tomorrow_length);
                    } else if (today.equals(days.get(days.size() - 1))) { //otherwise, if today is the last day, create and use tomorrow.
                        //save the tomorrow part
                        tomorrow = createTomorrow(today);
                        days.add(tomorrow);
                        saveScheduleItem(c, schedule, tomorrow, 0, tomorrow_length);
                    }
                }
                /* else if there is no day shift */
                 else { // if the time zone adjustment is still contained within the same day as the event
                saveScheduleItem(c, schedule, today, timeStart, length);
                }
            }
            c.close();
            db.close();
        }
        return schedule;
    }

    /** getAdjustedDailySchedule returns daily schedule for remote and custom-zone timezones
     * Created by Littlesnowman88 12 June 2018
     * Last Modified: 19 June 2018 (by Littlesnowman88)
     * @param days, an ArrayList<String> of string dates--formatted MM/dd/yyyy
     * @return an ArrayList with ScheduleInfo items (event) in it
     */
    private ArrayList<ScheduleInfo> getAdjustedDailySchedule(ArrayList<String> days) {
        ArrayList<ScheduleInfo> schedule = new ArrayList<>();
        int timeStart, timeLength, adjustedTimeStart, adjustedTimeEnd, time_zone_difference;
        TimeZone on_site_time_zone = TimeZone.getTimeZone(getGeneral("time_zone")); //TODO: taking away from getGeneral and to chosen location.
        TimeZone selected_time_zone = TimeZone.getTimeZone(sharedPreferences.getString("time_zone", getGeneral("time_zone"))); //TODO: taking away from getGeneral and to chosen location.
        String today, yesterday, tomorrow;
        SQLiteDatabase db;
        String queryString;
        Cursor c;
        int daysSize = days.size();

        //make a deep copy of days so that queryString indexing does not get messed up as days changes below.
        ArrayList<String> daysCopy = new ArrayList<>();
        for (String day : days) {
            daysCopy.add(day);
        }

        for (int i = 0; i < daysSize; i++) {
            db = this.getReadableDatabase();
            queryString = "SELECT * FROM " + TABLE_SCHEDULE + " WHERE " + COLUMN_DAY
                    + " LIKE \"%" + daysCopy.get(i) + "%\"" + " ORDER BY " + COLUMN_TIME_START;
            c = db.rawQuery(queryString, null);

            while(c.moveToNext()) {
                today = c.getString(0);
                timeStart = c.getInt(1);
                timeLength = c.getInt(2);

                time_zone_difference = TimeAdjuster.getTimeZoneDifference(on_site_time_zone, selected_time_zone, today);

                adjustedTimeStart = TimeAdjuster.addTimes(timeStart, time_zone_difference); //can result in < 0 or > 2359
                adjustedTimeEnd = TimeAdjuster.addTimes(TimeAdjuster.minutesToTime(timeLength), adjustedTimeStart); //can result in < 0 or > 2359

                /* adjust events with the time zone difference, taking into account potential day shifts */

                /* if there is a previous_day shift */
                if (adjustedTimeStart < 0) { //if the time zone adjustment causes overlap with previous day
                    if (adjustedTimeEnd > 0) { // if the event overlaps midnight on both sides (midnight exclusive)
                        //First save yesterday's portion
                        int today_length = TimeAdjuster.timeToMinutes(adjustedTimeEnd); //today_length = 0 + time_end
                        int yesterday_length = timeLength - today_length;
                        // if today is not the first day, use the previous day
                        if (days.contains(today) && (! today.equals(days.get(0)))) {
                            //save the yesterday part
                            yesterday = days.get(days.indexOf(today) - 1);
                            saveScheduleItem(c, schedule, yesterday, TimeAdjuster.addTimes(2400, adjustedTimeStart), yesterday_length); //ends at 2400
                        } else if (today.equals(days.get(0))) { //otherwise, if today is the first day, create and use yesterday.
                            //save the yesterday part
                            yesterday = createYesterday(today);
                            days.add(0, yesterday);
                            saveScheduleItem(c, schedule, yesterday, TimeAdjuster.addTimes(2400, adjustedTimeStart), yesterday_length); //ends at 2400
                        }
                        //then save today's portion (> 0 minutes in length)
                        saveScheduleItem(c, schedule, c.getString(0), 0, today_length);

                    } else { //if the time zone adjustment moves an event completely into another day (midnight inclusive)
                        //if today is not the first day, use the previous day
                        if (days.contains(today) && ( ! today.equals(days.get(0)))) {
                            //save the event in yesterday's date
                            yesterday = days.get(days.indexOf(today) - 1);
                            saveScheduleItem(c, schedule, yesterday, TimeAdjuster.adjustTime(adjustedTimeStart, 0), timeLength);
                        } else if (today.equals(days.get(0))) {
                            //save the yesterday part
                            yesterday = createYesterday(today);
                            days.add(0, yesterday);
                            saveScheduleItem(c, schedule, yesterday, TimeAdjuster.adjustTime(adjustedTimeStart, 0), timeLength);
                        }
                    }
                }
                /* else if there is a next_day shift */
                else if (adjustedTimeEnd > 2400) { //else if time zone adjustment causes overlap with next day)
                    if (adjustedTimeStart < 2400) { // if the event overlaps midnight on both sides (midnight exclusive)
                        //save today's "leftover" portion (> 0 minutes in length)
                        int today_length = TimeAdjuster.timeToMinutes(TimeAdjuster.addTimes(2400, -1 * adjustedTimeStart)); //today_length = 2400 - time start
                        saveScheduleItem(c, schedule, c.getString(0), adjustedTimeStart, today_length);
                        int tomorrow_length = timeLength - today_length;
                        // if today is not the final day, use the next day
                        if (days.contains(today) && (! today.equals(days.get(days.size() - 1)))) {
                            //save the tomorrow part
                            tomorrow = days.get(days.indexOf(today) + 1);
                            saveScheduleItem(c, schedule, tomorrow, 0, tomorrow_length);
                        } else if (today.equals(days.get(days.size() - 1))) { //otherwise, if today is the last day, create and use tomorrow.
                            //save the tomorrow part
                            tomorrow = createTomorrow(today);
                            days.add(tomorrow);
                            saveScheduleItem(c, schedule, tomorrow, 0, tomorrow_length);
                        }
                    } else { //if the time zone adjustment moves an event completely into another day (and not midnight of "tomorrow")
                        // if today is not the final day, use the next day
                        if (days.contains(today) && (! today.equals(days.get(days.size() - 1)))) {
                            //save the event in tomorrow's date
                            tomorrow = days.get(days.indexOf(today) + 1);
                            saveScheduleItem(c, schedule, tomorrow, TimeAdjuster.adjustTime(adjustedTimeStart, 0), timeLength);
                        } else if (today.equals( days.get(days.size() - 1))) { //otherwise, if today is the last day, create and use tomorrow.
                            //save the tomorrow part
                            tomorrow = createTomorrow(today);
                            days.add(tomorrow);
                            saveScheduleItem(c, schedule, tomorrow, TimeAdjuster.adjustTime(adjustedTimeStart, 0), timeLength);
                        }
                    }
                    /* else if there is no day shift */
                } else { // if the time zone adjustment is still contained within the same day as the event
                    saveScheduleItem(c, schedule, today, adjustedTimeStart, timeLength);
                }
            }
            c.close();
            db.close();
        }
        return schedule;
    }

    /** saveScheduleItem saves a custom schedule into a given schedule
     *  Created by: Littlesnowman88
     *  schedule-saving order/algorithm based on otter57's ordering
     *  @param cursor, a Cursor for navigating the local database information
     *         schedule, an ArrayList<ScheduleInfo> to put event items into.
     *         day, a String MM/dd/yyyy for saving the event's day
     *         startTime, the event's integer-formatted (hhmm) start time
     *         length, the event's integer length (in minutes)
     *  Precondition: for an event to be added, event length must be > 0
     *  Postconditions: an event is added into the provided schedule.
     *                  Cursor will remain at its given position
     */
    private void saveScheduleItem(Cursor cursor, ArrayList<ScheduleInfo> schedule, String day, int startTime, int eventLength) {
        if (0 < eventLength) {
            ScheduleInfo event = new ScheduleInfo();
            event.setDay(day);
            event.setTimeStart(startTime);
            event.setTimeLength(eventLength);
            event.setDesc(cursor.getString(3));
            event.setLocationName(cursor.getString(4));
            event.setCategory(cursor.getString(5));
            schedule.add(event);
        }
    }

    /** createYesterday receives a string date and returns the previous string date
     *  Created by: Littlesnowman88
     *  Based on: Nicholas Presa on Stack Overflow,
     *      https://stackoverflow.com/questions/38573810/subtract-one-day-from-date-with-format-as-mm-dd-yyyy
     *  @param currentDay, a String formatted date MM/dd/yyyy
     *  @return previous_day, a String formatted date MM/dd/yyyy (takes leap years and month-transitions into account)
     */
    private String createYesterday(String currentDay) {
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
        String previous_day = "";
        try {
            Date currentDayCopy = formatter.parse(currentDay);
            Calendar subtractionCalendar = Calendar.getInstance();
            subtractionCalendar.setTime(currentDayCopy);
            subtractionCalendar.add(Calendar.DATE, -1);
            Date yesterday = subtractionCalendar.getTime();
            previous_day = formatter.format(yesterday);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return previous_day;
    }

    /** createTomorrow receives a string date and returns the next string date
     *  Created by: Littlesnowman88
     *  Based on: Nicholas Presa on Stack Overflow,
     *      https://stackoverflow.com/questions/38573810/subtract-one-day-from-date-with-format-as-mm-dd-yyyy
     *  @param currentDay, a String formatted date MM/dd/yyyy
     *  @return next_day a String formatted date MM/dd/yyyy (takes leap years and month-transitions into account)
     */
    private String createTomorrow(String currentDay) {
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
        String next_day = "";
        try {
            Date currentDayCopy = formatter.parse(currentDay);
            Calendar additionCalendar = Calendar.getInstance();
            additionCalendar.setTime(currentDayCopy);
            additionCalendar.add(Calendar.DATE,1);
            Date yesterday = additionCalendar.getTime();
            next_day = formatter.format(yesterday);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return next_day;
    }

    /**
     * @return all of the days listed in the JSON schedule
     */
    private ArrayList<String> getJSONDays() {
        ArrayList<String> days = new ArrayList<>();
        String queryString = "SELECT DISTINCT " + COLUMN_DAY + " " +
                "FROM " + TABLE_SCHEDULE + " ORDER BY " + COLUMN_DAY + " ASC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(queryString, null);

        while (c.moveToNext()) {
            String day = c.getString(0);
            if (!day.contains(",")){
                days.add(c.getString(0));
            }
        }

        c.close();
        db.close();

        return days;
    }

    private static ArrayList<String> determineUniqueDays(ArrayList<String> days) {
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
        ArrayList<Date> unique_dates;
        ArrayList<String> unique_days = new ArrayList<>();
        unique_dates = determineEdgeDays(days, formatter);
        Date first_day = unique_dates.get(0);
        unique_days.add(formatter.format(first_day));
        if (unique_dates.size() > 1) {
            Date last_day = unique_dates.get(1);
            unique_days.add(formatter.format(last_day));
            Calendar additionCalendar = Calendar.getInstance();
            additionCalendar.setTime(first_day);
            additionCalendar.add(Calendar.DATE, 1);
            Date next_day = additionCalendar.getTime();
            while (! next_day.equals(last_day)) {
                int second_to_last = unique_dates.size() - 1;
                unique_dates.add(second_to_last, next_day);
                unique_days.add(second_to_last, formatter.format(next_day));
                additionCalendar.add(Calendar.DATE, 1);
                next_day = additionCalendar.getTime();
            }
        }
        return unique_days;
    }

    private static ArrayList<Date> determineEdgeDays(ArrayList<String> days, SimpleDateFormat formatter) {
        ArrayList<Date> edge_days = new ArrayList<>();
        Date first_day, last_day;
        try {
            first_day = formatter.parse(days.get(0));
            last_day = formatter.parse(days.get(0));
        } catch (ParseException e) {
            throw new RuntimeException(("ERROR: in determineEdgeDays, the first date in days failed to parse.\n" + e.getMessage()));
        }
        if (days.size() > 1) {
            for (String day : days) {
                try {
                    Date date = formatter.parse(day);
                    if (date.before(first_day)) { first_day = date; }
                    else if (date.after(last_day)) { last_day = date; }
                } catch (ParseException pe) { pe.printStackTrace(); }
            }
            edge_days.add(first_day);
            edge_days.add(last_day);
        } else { edge_days.add(first_day); }
        return edge_days;
    }

    /**
     * @return allDays, all the days of the event after time zone adjustments are taken into account
     *  (getFullSchedule must be called before this can happen);
     */
    public ArrayList<String> getDays() { return allDays; }

    /**
     * @param schedule, an ArrayList<ScheduleInfo> with activities in it
     * @return an ArrayList<Integer> with the earliest start and latest end time in the schedule.
     * Specifically, returns an ArrayList containing the schedule's earliest event start time
     *       and the schedule's latest event start time.
     * Modified by Littlesnowman88 on 19 June 2018
     * Now adjusts header to align with the app's selected time zone.
     * Called once the schedule is built by getFullSchedule()
     */
    private ArrayList<Integer> getScheduleEdges(ArrayList<ScheduleInfo> schedule) {
        ArrayList<Integer> times = new ArrayList<>();
        int timeStart = 2400;
        int eventStart;
        int timeEnd = 0;
        int comparison_time;

        for (ScheduleInfo activity : schedule) {
            eventStart = activity.getTimeStart();
            if (eventStart < timeStart) {
                timeStart = eventStart;
                comparison_time = timeStart;
            } else {comparison_time = eventStart; }

            int countMins = activity.getTimeLength();
            comparison_time += (countMins / 60)*100;
            comparison_time += countMins%60;
            if (comparison_time%100 >= 60)
                comparison_time += (100 - 60);
            if (timeEnd < comparison_time) {
                timeEnd = Math.min(comparison_time, 2400);
            }
        }
        times.add(timeStart);
        times.add(timeEnd);

        return times;
    }

    /**
     * returns the size 2 ArrayList<Integer> of the earliest and start times in the schedule
     * getFullSchedule must be called first.
     */
    public ArrayList<Integer> getScheduleTimeRange() { return scheduleTimeRange; }

    /**
     * returns Informational page data
     * @return an ArrayList of Info objects for page
     */
    public ArrayList<Info> getInfoPage(String page) {
        ArrayList<Info> hq = new ArrayList<>();

        String queryString = "SELECT * FROM " + TABLE_INFORMATION_PAGE + " WHERE "
                + COLUMN_PAGE + " LIKE \"%" + page + "%\"" + " LIMIT 1";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(queryString, null);

        while (c.moveToNext()) {
            Info temp = new Info();

            temp.setHeader(c.getString(0));
            temp.setBody(c.getString(1));

            hq.add(temp);
        }
        c.close();
        db.close();
        return hq;
    }

    /**
     * Returns about page information
     * @return an ArrayList of Info objects for page
     */
    public ArrayList<Info> getAboutPage(String page) {
        ArrayList<Info> hq = new ArrayList<>();

        String queryString = "SELECT * FROM " + TABLE_ABOUT_PAGE+ " WHERE "
                + COLUMN_PAGE + " LIKE \"%" + page + "%\"";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(queryString, null);

        while (c.moveToNext()) {
            Info temp = new Info();

            temp.setHeader(c.getString(0));
            temp.setBody(c.getString(1));

            hq.add(temp);
        }
        c.close();
        db.close();
        return hq;
    }

    /**
     * @return Contact Page info
     */
    public ArrayList<Info> getContactPage() {
        ArrayList<Info> contactPage = new ArrayList<>();
        String queryString = "SELECT * FROM " + TABLE_CONTACT_PAGE;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(queryString, null);

        while (c.moveToNext()) {
            Info temp = new Info();

            temp.setHeader(c.getString(0));
            temp.setBody(c.getString(1));
            temp.setId(c.getInt(2));

            contactPage.add(temp);
        }
        c.close();
        db.close();
        return contactPage;
    }

    /**
     * returns Notifications
     * @return Info
     */
    public ArrayList<Info> getNotifications() {
        ArrayList<Info> notifications = new ArrayList<>();
        String queryString = "SELECT * FROM " + TABLE_NOTIFICATIONS + " ORDER BY " + COLUMN_DATE + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(queryString, null);

        while (c.moveToNext()) {
            Info temp = new Info();
            temp.setId(c.getInt(0));
            temp.setHeader(c.getString(1));
            temp.setBody(c.getString(2));
            temp.setDate(c.getString(3));
            if (c.getInt(4) == 1)
                temp.setNew();

            notifications.add(temp);
        }
        c.close();
        db.close();
        return notifications;
    }

    /**
     * returns only new Notifications
     * @return Info
     */
    public ArrayList<Info> getNewNotifications() {
        ArrayList<Info> notifications = new ArrayList<>();
        String queryString = "SELECT * FROM " + TABLE_NOTIFICATIONS;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(queryString, null);

        while (c.moveToNext()) {
            Info temp = new Info();
            temp.setId(c.getInt(0));
            temp.setHeader(c.getString(1));
            temp.setBody(c.getString(2));
            temp.setDate(c.getString(3));
            if (c.getInt(4) == 1)
                temp.setNew();
            if (temp.getNew())
                notifications.add(temp);
        }
        c.close();
        db.close();
        return notifications;
    }

    /** sets all the notifications "is new" to false
     * created by: Littlesnowman88
     * created on: 16 July 2018
     */
    public void unflagNewNotifications() {
        String queryString = "SELECT * FROM " + TABLE_NOTIFICATIONS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(queryString, null);

        while (c.moveToNext()) {
            if (c.getInt(4) == 1) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_HEADER, c.getString(1));
                values.put(COLUMN_INFO, c.getString(2));
                values.put(COLUMN_DATE, c.getString(3));
                values.put(COLUMN_ID, c.getInt(0));
                values.put(COLUMN_NEW, 0);
                String whereClause = COLUMN_HEADER + " = " + "?";
                String[] whereArgs = {c.getString(1)};

                db.update(TABLE_NOTIFICATIONS, values, whereClause, whereArgs);
            }
        }

        ArrayList<Info> notifications = getNotifications();
        for (Info notification : notifications) {
            notification.setOld();
        }

        c.close();
        db.close();
    }

    /**
     * returns Notifications
     * @return Info
     */
    public ArrayList<Integer> getCurrentNotifications() {
        ArrayList<Integer> notificationTitles = new ArrayList<>();
        String queryString = "SELECT " + COLUMN_ID + " FROM " + TABLE_NOTIFICATIONS;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(queryString, null);

        while (c.moveToNext()) {
            notificationTitles.add(c.getInt(0));
        }
        c.close();
        db.close();
        return notificationTitles;
    }

    /**
     * @return Housing info
     */
    public ArrayList<HousingInfo> getHousing() {
        ArrayList<HousingInfo> housing = new ArrayList<>();
        String queryString = "SELECT " + TABLE_HOUSING + "." + COLUMN_NAME + ", " + TABLE_CONTACTS
                + "." + COLUMN_ADDRESS + ", " + TABLE_CONTACTS + "." + COLUMN_PHONE + ", " + TABLE_HOUSING
                + "." + COLUMN_DRIVER + ", " + TABLE_HOUSING + "." + COLUMN_STUDENTS + " FROM " + TABLE_HOUSING + " INNER JOIN " + TABLE_CONTACTS
                + " ON " + TABLE_HOUSING + "." + COLUMN_NAME + " = " + TABLE_CONTACTS + "." + COLUMN_NAME
                + " ORDER BY " + COLUMN_DRIVER;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(queryString, null);

        while (c.moveToNext()) {
            HousingInfo temp = new HousingInfo();

            temp.setName(c.getString(0));
            temp.setAddress(c.getString(1));
            temp.setPhone(c.getString(2));
            temp.setDriver(c.getString(3));
            temp.setStudents(c.getString(4));

            housing.add(temp);
        }
        c.close();
        db.close();
        return housing;
    }

    /**
     * @return Prayer partner info
     */
    public ArrayList<String> getPrayerPartners() {
        ArrayList<String> students = new ArrayList<>();
        String queryString = "SELECT * FROM " + TABLE_PRAYER_PARTNERS;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(queryString, null);

        while (c.moveToNext()) {
            students.add(c.getString(0));
        }
        c.close();
        db.close();
        return students;
    }

    /* ************************* Delete queries ************************* */

    /**
     *
     * @param url, the url of the event to be removed
     */
    public void removeEvent (String url) {
        SQLiteDatabase db = this.getWritableDatabase();
        String whereClause = COLUMN_URL + " = " + "?";
        String[] whereArgs = {url};
        db.delete(TABLE_SCANNED_EVENTS, whereClause, whereArgs);
        db.close();
    }

    private void removeVersionNum (){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_VERSION_NUM, null, null);
        db.close();
    }

    /* ************************* Replace Queries ************************* */

    /**
     * A function to replace an event by using the removeEvent and addEvent functions
     * Created by TFMoo
     * @param remove_url, the url of the event to be removed
     * @param add_url, the url of the event to be added
     * @param add_name, the name of the event to be added
     */
    public void replaceEvent (String remove_url, String add_url, String add_name){
        removeEvent(remove_url);
        addEvent(add_url,add_name);
    }

    /**
     * Updates the version number for the current event
     * @param version, the new version number of the config and notification urls
     */
    public void replaceVersionNum (int[] version){
        removeVersionNum();
        addVersionNum(version);
    }

}
