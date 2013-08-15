package com.android.db;

import com.android.util.LogUtil;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
	
	private static final String DATABASE_NAME = "ac.db";
	private static final int DATABASE_VERSION = 2;
	private static final String sql = "CREATE TABLE IF NOT EXISTS apk_info" +
			"(_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
			"appname VARCHAR, " +
			"pkgname VARCHAR, " +
			"version VARCHAR, " +
			"uninstall INTEGER, " +
			"needActivate INTEGER, " +
			"launchPara VARCHAR," +
			"serial_id VARCHAR, " +
			"nextReqTime LONG ," +
			"launcheTimeStart INTEGER ," +
			"launcheTimeEnd INTEGER ," +
			"hadLaunchButNotUpLoad INTEGER," +
			"uninstallTime VARCHAR )" ;
	String sql2 = "CREATE TABLE IF NOT EXISTS traffic_info" +
			"(_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
			"pkgname VARCHAR UNIQUE, " +
			"label VARCHAR, " +
			"traffic LONG, " +
			"traffic_last LONG " +
			")";
	public DBHelper(Context context) {
		//CursorFactory设置为null,使用默认值
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	//数据库第一次被创建时onCreate会被调用
	@Override
	public void onCreate(SQLiteDatabase db) {
		
		LogUtil.log(DBManager.tag, "crate table sql = "+sql);
		LogUtil.log(DBManager.tag, "crate table sql2 = "+sql2);
		db.execSQL(sql);
		db.execSQL(sql2);
	}

	//如果DATABASE_VERSION值被改为2,系统发现现有数据库版本不同,即会调用onUpgrade
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("drop table IF EXISTS apk_info ");
		db.execSQL(sql);
		db.execSQL("drop table IF EXISTS traffic_info ");
		db.execSQL(sql2);
	}
}
