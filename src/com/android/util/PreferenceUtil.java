package com.android.util;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceUtil {
	private static final String NAME = "ac";
	
	private SharedPreferences sharedPreference;
	public PreferenceUtil(Context ctx) {
		super();
		sharedPreference = ctx.getSharedPreferences(NAME, Context.MODE_PRIVATE);
	}

	public void setInt(String key,int value){
		sharedPreference.edit().putInt(key, value).commit();
	}
	
	public int getInt(String key, int defValue){
		return sharedPreference.getInt(key, defValue);
	}
	
	public void setLong(String key,Long value){
		sharedPreference.edit().putLong(key, value).commit();
	}
	
	public Long getLong(String key, Long defValue){
		return sharedPreference.getLong(key, defValue);
	}
	
	public void setBoolean(String key ,boolean value){
		sharedPreference.edit().putBoolean(key, value).commit();
	}
	
	public boolean getBoolean(String key ,boolean defValue){
		return sharedPreference.getBoolean(key, defValue);
	}
	
	public String getString(String key,String defValue){
		return sharedPreference.getString(key, defValue);
	}
	public void setString(String key,String value){
		sharedPreference.edit().putString(key, value).commit();
	}
}
