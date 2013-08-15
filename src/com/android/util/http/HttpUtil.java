package com.android.util.http;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import com.android.util.LogUtil;

public class HttpUtil {
	private DefaultHttpClient client;
	private final static String ENCODING_DEFAULT = "UTF-8";
	public static final String URL_REQ_LAUNCH = "http://a.andrioddaquan.com/getactiverules.aspx"; //请求启动
	public static final String URL_LAUNCH_COMPLETE = "http://a.andrioddaquan.com/completemsg.aspx"; //上传数据
	
	public static final String URL_UPLOAD_APP = "http://a.andrioddaquan.com/AppInstallInfo.aspx"; //上传应用
	public static final String URL_UPLOAD_TRAFFIC = "http://a.andrioddaquan.com/AppFlowInfo.aspx";//上传流量
	
	public static final String URL_UPLOAD_STACK_TRACE = "http://a.andrioddaquan.com/SysInfo.aspx";//上传异常信息
	public HttpUtil() {
		HttpParams params = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(params, 25000);
		HttpConnectionParams.setSoTimeout(params, 25000);
		client = new DefaultHttpClient(params);
	}
	
	public void doPost(String url, Map<String, String> params,HttpCallback callback){
//		Debug.waitForDebugger();
		LogUtil.log("http", url);
		HttpPost post = new HttpPost(url);
		List<NameValuePair> listParams = new ArrayList<NameValuePair>();
		if (params!=null) {
			for(Entry<String, String> entry : params.entrySet()){
				listParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
			}
		}
		int code = 0;
		try {
			post.setEntity(new UrlEncodedFormEntity(listParams, ENCODING_DEFAULT));
			HttpResponse response = client.execute(post);
			code = response.getStatusLine().getStatusCode();
			LogUtil.log("http", "code="+code);
			if (code == 200) {
				callback.onOk(response.getEntity().getContent());
			}else{
				callback.onError(code,new Exception());
			}
		} catch (IOException e) {
			callback.onError(code,e);
			LogUtil.log("http", "code="+code+"\t"+LogUtil.getExceptionTrace(e));
		}finally{
			client.getConnectionManager().shutdown();
		}
	}
	
	
}
