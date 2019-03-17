package com.licairiji.web;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Description: 时间工具类
 * pbd add 2018/12/21 10:54
 */
public class DateUtil {
    /**
     * 时间戳转换成时间
     *
     * @param s
     * @return
     */
    public static String stampToDate(long s) {
        String res;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        long lt = new Long(s);
        Date date = new Date(s * 1000);
        res = simpleDateFormat.format(date);
        return res;
    }

    /**
     * 时间字符串转换成时间戳
     * @param time
     * @return
     */
    public static Integer dateToStamp(String time) {
        Integer res = 0;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = simpleDateFormat.parse(time);
            res = (int) (date.getTime() / 1000);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return res;
    }

    public static Integer dateToStamp(Date time) {
        Integer res = 0;
        res = (int) (time.getTime() / 1000);
        return res;
    }


    /**
     * 把符合日期格式的字符串转换为日期类型
     */
    public static Date stringtoDate(String dateStr, String format) {
        Date d = null;
        SimpleDateFormat formater = new SimpleDateFormat(format);
        try {
            formater.setLenient(false);
            d = formater.parse(dateStr);
        } catch (Exception e) {
            // log.error(e);
            d = null;
        }
        return d;
    }

    /*
     * 将unix时间戳转换成时间格式字符串
     * @param timestampString
     * @return
     */
    public static String unixTimeStampToDate(String timestampString) {
        if (timestampString == null) return null;
        Long timestamp = Long.parseLong(timestampString) * 1000;
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(timestamp));
        return date;
    }


    /**
     * 把日期转换为字符串
     */
    public static String dateToString(Date date, String format) {
        String result = "";
        SimpleDateFormat formater = new SimpleDateFormat(format);
        try {
            result = formater.format(date);
        } catch (Exception e) {
            // log.error(e);
        }
        return result;
    }
    /**
     * 日期格式字符串转换成时间戳
     * @param date 字符串日期
     * @param format 如：yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static Long date2TimeStamp(String date_str,String format){

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return sdf.parse(date_str).getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0l;
    }
//
//    public static void main(String[] args) {
//        //System.out.println(stampToDate(String.valueOf(System.currentTimeMillis())));
//        //System.out.println(System.currentTimeMillis()/1000);
//       /* String str="a,b,c";
//        String[] strArr = str.split(",");
//        System.out.println(strArr[1]);*/
//        System.out.println(dateToStamp("2017-08-15"));
//    }

}
