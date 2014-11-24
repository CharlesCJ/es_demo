package com.util;
import java.text.SimpleDateFormat;
import java.util.Date;


public class TimeHelper {
	public static String getCurrentTime() {

		String returnStr = null;

		SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		Date date = new Date();

		returnStr = f.format(date);

		return returnStr;

	}
}
