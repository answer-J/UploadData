package android.net;

import java.net.InetAddress;

public class ConnectivityManager {
	public static final String CONNECTIVITY_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
	public static final String CONNECTIVITY_ACTION_IMMEDIATE = "android.net.conn.CONNECTIVITY_CHANGE_IMMEDIATE";
	public static final String EXTRA_NETWORK_INFO = "networkInfo";
	public static final String EXTRA_IS_FAILOVER = "isFailover";
	public static final String EXTRA_OTHER_NETWORK_INFO = "otherNetwork";
	public static final String EXTRA_NO_CONNECTIVITY = "noConnectivity";
	public static final String EXTRA_REASON = "reason";
	public static final String EXTRA_EXTRA_INFO = "extraInfo";
	public static final String EXTRA_INET_CONDITION = "inetCondition";
	public static final String ACTION_BACKGROUND_DATA_SETTING_CHANGED = "android.net.conn.BACKGROUND_DATA_SETTING_CHANGED";
	public static final String INET_CONDITION_ACTION = "android.net.conn.INET_CONDITION_ACTION";
	public static final String ACTION_TETHER_STATE_CHANGED = "android.net.conn.TETHER_STATE_CHANGED";
	public static final String EXTRA_AVAILABLE_TETHER = "availableArray";
	public static final String EXTRA_ACTIVE_TETHER = "activeArray";
	public static final String EXTRA_ERRORED_TETHER = "erroredArray";
	public static final int TYPE_NONE = -1;
	public static final int TYPE_MOBILE = 0;
	public static final int TYPE_WIFI = 1;
	public static final int TYPE_MOBILE_MMS = 2;
	public static final int TYPE_MOBILE_SUPL = 3;
	public static final int TYPE_MOBILE_DUN = 4;
	public static final int TYPE_MOBILE_HIPRI = 5;
	public static final int TYPE_WIMAX = 6;
	public static final int TYPE_BLUETOOTH = 7;
	public static final int TYPE_DUMMY = 8;
	public static final int TYPE_ETHERNET = 9;
	public static final int TYPE_MOBILE_FOTA = 10;
	public static final int TYPE_MOBILE_IMS = 11;
	public static final int TYPE_MOBILE_CBS = 12;
	public static final int TYPE_WIFI_P2P = 13;
	public static final int MAX_RADIO_TYPE = TYPE_WIFI_P2P;
	public static final int MAX_NETWORK_TYPE = TYPE_WIFI_P2P;
	public static final int DEFAULT_NETWORK_PREFERENCE = TYPE_WIFI;

	public static boolean isNetworkTypeValid(int networkType) {
		return networkType >= 0 && networkType <= MAX_NETWORK_TYPE;
	}

	/** {@hide} */
	public static String getNetworkTypeName(int type) {
		return Integer.toString(type);
	}

	/** {@hide} */
	public static boolean isNetworkTypeMobile(int networkType) {
		return false;
	}

	public void setNetworkPreference(int preference) {
	}

	public int getNetworkPreference() {
		return 0;
	}

	/*class NetworkInfo {
	}*/

	public NetworkInfo getActiveNetworkInfo() {
		return null;
	}

	/** {@hide} */
	public NetworkInfo getActiveNetworkInfoForUid(int uid) {
		return null;
	}

	public NetworkInfo getNetworkInfo(int networkType) {
		return null;
	}

	public NetworkInfo[] getAllNetworkInfo() {
		return null;
	}

//	/** {@hide} */
//	public LinkProperties getActiveLinkProperties() {
//		return null;
//	}

	/** {@hide} */
//	public LinkProperties getLinkProperties(int networkType) {
//		return null;
//	}

	/** {@hide} */
	public boolean setRadios(boolean turnOn) {
		return turnOn;
	}

	/** {@hide} */
	public boolean setRadio(int networkType, boolean turnOn) {
		return turnOn;
	}

	public int startUsingNetworkFeature(int networkType, String feature) {
		return networkType;
	}

	public int stopUsingNetworkFeature(int networkType, String feature) {
		return networkType;
	}

	public boolean requestRouteToHost(int networkType, int hostAddress) {
		return false;
	}

	public boolean requestRouteToHostAddress(int networkType,
			InetAddress hostAddress) {
		return false;
	}

	@Deprecated
	public boolean getBackgroundDataSetting() {
		return true;
	}

	@Deprecated
	public void setBackgroundDataSetting(boolean allowBackgroundData) {
	}

//	public NetworkQuotaInfo getActiveNetworkQuotaInfo() {
//		return null;
//
//	}

	public boolean getMobileDataEnabled() {
		return false;
	}

	public void setMobileDataEnabled(boolean enabled) {
	}

	class Context {
	}

	public static ConnectivityManager from(Context context) {
		return new ConnectivityManager();
	}

	public String[] getTetherableIfaces() {
		return null;
	}

	public String[] getTetheredIfaces() {
		return null;
	}

	public String[] getTetheringErroredIfaces() {
		return null;
	}

	public int tether(String iface) {
		return 0;
	}

	public int untether(String iface) {
		return 0;
	}

	public boolean isTetheringSupported() {
		return false;
	}

	public String[] getTetherableUsbRegexs() {
		return null;
	}

	public String[] getTetherableWifiRegexs() {
		return null;
	}

	public String[] getTetherableBluetoothRegexs() {
		return null;
	}

	public int setUsbTethering(boolean enable) {
		return 0;
	}

	/** {@hide} */
	public static final int TETHER_ERROR_NO_ERROR = 0;
	/** {@hide} */
	public static final int TETHER_ERROR_UNKNOWN_IFACE = 1;
	/** {@hide} */
	public static final int TETHER_ERROR_SERVICE_UNAVAIL = 2;
	/** {@hide} */
	public static final int TETHER_ERROR_UNSUPPORTED = 3;
	/** {@hide} */
	public static final int TETHER_ERROR_UNAVAIL_IFACE = 4;
	/** {@hide} */
	public static final int TETHER_ERROR_MASTER_ERROR = 5;
	/** {@hide} */
	public static final int TETHER_ERROR_TETHER_IFACE_ERROR = 6;
	/** {@hide} */
	public static final int TETHER_ERROR_UNTETHER_IFACE_ERROR = 7;
	/** {@hide} */
	public static final int TETHER_ERROR_ENABLE_NAT_ERROR = 8;
	/** {@hide} */
	public static final int TETHER_ERROR_DISABLE_NAT_ERROR = 9;
	/** {@hide} */
	public static final int TETHER_ERROR_IFACE_CFG_ERROR = 10;

	public int getLastTetherError(String iface) {
		return 0;
	}

	public boolean requestNetworkTransitionWakelock(String forWhom) {
		return false;
	}

	public void reportInetCondition(int networkType, int percentage) {
	}

	class PointerProperties {
	}

	public void setGlobalProxy(PointerProperties p) {
	}

	public PointerProperties getGlobalProxy() {
		return null;
	}

	public PointerProperties getProxy() {
		return null;
	}

	public void setDataDependency(int networkType, boolean met) {
	}

	public boolean isNetworkSupported(int networkType) {
		return false;
	}

	public boolean isActiveNetworkMetered() {
		return false;
	}
}