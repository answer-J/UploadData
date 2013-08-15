package com.android.upload;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import com.android.util.CommonUtil;
import com.android.util.LogUtil;

public class StartReceiver extends BroadcastReceiver {
	private static final String tag = "StartReceiver";
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		LogUtil.log(tag, "action="+action);
		if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
			Intent i = new Intent(context, TrafficService.class);
			i.putExtra("param_boot", "boot");
			context.startService(i);
			
			context.startService(new Intent(context, StartService.class));
			
		}else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
			int type = CommonUtil.getNetworkType(context);
			//启动监听流量的service
			Intent i = new Intent(context, TrafficService.class);
			LogUtil.log(tag, "type="+type);
			//上传Service
			if (type>=0) {
				if (type==1) {
					context.startService(i);
				}
				context.startService(new Intent(context, StartService.class));
			}
		}else if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
			String packageName = intent.getData().getSchemeSpecificPart();
			Intent i = new Intent(context, StartService.class);
			i.putExtra(StartService.PARAM_KEY_UNINSTALLED_APK, packageName);
			context.startService(i);
		}
	}
	
	

}
