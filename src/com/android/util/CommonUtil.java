package com.android.util;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;

public class CommonUtil {
	private  final static String tag = "NetworkUtil";

	/**
	 * 
	 * @param ctx
	 * @return if wifi return 0,if GPRS return 1,else return -1
	 */
	public static int  getNetworkType (Context ctx){
		ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = cm.getActiveNetworkInfo();
		if (networkInfo==null) {
			return -1;
		}
		LogUtil.log(tag, ""+networkInfo.getState().toString());
		if (networkInfo.isConnected()) {
			LogUtil.log(tag, "isConnected");
			if (ConnectivityManager.TYPE_WIFI==networkInfo.getType()) {
				LogUtil.log(tag, "TYPE_WIFI");
				return 0;
			}else if (ConnectivityManager.TYPE_MOBILE==networkInfo.getType()) {
				LogUtil.log(tag, "TYPE_MOBILE");
				return 1;
			}
		}
		return -1;
	}
	
	/**
	 * 
	 * @param time
	 * @return 如果当前时间比time对应时间>1天就返回true，否则返回false
	 */
	public static boolean moreOneDay(String time) {
		return moreDays(time, 1);
		/*SimpleDateFormat smdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			Date start = smdf.parse(time);
			
			String strFormat = getHumanDate(System.currentTimeMillis());
			
			Date end = smdf.parse(strFormat);
			
			long t = (end.getTime() - start.getTime()) / (3600 * 24 * 1000);
			System.out.println(t);
			if (t>=1 || t <= -1) {//手动改时间
				return true;
			}
			return false;
		} catch (ParseException e) {
			e.printStackTrace();
			return true;
		}*/
	}
	
	public static boolean moreDays(String time,int days) {
		SimpleDateFormat smdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			Date start = smdf.parse(time);
			
			String strFormat = getHumanDate(System.currentTimeMillis());
			
			Date end = smdf.parse(strFormat);
			
			long t = (end.getTime() - start.getTime()) / (3600 * 24 * 1000);
			System.out.println(t);
			if (t>=days || t <= -days) {//手动改时间
				return true;
			}
			return false;
		} catch (ParseException e) {
			e.printStackTrace();
			return true;
		}
	}
	
	public static String getVersion(Context ctx,String pkgName) throws NameNotFoundException{
		return ctx.getPackageManager().getPackageInfo(pkgName, 0).versionName;
	}
	
	/**
	 * 
	 * @param ctx
	 * @param key
	 * @return 获取指定meta-data 标签的值
	 */
	public static String getMetaDataString(Context ctx,String key){
		ApplicationInfo ai;
		try {
			ai = ctx.getPackageManager().getApplicationInfo(ctx.getPackageName(), PackageManager.GET_META_DATA);
			return ai.metaData.getString(key);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return "";
		}
	}
	public static boolean getMetaDataBoolean(Context ctx,String key){
		ApplicationInfo ai;
		try {
			ai = ctx.getPackageManager().getApplicationInfo(ctx.getPackageName(), PackageManager.GET_META_DATA);
			return ai.metaData.getBoolean(key);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * 日期 把毫秒数转化为字符串格式的
	 * @param mils
	 * @return "yyyy-MM-dd HH:mm:ss"
	 */
	public static String getHumanDate(long mils){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(new Date(mils));
	}
	
	public static String getImei(TelephonyManager tm) {
		String imei = tm.getDeviceId();
		return imei==null?"":imei;
	}
	public static String getImsi(TelephonyManager tm) {
		String imsi = tm.getSubscriberId();
		return imsi==null?"":imsi;
	}
	
	public static String getMac(WifiManager wm) {
		String mac=wm.getConnectionInfo().getMacAddress();
		Long start = System.currentTimeMillis();
		while (mac==null || mac.length()==0) {
			Long end = System.currentTimeMillis();
			if (end-start>2500) {//奇葩的发现部分手机没有wifi~~~
				return "";
			}
			if (!wm.isWifiEnabled()) {
				wm.setWifiEnabled(true);
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			mac=wm.getConnectionInfo().getMacAddress();
		}
		return mac;
	}
	
	public static String getVersion(Context ctx){
		try {
			return ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return "";
	}
}
