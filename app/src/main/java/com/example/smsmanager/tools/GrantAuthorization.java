package com.example.smsmanager.tools;

import java.util.Calendar;
import java.util.Date;

public class GrantAuthorization {
    public static boolean grant(Date n){
        if(n==null)return false;
        else{
            Calendar calendar = Calendar.getInstance();
            Date date=calendar.getTime();
            return date.getTime() <= n.getTime();
        }

    }
}
