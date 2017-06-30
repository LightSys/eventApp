package org.lightsys.sbcat.data;

/**
 * Created by otter57 on 3/30/17.
 *
 * Object for HQ info
 */

public class Info {


    private int id;
    private String header;
    private String body;
    private String date;

    /* ************************* Construct ************************* */
    public Info() {
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

}