package com.app.cabmanagment.alpha.models;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

public class Vehicle implements java.io.Serializable{
    private long contact;
    private String registration_no;
    private boolean in_service;
    private String driver_name;
    private String city;
    private String cab_status;

    public String getCab_status() {
        return cab_status;
    }

    public void setCab_status(String cab_status) {
        this.cab_status = cab_status;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public long getContact() {
        return contact;
    }

    public void setContact(long contact) {
        this.contact = contact;
    }

    public String getRegistration_no() {
        return registration_no;
    }

    public void setRegistration_no(String registration_no) {
        this.registration_no = registration_no;
    }

    public boolean isIn_service() {
        return in_service;
    }

    public void setIn_service(boolean in_service) {
        this.in_service = in_service;
    }

    public String getDriver_name() {
        return driver_name;
    }

    public void setDriver_name(String driver_name) {
        this.driver_name = driver_name;
    }
}
