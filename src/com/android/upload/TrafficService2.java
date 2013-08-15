/*package com.android.upload;

import java.util.ArrayList;
import java.util.List;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.TrafficStats;
import android.os.IBinder;
import android.text.format.Formatter;

import com.android.db.DBManager;
import com.android.db.TrafficModel;
import com.android.util.LogUtil;
import com.android.util.PreferenceUtil;

public class TrafficService2 extends Service {
	static final String tag = "TrafficService";
	
	private DBManager dbManager;
	private AlarmManager am;
	private PendingIntent pi;
	private String action_listen_network = "action_listen_network";
	private AlarmReceiver alarmReceiver = null;
	
	PreferenceUtil prefUtil;
	public static final String key_last_traffic="key_last_traffic";
	public static final String key_traffic="key_traffic";
	
	String listenNetwork;
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		am = (AlarmManager)getSystemService(ALARM_SERVICE);
		alarmReceiver = new AlarmReceiver();
		dbManager = new DBManager(this);
		prefUtil = new PreferenceUtil(this);
		registerReceiver(alarmReceiver, new IntentFilter(action_listen_network));
	}
	
	@Override
	public void onDestroy() {
		if (alarmReceiver!=null) {
			unregisterReceiver(alarmReceiver);
		}
		//dbManager.closeDB();
		super.onDestroy();
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent==null) {
			return super.onStartCommand(intent, flags, startId);
		}
		if ("boot".equals(intent.getStringExtra("param_boot"))) {
			dbManager.updateTraffics_last2Zero();
			return super.onStartCommand(intent, flags, startId); 
		}
		
		listenNetwork = intent.getStringExtra("listenNetwork");
		LogUtil.log(tag, "onStartCommand!!listenNetwork="+listenNetwork);
		if ("true".equals(listenNetwork)) {
			
			queryTraffic();
			
			PackageManager pm=getPackageManager();
			List<PackageInfo> pinfos=pm.getInstalledPackages
	    			(PackageManager.GET_UNINSTALLED_PACKAGES | PackageManager.GET_PERMISSIONS);
			List<TrafficModel> models = new ArrayList<TrafficModel>();
			for(PackageInfo info:pinfos){
				String[] premissions=info.requestedPermissions;
	    		if(premissions!=null && premissions.length>0){
	    			for(String premission : premissions){
	    				if("android.permission.INTERNET".equals(premission)){
	    					int uId=info.applicationInfo.uid;
	    					long rx=TrafficStats.getUidRxBytes(uId);
	    					
	    					long tx=TrafficStats.getUidTxBytes(uId);
	    					if(rx<0 || tx<0){
	    						continue;
	    					}else{
	    						TrafficModel model = dbManager.queryTraffic(info.packageName);
	    						if (model == null) {
	    							model = new TrafficModel();
	    							model.pkgname = info.packageName;
	    							model.label = info.applicationInfo.loadLabel(pm).toString();
	    							model.traffic = 0;
	    							model.traffic_last = rx + tx;
	    							
	    							models.add(model);
								}
	    					}
	    				}
	    			}
	    		}
			}
			dbManager.insertTraffics(models);
			sendBroadcastCalcTraffic();
		}else if("false".equals(listenNetwork)){
			if (pi!=null) {
				am.cancel(pi);
			}
			sendBroadcastWithAM(action_listen_network, 0);
			
		}
		return super.onStartCommand(intent, flags, startId);
	}

	private void queryTraffic() {
		List<TrafficModel> lists = dbManager.queryTraffics();
		long traffic = 0;
		for (int i = 0; i < lists.size(); i++) {
			TrafficModel model = lists.get(i);
			if (model.traffic==0) {
				continue;
			}
			traffic += model.traffic;
			LogUtil.log(tag, model.label+":"+Formatter.formatFileSize(this, model.traffic));
		}
		LogUtil.log(tag, "总流量="+Formatter.formatFileSize(this, traffic));
		LogUtil.log(tag, "总流量2="+Formatter.formatFileSize(this, prefUtil.getLong(key_traffic, 0l)));
	}

	private void sendBroadcastCalcTraffic() {
		sendBroadcastWithAM(action_listen_network, 30*1000);
	}
	
	private void sendBroadcastWithAM(String action,long delayTimes){
		long start = System.currentTimeMillis();
		Intent i = new Intent(action);
		pi = PendingIntent.getBroadcast(TrafficService2.this, 0, i,PendingIntent.FLAG_UPDATE_CURRENT);
		LogUtil.log(tag, delayTimes+" later send broadcast! action="+action);
		am.set(AlarmManager.RTC_WAKEUP, start+delayTimes,pi);
	}
	
	class AlarmReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			LogUtil.log(tag, "action="+action);
			if (action_listen_network.equals(action)) {
				calcMobileTraffic();
				queryTraffic();
				if ("true".equals(listenNetwork)) {
					sendBroadcastCalcTraffic();
				}
			}
		}
		
	}


	public void calcMobileTraffic() {
		long crrTraffic = TrafficStats.getMobileRxBytes()+TrafficStats.getMobileTxBytes();
		long lastTraffic = prefUtil.getLong(key_last_traffic, 0l);
		long traffic = prefUtil.getLong(key_traffic, 0l);
		traffic += (crrTraffic - lastTraffic);
		
		prefUtil.setLong(key_traffic, traffic);
		prefUtil.setLong(key_last_traffic, crrTraffic);
		
		PackageManager pm=getPackageManager();
		List<PackageInfo> pinfos=pm.getInstalledPackages
    			(PackageManager.GET_UNINSTALLED_PACKAGES | PackageManager.GET_PERMISSIONS);
		List<TrafficModel> models = new ArrayList<TrafficModel>();
		for(PackageInfo info:pinfos){
			String[] premissions=info.requestedPermissions;
    		if(premissions!=null && premissions.length>0){
    			for(String premission : premissions){
    				if("android.permission.INTERNET".equals(premission)){
    					int uId=info.applicationInfo.uid;
    					long rx=TrafficStats.getUidRxBytes(uId);
    					
    					long tx=TrafficStats.getUidTxBytes(uId);
    					if(rx<0 || tx<0){
    						continue;
    					}else{
    						TrafficModel model = dbManager.queryTraffic(info.packageName);
    						if (model == null) {
    							model = new TrafficModel();
    							model.pkgname = info.packageName;
    							model.label = info.applicationInfo.loadLabel(pm).toString();
    							model.traffic = 0;
    							model.traffic_last = rx + tx;
    							
    							//dbManager.insertTraffic(model);
							}else{
								long tempTraffic = rx+tx;
								model.traffic += (tempTraffic - model.traffic_last);
								model.traffic_last = tempTraffic;
								
							}
    						models.add(model);
    					}
    				}
    			}
    		}
		}
		
		dbManager.updateTraffics(models);
	}
}
*/