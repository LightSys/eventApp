package org.lightsys.eventApp.tools;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.lightsys.eventApp.data.Info;
import org.lightsys.eventApp.data.ContactInfo;
import org.lightsys.eventApp.data.HousingInfo;
import org.lightsys.eventApp.data.ScheduleInfo;

import java.util.ArrayList;

/**
 * Created by otter57 on 3/30/17.
 *
 * SQLite Database to store event information
 */

public class LocalDB extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 11;
    private static final String DATABASE_NAME = "SBCaT.db";
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
    //INFORMATION PAGE TABLE
    private static final String TABLE_INFORMATION_PAGE = "information_page";
    private static final String COLUMN_HEADER = "header";
    private static final String COLUMN_INFO = "info";
    private static final String COLUMN_PAGE = "page";
    //HOUSING TABLE
    private static final String TABLE_HOUSING = "housing";
    private static final String COLUMN_DRIVER = "driver";
    private static final String COLUMN_STUDENTS = "students";
    //PRAYER PARTNER TABLE
    private static final String TABLE_PRAYER_PARTNERS = "prayer_partners";
    //NOTIFICATIONS TABLE
    private static final String  TABLE_NOTIFICATIONS = "notifications";
    private static final String COLUMN_NEW = "new";
    private static final String COLUMN_ID = "id";
    //NAVIGATION TITLES TABLE
    private static final String TABLE_NAVIGATION_TITLES = "navigation_titles";
    private static final String COLUMN_ICON = "icon";
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

        String CREATE_TABLE_NOTIFICATIONS = "CREATE TABLE " + TABLE_NOTIFICATIONS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY," + COLUMN_HEADER + " TEXT," + COLUMN_INFO + " TEXT," + COLUMN_NEW + " INTEGER," + COLUMN_DATE + " TEXT)";
        db.execSQL(CREATE_TABLE_NOTIFICATIONS);

        String CREATE_TABLE_HOUSING = "CREATE TABLE " + TABLE_HOUSING + "("
                + COLUMN_NAME + " TEXT," + COLUMN_DRIVER + " TEXT," + COLUMN_STUDENTS + " TEXT)";
        db.execSQL(CREATE_TABLE_HOUSING);

        String CREATE_TABLE_PRAYER_PARTNERS = "CREATE TABLE " + TABLE_PRAYER_PARTNERS + "("
                + COLUMN_STUDENTS + " TEXT)";
        db.execSQL(CREATE_TABLE_PRAYER_PARTNERS);

        String CREATE_TABLE_NAVIGATION_TITLES = "CREATE TABLE " + TABLE_NAVIGATION_TITLES + "("
                + COLUMN_NAME + " TEXT," + COLUMN_ICON + " TEXT)";
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
    public void addNavigationTitles(String title, String icon) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, title);
        values.put(COLUMN_ICON, icon);

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
     * adds Notifications info
     */
    public void addNotification(Info notification, boolean isNew) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_HEADER, notification.getHeader());
        values.put(COLUMN_INFO, notification.getBody());
        values.put(COLUMN_NEW, isNew);
        values.put(COLUMN_DATE, notification.getDate());
        values.put(COLUMN_ID, notification.getId());

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
     * get general info
     * @param type, title under which info is stored
     * @return general info (year, url, logo, etc.)
     */
    public String getGeneral(String type) {
        String general=null;
        String queryString = "SELECT " + COLUMN_INFO + " FROM " + TABLE_GENERAL_INFO + " WHERE "
                + COLUMN_TYPE + " = \'" + type + "\'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(queryString, null);

        while (c.moveToNext()) {
            general = c.getString(0);

        }
        c.close();
        db.close();
        return general;
    }

    /**
     * @return color
     */
    public String getThemeColor(String name) {
        String color="#000000";
        String queryString = "SELECT " + COLUMN_HEX_CODE + " FROM " + TABLE_THEME + " WHERE "
                + COLUMN_NAME + " = \'" + name + "\'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(queryString, null);

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
            Log.d("HERE", "getNavigationTitles: " + temp.getBody() + temp.getHeader());
            titles.add(temp);

        }

        c.close();
        db.close();
        return titles;
    }

    /**
     * @return Contacts
     */
    public ContactInfo getContactByName(String name) {
        ContactInfo contact = new ContactInfo();
        String queryString = "SELECT * FROM " + TABLE_CONTACTS + " WHERE " + COLUMN_NAME + " = '" + name + "'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(queryString, null);

        while (c.moveToNext()) {
            contact.setName(c.getString(0));
            contact.setAddress(c.getString(1));
            contact.setPhone(c.getString(2));

        }
        c.close();
        db.close();
        return contact;
    }
    /**
     * @return schedule info
     */
    public ArrayList<ScheduleInfo> getScheduleByDay(String day) {
        ArrayList<ScheduleInfo> schedule = new ArrayList<>();
        String queryString = "SELECT * FROM " + TABLE_SCHEDULE + " WHERE " + COLUMN_DAY
                + " LIKE \"%" + day + "%\"" + " ORDER BY " + COLUMN_TIME_START;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(queryString, null);

        while (c.moveToNext()) {
            ScheduleInfo temp = new ScheduleInfo();
            temp.setDay(c.getString(0));
            temp.setTimeStart(c.getInt(1));
            temp.setTimeLength(c.getInt(2));
            temp.setDesc(c.getString(3));
            temp.setLocationName(c.getString(4));
            temp.setCategory(c.getString(5));

            schedule.add(temp);
        }
        c.close();
        db.close();
        return schedule;
    }

    /**
     * @return different days in schedule
     */
    public ArrayList<String> getDays() {
        ArrayList<String> days = new ArrayList<>();
        String queryString = "SELECT DISTINCT " + COLUMN_DAY + " FROM " + TABLE_SCHEDULE;

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

    /**
     * @return time range of schedule
     */
    public ArrayList<Integer> getScheduleTimeRange() {
        ArrayList<Integer> times = new ArrayList<>();
        int timeStart = 0;
        int timeEnd = 0;
        String queryString = "SELECT DISTINCT " + COLUMN_TIME_START + ", "
                + COLUMN_TIME_LENGTH + " FROM " + TABLE_SCHEDULE;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(queryString, null);

        while (c.moveToNext()) {
            int temp = c.getInt(0);
            if (timeStart == 0 || temp<timeStart){
                timeStart = temp;
            }
            int tempE = (c.getInt(1));//-15
            int x;
            if (tempE>=60){
                x = tempE/60;
                x= (tempE-(x*60))+(x*100);
            }else{
                x=tempE;
            }
            if (timeEnd<temp+x){
                timeEnd = temp + x;
            }
        }
        times.add(timeStart);
        times.add(timeEnd);

        c.close();
        db.close();
        return times;
    }


    /**
     * returns Informational page data
     * @return an ArrayList of Info objects for page
     */
    public ArrayList<Info> getInfoPage(String page) {
        ArrayList<Info> hq = new ArrayList<>();

        String queryString = "SELECT * FROM " + TABLE_INFORMATION_PAGE + " WHERE "
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
            temp.setDate(c.getString(4));

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
            temp.setDate(c.getString(4));

            if (c.getInt(3)==1) {
                notifications.add(temp);
                updateNotification(c.getInt(0));
            }
        }
        c.close();
        db.close();
        return notifications;
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

    /* ************************* Update Queries ************************* */

    /**
     * update notifications to indicate they are not new
     */
    private void updateNotification(int id){
        ContentValues values = new ContentValues();
        values.put(COLUMN_NEW, false);

        SQLiteDatabase db = this.getWritableDatabase();
        db.update(TABLE_NOTIFICATIONS, values, COLUMN_ID + " = " + id, null);
        db.close();
    }

    /**
     * update general table to set refresh rate
     */
    public void updateRefreshRate(String rate){
        ContentValues values = new ContentValues();
        values.put(COLUMN_INFO, rate);

        SQLiteDatabase db = this.getWritableDatabase();
        db.update(TABLE_GENERAL_INFO, values, COLUMN_TYPE + " = 'refresh'", null);
        db.close();
    }


}
