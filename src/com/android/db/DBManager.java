package com.android.db;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.android.util.LogUtil;

public class DBManager {
	public final static String tag = "DBManager";
	private DBHelper helper;
	private SQLiteDatabase db;

	public DBManager(Context context) {
		helper = new DBHelper(context);
		// 因为getWritableDatabase内部调用了mContext.openOrCreateDatabase(mName, 0,
		// mFactory);
		// 所以要确保context已初始化,我们可以把实例化DBManager的步骤放在Activity的onCreate里
		db = helper.getWritableDatabase();
	}
	
	/**
	 * 添加单个激活包
	 * @param apkInfo
	 */
	public void addApkInfo(APKInfo apkInfo) {
		db.beginTransaction(); // 开始事务
		try {
			db.execSQL("INSERT INTO apk_info VALUES(null,?, ?, ?,?,?,?,?,?,?,?,?,?)", new Object[] {
					apkInfo.appName,
					apkInfo.pkgName, apkInfo.version, apkInfo.uninstall?APKInfo.yes:APKInfo.no,
					apkInfo.needActivate?APKInfo.yes:APKInfo.no,apkInfo.launchPara, apkInfo.serial_id,apkInfo.nextReqTime,
					apkInfo.launcheTimeStart,apkInfo.launcheTimeEnd,apkInfo.hadLaunchButNotUpLoad?APKInfo.yes:APKInfo.no,
					apkInfo.uninstallTime});

			db.setTransactionSuccessful(); // 设置事务成功完成
		} catch(Exception e){
			Log.e(tag, e.getMessage());
		} finally {
			db.endTransaction(); // 结束事务
		}
	}
	
	public void updateUninstallTime(String pkgName,String uninstallTime){
		ContentValues cv = new ContentValues(); 
		cv.put("uninstall",APKInfo.yes);
		cv.put("uninstallTime",uninstallTime);
		int i = db.update("apk_info", cv, "pkgname = ?", new String[]{pkgName});
		LogUtil.log(tag, "update uninstall time rows ="+i);
	}
	
	public JSONArray queryUploadAppInfo(){
		String sql = "select appname,pkgname,version,uninstall,uninstallTime from apk_info";
		Cursor c = db.rawQuery(sql, null);
		JSONArray array = new JSONArray();
		while(c.moveToNext()){
			JSONObject obj = new JSONObject();
			try {
				obj.put("appname", c.getString(0));
				obj.put("pkgname", c.getString(1));
				obj.put("version", c.getString(2));
				obj.put("uninstall", c.getString(3));
				obj.put("uninstallTime", c.getString(4));
				array.put(obj);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return array;
	}
	
	
	/**
	 * 
	 * @param apkInfo
	 * @param updataUninstall 如果需要更新"uninstall"字段，就传ture，否则false 
	 * @return
	 */
	public synchronized int update(APKInfo apkInfo , boolean updataUninstall){
		ContentValues cv = new ContentValues(); 
		if (updataUninstall) {
			cv.put("uninstall", apkInfo.uninstall ? APKInfo.yes : APKInfo.no);
		}
		cv.put("needActivate", apkInfo.needActivate ? APKInfo.yes : APKInfo.no);
		cv.put("launchPara", apkInfo.launchPara);
		cv.put("serial_id", apkInfo.serial_id);
		cv.put("nextReqTime", apkInfo.nextReqTime);
		cv.put("launcheTimeStart", apkInfo.launcheTimeStart);
		cv.put("launcheTimeEnd", apkInfo.launcheTimeEnd);
		return db.update("apk_info", cv, "pkgname = ?", new String[]{apkInfo.pkgName});
	}
	
	/**
	 * 
	 * @param pkgName 包名
	 * @param hadLaunchButNotUpLoad true，如果启动成功，上传启动数据失败
	 * @return 更新hadLaunchButNotUpLoad字段
	 */
	public synchronized int update(String pkgName,boolean hadLaunchButNotUpLoad){
		ContentValues cv = new ContentValues();
		cv.put("hadLaunchButNotUpLoad", hadLaunchButNotUpLoad?APKInfo.yes:APKInfo.no);
		return db.update("apk_info", cv, "pkgname = ?", new String[]{pkgName});
	}
	
	
	/**
	 * 
	 * @param pkgName
	 * @return 返回是否存在该包名的记录
	 */
	public boolean hadExsit(String pkgName,String versionName){
		Cursor c = db.rawQuery("select * from apk_info where pkgname = ? and version = ?", new String[]{pkgName,versionName});
		if (c.getCount()==1) {
			return true;
		}
		return false;
	}
	
	/**
	 * 查询第一个需要激活的包,如果没有需要激活的包，则返回null
	 * @return
	 */
	public APKInfo queryFirstActivateAPK(Context context){
		String sql = "select * from apk_info where uninstall=0 and needActivate = 1 and hadLaunchButNotUpLoad = 0 order by _id limit 0,1";
		Cursor c = db.rawQuery(sql, null);
		APKInfo apkInfo = null;
		if (c.moveToNext()) {
			apkInfo = cursor2APKInfo(c);
			LogUtil.log(tag, "apkInfo="+apkInfo.toString());
		}
		//需要激活的包被卸载
		if (apkInfo!=null && unInstallApp(context, apkInfo.pkgName, apkInfo.version)) {
			apkInfo.uninstall = true;
			update(apkInfo, true);
			return queryFirstActivateAPK(context);
		}else{
			return apkInfo;
		}
	}

	/**
	 * 
	 * @param pkgName
	 * @param version
	 * @return 根据包名和版本返回该app是否被卸载
	 */
	private boolean unInstallApp(Context context,String pkgName, String version) {
		try {
			PackageInfo info = context.getPackageManager().getPackageInfo(pkgName, 0);
			if (version.equalsIgnoreCase(info.versionName)) {
				return false;
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return true;
	}
	
	private APKInfo cursor2APKInfo(Cursor c) {
		APKInfo apkInfo;
		apkInfo = new APKInfo();
		apkInfo._id = c.getInt(c.getColumnIndex("_id"));
		apkInfo.pkgName = c.getString(c.getColumnIndex("pkgname"));
		apkInfo.version = c.getString(c.getColumnIndex("version"));
		//检查apk包是否被卸载
		
		apkInfo.uninstall = c.getInt(c.getColumnIndex("uninstall"))== APKInfo.yes ? true : false;
		apkInfo.needActivate = c.getInt(c.getColumnIndex("needActivate"))== APKInfo.yes ? true : false;
		apkInfo.launchPara = c.getString(c.getColumnIndex("launchPara"));
		apkInfo.serial_id = c.getString(c.getColumnIndex("serial_id"));
		apkInfo.nextReqTime = c.getLong(c.getColumnIndex("nextReqTime"));
		apkInfo.launcheTimeStart = c.getInt(c.getColumnIndex("launcheTimeStart"));
		apkInfo.launcheTimeEnd = c.getInt(c.getColumnIndex("launcheTimeEnd"));
		apkInfo.hadLaunchButNotUpLoad = c.getInt(c.getColumnIndex("hadLaunchButNotUpLoad"))== APKInfo.yes ? true : false;
		return apkInfo;
	}

	/**
	 * 查询所有记录
	 * @return
	 */
	public List<APKInfo> queryAll(){
		List<APKInfo> apkInfos = new ArrayList<APKInfo>();
		String sql = "select * from apk_info";
		Cursor c = db.rawQuery(sql, null);
		while(c.moveToNext()){
			apkInfos.add(cursor2APKInfo(c));
		}
		return apkInfos;
	}
	
	/**
	 * 
	 * @return 返回下次请求请求时间,如果数据库为空，则返回当前时间
	 */
	public long queryNextReqTime(){
		try {
			String sql = "SELECT nextReqTime FROM apk_info WHERE nextReqTime > 0 ORDER BY nextReqTime LIMIT 0,1";
			Cursor c = db.rawQuery(sql, null);
			if (c.moveToNext()) {
				long t = c.getLong(c.getColumnIndex("nextReqTime"));
				return t;
			}
		} catch(Exception e){
			Log.e(tag, e.getMessage());
		}
		return System.currentTimeMillis();
	}
	
	/**
	 * 查询所有已经成功启动、上传启动数据失败的记录
	 * @return
	 */
	public List<APKInfo> queryNotUpload(){
		List<APKInfo> apkInfos = new ArrayList<APKInfo>();
		
		String sql = "SELECT * FROM apk_info WHERE hadLaunchButNotUpLoad = 1";
		Cursor c = db.rawQuery(sql, null);
		while (c.moveToNext()) {
			apkInfos.add(cursor2APKInfo(c));
		}
		
		return apkInfos;
	}
	
	
	//-----------------------------------------------------
	//流量监控 数据库操作
	
	public void insertTraffic(TrafficModel trafficModel) {
		
		db.beginTransaction(); // 开始事务
		try {
			db.execSQL("INSERT INTO traffic_info VALUES(null, ?, ?,?,?)", new Object[] {
					trafficModel.pkgname, trafficModel.label,trafficModel.traffic,trafficModel.traffic_last});
			db.setTransactionSuccessful(); // 设置事务成功完成
		} catch(Exception e){
			Log.e(tag, e.getMessage());
		} finally {
			db.endTransaction(); // 结束事务
		}
	}
	
	
	public void insertTraffics(List<TrafficModel> trafficModels) {
		db.beginTransaction(); // 开始事务
		try {
			for (TrafficModel trafficModel : trafficModels) {
				db.execSQL("INSERT INTO traffic_info VALUES(null, ?, ?,?,?)", new Object[] {
						trafficModel.pkgname, trafficModel.label,trafficModel.traffic,trafficModel.traffic_last});
			}
			db.setTransactionSuccessful(); // 设置事务成功完成
		} catch(Exception e){
			Log.e(tag, e.getMessage());
		} finally {
			db.endTransaction(); // 结束事务
		}
	}
	
	
	
	public void updateTraffics(List<TrafficModel> trafficModels){
		db.beginTransaction(); // 开始事务
		try {
			for (TrafficModel trafficModel : trafficModels) {
				if (queryByPackageName(trafficModel.pkgname)) {//如果已经存在该记录则更新
					db.execSQL("UPDATE traffic_info set traffic = ? ,traffic_last = ? where pkgname =? ", new Object[] {
							trafficModel.traffic, trafficModel.traffic_last,trafficModel.pkgname});
				}else{//不存在则插入
					db.execSQL("INSERT INTO traffic_info VALUES(null, ?, ?,?,?)", new Object[] {
							trafficModel.pkgname, trafficModel.label,trafficModel.traffic,trafficModel.traffic_last});
				}
				
			}
			db.setTransactionSuccessful(); // 设置事务成功完成
		} catch(Exception e){
			Log.e(tag, e.getMessage());
		} finally {
			db.endTransaction(); // 结束事务
		}
	}
	/**
	 * 重启后 traffic_last 归零
	 */
	public void updateTraffics_last2Zero(){
		db.beginTransaction(); // 开始事务
		try {
			db.execSQL("UPDATE traffic_info set traffic_last = ? ", new Object[] {0l});
			db.setTransactionSuccessful(); // 设置事务成功完成
		} catch(Exception e){
			Log.e(tag, e.getMessage());
		} finally {
			db.endTransaction(); // 结束事务
		}
	}
	
	
	private boolean queryByPackageName(String pkgname){
		String sql = "select * from traffic_info where pkgname = ?";
		Cursor c = null;
		try {
			c = db.rawQuery(sql, new String[]{pkgname});
			if (c.getCount()>0) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			c.close();
		}
		return false;
	}
	public List<TrafficModel> queryTraffics(){
		String sql = "select _id,pkgname,label,traffic,traffic_last from traffic_info ";
		Cursor c = null;
		List<TrafficModel> lists = new ArrayList<TrafficModel>();
		try {
			c = db.rawQuery(sql, null);
			while(c.moveToNext()){
				TrafficModel model = new TrafficModel();
				model.id = c.getInt(0);
				model.pkgname = c.getString(1);
				model.label = c.getString(2);
				model.traffic = c.getLong(3);
				model.traffic_last = c.getLong(4);
				lists.add(model);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			c.close();
		}
		return lists;
	}
	
	public TrafficModel queryTraffic(String pkgname){
		String sql = "select _id,pkgname,label,traffic,traffic_last from traffic_info where pkgname = ?";
		Cursor c = null;
		TrafficModel model = null;
		try {
			c = db.rawQuery(sql, new String[]{pkgname});
			if(c.moveToNext()){
				model = new TrafficModel();
				model.id = c.getInt(0);
				model.pkgname = c.getString(1);
				model.label = c.getString(2);
				model.traffic = c.getLong(3);
				model.traffic_last = c.getLong(4);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			c.close();
		}
		return model;
	}

	public JSONArray queryTrafficInfo() {
		String sql = "select pkgname,label,traffic from traffic_info";
		Cursor c = db.rawQuery(sql, null);
		JSONArray array = new JSONArray();
		while(c.moveToNext()){
			JSONObject obj = new JSONObject();
			try {
				obj.put("pkgname", c.getString(0));
				obj.put("appname", c.getString(1));
				obj.put("traffic", c.getLong(2));
				array.put(obj);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return array;
	}
	
	/**
	 * close database
	 */
	public void closeDB() {
		db.close();
	}
}
