package com.bokwas.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
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
		String responseString = EntityUtils.toString(response.getEntity(),
				"UTF-8");
		Log.d("doPost : " + url, "Response : " + responseString);
		return responseString;
	}

	public static String getData(String url, List<BasicNameValuePair> params) {
		String responseString;
		try {
			HttpClient httpclient = new DefaultHttpClient();

			if (params != null) {
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				for (BasicNameValuePair pair : params) {
					nameValuePairs.add(pair);
				}

				String paramString = URLEncodedUtils.format(nameValuePairs,
						"utf-8");
				url += paramString;
			}
			HttpGet httpGet = new HttpGet(url);

			HttpResponse response = httpclient.execute(httpGet);
			responseString = EntityUtils
					.toString(response.getEntity(), "UTF-8");
			Log.d("BokwasHttpClient" + url, "Response : " + responseString);
			return responseString;
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "";
	}

}
