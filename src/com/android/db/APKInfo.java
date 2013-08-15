package com.android.db;

import com.android.util.CommonUtil;


/**
 * 默认是未卸载，不需要激活
 * @author guorong
 *
 */
public class APKInfo {
	public int _id;
	public String appName;
	public String pkgName;
	public String version;
	public boolean uninstall;
	/**
	 * apk uninstall 
	 */
	public static final int yes = 1;
	/**
	 * apk not uninstall 
	 */
	public static final int no = 0;
	
	public boolean needActivate;
	/**
	 * 启动参数 "http开头"表示参数
	 */
	public String launchPara;
	/**
	 * ac serial number
	 */
	public String serial_id;
	/**
	 * 下次请求时间
	 */
	public long nextReqTime;
	/**
	 * 启动时间段开始时间
	 */
	public int launcheTimeStart;
	/**
	 * 启动时间段结束时间
	 */
	public int launcheTimeEnd = 24;
	
	/**
	 * 已经启动一次，但是 上传数据失败
	 */
	public boolean hadLaunchButNotUpLoad;
	
	public String uninstallTime = "";
	
	@Override
	public String toString() {
		return "APKInfo [_id=" + _id + ", pkgName=" + pkgName + ", version="
				+ version + ", uninstall=" + uninstall + ", needActivate="
				+ needActivate + ", launchPara=" + launchPara + ", serial_id="
				+ serial_id + ", nextReqTime=" + nextReqTime+"-->"+CommonUtil.getHumanDate(nextReqTime)
				+ ", launcheTimeStart=" + launcheTimeStart
				+ ", hadLaunchButNotUpLoad=" + hadLaunchButNotUpLoad
				+ ", launcheTimeEnd=" + launcheTimeEnd + "]";
	}


	public APKInfo(String pkgName, String version, boolean uninstall,
			boolean needActivate, String launchPara, String serial_id,
			long nextReqTime, int launcheTimeStart, int launcheTimeEnd) {
		super();
		this.pkgName = pkgName;
		this.version = version;
		this.uninstall = uninstall;
		this.needActivate = needActivate;
		this.launchPara = launchPara;
		this.serial_id = serial_id;
		this.nextReqTime = nextReqTime;
		this.launcheTimeStart = launcheTimeStart;
		this.launcheTimeEnd = launcheTimeEnd;
	}


	public APKInfo() {
	}
	
	
}
