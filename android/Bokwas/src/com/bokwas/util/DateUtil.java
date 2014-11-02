package com.bokwas.util;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import android.annotation.SuppressLint;

@SuppressLint("SimpleDateFormat")
public class DateUtil {
	
	@SuppressWarnings("deprecation")
	public static String getSimpleTime(Date date) {
		Format formatter = new SimpleDateFormat("MMMM, dd");
		String s = formatter.format(date);
		s += " "+date.getHours() + ":"+date.getMinutes();
		Date todayDate = new Date();
		int diffInDays = (int) getDateDiff(date, todayDate, TimeUnit.DAYS);
		int diffInHours = (int)getDateDiff(date, todayDate, TimeUnit.HOURS);
		int diffInMins = (int)getDateDiff(date, todayDate, TimeUnit.MINUTES);
		if(diffInDays >= 1 && diffInDays <8) {
			if(diffInDays == 1) {
				return "Yesterday";
			}
			return diffInDays + " days ago";
		}
		if(diffInHours >= 1 && diffInHours < 24) {
			if(diffInHours == 1) {
				return "An hour ago";
			}else if(diffInHours >= 24) {
				if(diffInDays<1) {
					return "Earlier today";
				}
			}
			return diffInHours + " hours ago";
		}
		else if(diffInMins < 60 ){
			return diffInMins + " mins ago";
		}
		
		return s;
	}
	
	/**
	 * 
	 * @param Older date
	 * @param Newer date
	 * @param TimeUnit.<Whatever>
	 * @return difference in time
	 */
	public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
	    long diffInMillies = date2.getTime() - date1.getTime();
	    return timeUnit.convert(diffInMillies,TimeUnit.MILLISECONDS);
	}
	
	@SuppressWarnings("deprecation")
	public static String formatToYesterdayOrToday(Date date) {
		Format formatter = new SimpleDateFormat("MMMM, dd");
		String s = formatter.format(date);
		s += " "+date.getHours() + ":"+date.getMinutes();
		return s;
	}
}
