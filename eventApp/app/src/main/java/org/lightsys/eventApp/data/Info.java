package org.lightsys.eventApp.data;

/**
 * Created by otter57 on 3/30/17.
 * Modified by Littlesnowman88 on 14 June 2018
 * Object for general info object
 */

public class Info {


    private int id;
    private String header;
    private String body;
    private String date;
    private String id_name;
    private boolean is_new;

    /* ************************* Construct ************************* */
    public Info() {
        this.is_new = false;
    }

    public Info(String header, String body){
        this.header = header;
        this.body = body;
        this.is_new = false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) { this.body = body;  }

    public String getName() { return id_name; }

    public void setName(String name) { this.id_name = name; }

    public void setNew() { this.is_new = true; }

    public void setOld() {this.is_new = false; }

    public boolean getNew() { return this.is_new; }

}