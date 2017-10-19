package org.lightsys.eventApp.data;


/**
 * @author otter57
 * created on 3/30/2017.
 *
 * Class represents Contact Info
 */
public class ContactInfo {

    private String name;
    private String Address;
    private String Phone;

    /* ************************* Construct ************************* */
    public ContactInfo() {}

    /* ************************* Set ************************* */
    public void setName(String name)               { this.name = name; }

    public void setAddress(String Address)        { this.Address = Address; }

    public void setPhone(String Phone)      { this.Phone = Phone; }


    /* ************************* Get ************************* */
    public String getName()      { return name; }

    public String getAddress()    { return Address; }

    public String getPhone()   { return Phone; }


}