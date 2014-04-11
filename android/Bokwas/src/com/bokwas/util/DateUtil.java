package com.bokwas.util;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;

@SuppressLint("SimpleDateFormat")
public class DateUtil {
	
//	public static String formatToYesterdayOrToday(Date mDate) {
//		
//		SimpleDateFormat sdf = new SimpleDateFormat("EEE hh:mma MMM d, yyyy");
//		Date in = null;
//		String date = mDate.toString();
//		try {
//			in = sdf.parse(date);
//		} catch (ParseException e) {
//			e.printStackTrace();
//		}
//
//		Calendar x = Calendar.getInstance();
//		x.setTime(in);
//
//		String hour = Integer.toString(x.get(Calendar.HOUR));
//		String minute = Integer.toString(x.get(Calendar.MINUTE));
//		String pm_am = x.get(Calendar.AM_PM) == Calendar.AM ? "AM" : "PM";
//
//		x.set(Calendar.HOUR, 0);
//		x.set(Calendar.HOUR_OF_DAY, 0);
//		x.set(Calendar.MINUTE, 0);
//		x.set(Calendar.SECOND, 0);
//		x.set(Calendar.MILLISECOND, 0);
//
//		Calendar today = Calendar.getInstance();
//		today.set(Calendar.HOUR, 0);
//		today.set(Calendar.HOUR_OF_DAY, 0);
//		today.set(Calendar.MINUTE, 0);
//		today.set(Calendar.SECOND, 0);
//		today.set(Calendar.MILLISECOND, 0);
//
//		Calendar yesterday = Calendar.getInstance();
//		yesterday.set(Calendar.HOUR, 0);
//		yesterday.set(Calendar.HOUR_OF_DAY, 0);
//		yesterday.set(Calendar.MINUTE, 0);
//		yesterday.set(Calendar.SECOND, 0);
//		yesterday.set(Calendar.MILLISECOND, 0);
//		yesterday.add(Calendar.DATE, -1);
//
//		if (x.compareTo(today) == 0) {
//			return "Today " + hour + ":" + minute + pm_am;
//		}
//		if (x.compareTo(yesterday) == 0) {
//			return "Yesterday " + hour + ":" + minute + pm_am;
//		}
//		return date;
//	}
	
	public static String formatToYesterdayOrToday(Date date) {
		Format formatter = new SimpleDateFormat("MMMM, dd");
		String s = formatter.format(date);
		s += " "+date.getHours() + ":"+date.getMinutes();
		return s;
	}
}
