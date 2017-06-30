package org.lightsys.sbcat.data;

/**
 * Created by otter57 on 3/30/17.
 *
 * Object for Schedule Info objects
 */

public class ScheduleInfo {


    private String day;
    private int timeStart;
    private int timeLength;
    private String desc;
    private String locationName;
    private String category;

    /* ************************* Construct ************************* */
    public ScheduleInfo() {
    }

    public ScheduleInfo (int timeLength, String category){
        this.timeLength = timeLength;
        this.category = category;
    }
    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }


    public int getTimeStart() {
        return timeStart;
    }

    public void setTimeStart(int timeStart) {
        this.timeStart = timeStart;
    }
    public int getTimeLength() {
        return timeLength;
    }

    public void setTimeLength(int timeEnd) {
        this.timeLength = timeEnd;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

}


