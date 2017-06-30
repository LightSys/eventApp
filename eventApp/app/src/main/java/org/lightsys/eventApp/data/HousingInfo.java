package org.lightsys.eventApp.data;


/**
 * @author otter57
 * created on 3/29/2017.
 *
 * Class represents housing info
 */
public class HousingInfo {

    private String name;
    private String address;
    private String phone;
    private String students;
    private String driver;

    /* ************************* Construct ************************* */
    public HousingInfo() {}

    /* ************************* Set ************************* */
    public void setName(String name)               { this.name = name; }

    public void setAddress(String address)        { this.address = address; }

    public void setPhone(String phone)      { this.phone = phone; }

    public void setStudents (String students)     { this.students = students; }

    public void setDriver (String driver)     { this.driver = driver; }

    /* ************************* Get ************************* */
    public String getName()      { return name; }

    public String getAddress()    { return address; }

    public String getPhone()   { return phone; }

    public String getStudents()   { return students; }

    public String getDriver()   { return driver; }

}
