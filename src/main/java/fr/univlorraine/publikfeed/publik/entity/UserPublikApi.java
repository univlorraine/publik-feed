package fr.univlorraine.publikfeed.publik.entity;

import lombok.Data;

@Data
public class UserPublikApi {
	private int id;
    private String ou;
    private String date_joined;
    private String last_login;
    private String password;
    private Boolean is_superuser;
    private String  uuid;
    private String username;
    private String first_name;
    private String last_name;
    private String email;
    private Boolean email_verified;
    private String email_verified_date;
    private Boolean is_staff;
    private Boolean is_active;
    private String modified;
    private Boolean last_account_deletion_alert;
    private Boolean deleted;
    private Boolean deactivation;
    private String deactivation_reason;
    private String title;
    private Boolean title_verified;
    private Boolean first_name_verified;
    private Boolean last_name_verified;
    private String address;
    private Boolean address_verified;
    private String zipcode;
    private Boolean zipcode_verified;
    private String city;
    private Boolean city_verified;
    private String phone;
    private Boolean phone_verified;
    private String mobile;
    private Boolean mobile_verified;
    private String uid;
    private Boolean uid_verified;

}
