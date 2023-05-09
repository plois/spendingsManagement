package com.android.mma.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

@Entity
public class Spending {

    @PrimaryKey (autoGenerate = true)
    @NonNull
    public int uid;


    private String timestamp;
    private String merchant;
    private String origin;
    private double cost;

    public Spending(String timestamp, String merchant, String origin,double cost){
        this.timestamp = timestamp;
        this.merchant = merchant;
        this.origin = origin;
        this.cost = cost;
    }

    public String getMerchant() {
        return this.merchant;
    }

    public double getCost() {
        return this.cost;
    }

    public String getTimestamp() {
        return this.timestamp;
    }

    public String getOrigin() {
        return origin;
    }

    public String getMonthString(){
        String pattern = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat df = new SimpleDateFormat(pattern,Locale.ENGLISH);
        Calendar calendar = Calendar.getInstance();
        try{
            calendar.setTime(df.parse(timestamp));
        }catch(ParseException e){
            e.printStackTrace();
        }
        calendar.setTimeZone(TimeZone.getTimeZone("UTC+8"));
        return calendar.getDisplayName(Calendar.MONTH,Calendar.LONG, Locale.ENGLISH);
    }

    public String getMonthRaw(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat sdf2 = new SimpleDateFormat("MM");
        Calendar calendar = Calendar.getInstance();
        try{
            calendar.setTime(sdf.parse(timestamp));
        }catch(ParseException e){
            e.printStackTrace();
        }
        return sdf2.format(calendar.getTime());
    }

    public String getMonthDate(){
        String pattern = "MM/dd HH:mm";
        DateFormat df = new SimpleDateFormat(pattern);
        Calendar calendar = Calendar.getInstance();
        //calendar.setTimeInMillis(timestamp);
        calendar.setTimeZone(TimeZone.getTimeZone("UTC+8"));
        String hour = df.format(calendar.getTime());
        return timestamp;
    }

    public static class MonthCost {
        private double cost;
        private String month;

        public MonthCost(double cost, String month){
            this.cost = cost;
            this.month = month;
        }

        public double getCost(){
            return this.cost;
        }
        public String getMonth(){
            return this.month;
        }
    }

    public static class RatioTag {
        private double ratio;
        private String tag;

        public RatioTag(double ratio, String tag){
            this.ratio = ratio;
            this.tag = tag;
        }

        public double getRatio(){ return this.ratio; }

        public String getTag(){ return this.tag; }
    }

}
