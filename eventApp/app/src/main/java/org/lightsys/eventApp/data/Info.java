package org.lightsys.eventApp.data;

/**
 * Created by otter57 on 3/30/17.
 *
 * Object for general info object
 */

public class Info {


    private int id;
    private String header;
    private String body;
    private String date;
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

    public void setBody(String body) {
        this.body = body;
    }

    public void setNew() { this.is_new = true; }

    public boolean getNew() { return this.is_new; }

}