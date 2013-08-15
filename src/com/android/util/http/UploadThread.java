package com.android.util.http;

import java.util.Map;

public class UploadThread extends Thread {
	private String url;
	private Map<String, String> params;
	private HttpCallback callback;
	
	public UploadThread(String url, Map<String, String> params,
			HttpCallback callback) {
		super();
		this.url = url;
		this.params = params;
		this.callback = callback;
	}

	@Override
	public void run() {
		new HttpUtil().doPost(url, params, callback);
	}
}
