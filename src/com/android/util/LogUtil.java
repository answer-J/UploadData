package com.android.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.Environment;
import android.util.Log;

public class LogUtil {
	private static final boolean DEBUG = true;
	public static final String LOG_FILE = "/sdcard/upload.log";
	public static void log(String tag ,String msg){
		Log.d(tag, Thread.currentThread().getName()+"----"+Thread.currentThread().getId()+"---"+msg);
		writeLog(tag, Thread.currentThread().getName()+"----"+Thread.currentThread().getId()+"---"+msg,false);
	}
	
	public static String getExceptionTrace(Throwable e){
		if (e != null) {
			StringWriter w = new StringWriter();
			PrintWriter pw = new PrintWriter(w);
			e.printStackTrace(pw);
			return w.toString();
		}
		return "no exception";
	}
	
	private static void writeLog(String tag, String log, boolean isLong) {
		if (DEBUG) {
			File file = new File(LOG_FILE);
			if (!file.exists()) {
				try {
					file.createNewFile();
				} catch (IOException e) {
					 e.printStackTrace();
				}
			}
			BufferedWriter out = null;
			try {
				// Create file
				FileWriter fstream = new FileWriter(LOG_FILE, true);
				out = new BufferedWriter(fstream);
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss  ");
				String time = sdf.format(new Date(System.currentTimeMillis()));
				out.append(time + tag);
				if (isLong) {
					out.append("\r\n\r\n");
				} else {
					out.append(" - ");
				}
				out.append(log);
				out.append("\r\n\r\n");
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (out != null) {
						out.close();
					}
				} catch (IOException e) {
				}
			}
		}
	}
}
