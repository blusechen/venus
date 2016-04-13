package com.meidusa.venus.validate.annotation.service.domain;

import java.util.Date;

import com.meidusa.venus.validate.validator.annotation.LongRange;
import com.meidusa.venus.validate.validator.annotation.StringLength;
import com.meidusa.venus.validate.validator.annotation.StringNotEmpty;

public class AccountTestAnnotation {
    @LongRange(min = 1, max = 10000)
    private Long id;
    @StringNotEmpty
    @StringLength(minLength = 1, maxLength = 15)
    private String username;

    private Date registerDate;
    @StringNotEmpty(policy = "CITY_NEED")
    private String city;
    @StringNotEmpty(policy = "COUNTRY_NEED")
    private String country;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Date getRegisterDate() {
        return registerDate;
    }

    public void setRegisterDate(Date registerDate) {
        this.registerDate = registerDate;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @Override
    public String toString() {
        return "Account [id=" + id + ", username=" + username + ", registerDate=" + registerDate + ", city=" + city + ", country=" + country + "]";
    }

}
