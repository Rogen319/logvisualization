package logapi.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class MyUtil {

    //Convert the time from utc format to local format
    public static String getLocalTimeFromUTCFormat(String utcTime){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            Date myDate = simpleDateFormat.parse(utcTime);
            simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            simpleDateFormat.setTimeZone(TimeZone.getDefault());
            String localTime = simpleDateFormat.format(myDate.getTime());
            return localTime;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }
}
