package com.android.util.http;

import java.io.InputStream;

public interface HttpCallback {
	public void onOk(InputStream in);
	public void onError(int code,Exception e);
}
