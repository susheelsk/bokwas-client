package com.bokwas.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.util.Log;

public class BokwasHttpClient {

	public static String postData(String url, List<BasicNameValuePair> params)
			throws Exception {
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(url);

		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		for (BasicNameValuePair pair : params) {
			nameValuePairs.add(pair);
		}
		httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

		HttpResponse response = httpclient.execute(httppost);
		String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
		Log.d("doPost : " + url, "Response : " + responseString);
		return responseString;
	}

}
