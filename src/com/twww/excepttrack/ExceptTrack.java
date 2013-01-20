/*
Copyright (c) 2011 BugSense.com

Permission is hereby granted, free of charge, to any person
obtaining a copy of this software and associated documentation
files (the "Software"), to deal in the Software without
restriction, including without limitation the rights to use,
copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the
Software is furnished to do so, subject to the following
conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
OTHER DEALINGS IN THE SOFTWARE.

Contributors:
Jon Vlachoyiannis
 */

package com.twww.excepttrack;


import java.io.BufferedReader;
import java.io.StringReader;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

public class ExceptTrack {

	// FIXME: Use Gson
	public static String createJSON(String app_package, String version, String phoneModel, String android_version, String stackTrace, String wifi_status, String mob_net_status, String gps_status, String[] screenProperties, Date occuredAt) throws Exception {
		JSONObject json = new JSONObject();

		JSONObject request_json = new JSONObject();
		JSONObject exception_json = new JSONObject();
		JSONObject application_json = new JSONObject();
		JSONObject client_json = new JSONObject();
		
		request_json.put("remote_ip", ""); 
		json.put("request", request_json);

		// stackTrace contains many info we need to extract
		BufferedReader reader = new BufferedReader(new StringReader(stackTrace));
		
		if (occuredAt == null)
			exception_json.put("occured_at", reader.readLine()); 
		else
			exception_json.put("occured_at", occuredAt);
		exception_json.put("message", reader.readLine()); //get message

		String exception_class = reader.readLine();
		exception_json.put("where", exception_class.substring(exception_class.lastIndexOf("(") + 1, exception_class.lastIndexOf(")")));  

		exception_json.put("klass", getClass(stackTrace));
		exception_json.put("backtrace", stackTrace);

		json.put("exception", exception_json);
		
		reader.close();

		application_json.put("phone", phoneModel);
		application_json.put("appver", version);
		application_json.put("appname", app_package);
		application_json.put("osver", android_version); //os_ver
		application_json.put("wifi_on", wifi_status);
		application_json.put("mobile_net_on", mob_net_status);
		application_json.put("gps_on", gps_status);
		application_json.put("screen:width", screenProperties[0]);
		application_json.put("screen:height", screenProperties[1]);
		application_json.put("screen:orientation", screenProperties[2]);
		application_json.put("screen_dpi(x:y)", screenProperties[3] + ":"+ screenProperties[4]);

		json.put("application_environment", application_json);

		client_json.put("version", "bugsense-version-0.6");
		client_json.put("name", "bugsense-android");
		json.put("client", client_json);

		return json.toString();
	}

	
    public static String MD5 (String data) throws Exception {
		MessageDigest m = MessageDigest.getInstance("MD5");

		m.update(data.getBytes(), 0, data.length());
		return new BigInteger(1, m.digest()).toString(16);
	}

	// FIXME: This need some optimizing
	public static String getClass(String in) {
		String out = "";
		int endOfFirstLine = in.indexOf(":");
		if (endOfFirstLine != -1 && endOfFirstLine+1 < in.length() ) {
			out = in.substring(0, endOfFirstLine);
		}
		return out;
	}

	public static void submitError(int sTimeout, Date occuredAt, final String stacktrace) throws Exception {
		//Modification to run off thread
		(new SubmitErrorTask()).execute(String.valueOf(sTimeout),stacktrace, occuredAt.toString());
	}
	

	//Update questions
protected static class SubmitErrorTask extends AsyncTask<String, Integer, Boolean>
	
	{
		@Override
		protected Boolean doInBackground(String...passedParams)
		{
			String stacktrace = passedParams[1];
			int sTimeout = Integer.valueOf(passedParams[0]);
			Date occuredAt = new Date(passedParams[2]);
			
			Log.d(G.TAG, "Transmitting stack trace: " + stacktrace);									
			// Transmit stack trace with POST request
			try {
			DefaultHttpClient httpClient = new DefaultHttpClient();
			/*HttpParams params = httpClient.getParams();

			 //Lighty 1.4 has trouble with the expect header
			 //(http://redmine.lighttpd.net/issues/1017), and a
			// potential workaround is only included in 1.4.21
			// (http://www.lighttpd.net/2009/2/16/1-4-21-yes-we-can-do-another-release).
			HttpProtocolParams.setUseExpectContinue(params, false);
			if (sTimeout != 0) {
				HttpConnectionParams.setConnectionTimeout(params, sTimeout);
				HttpConnectionParams.setSoTimeout(params, sTimeout);
			}*/
		
			HttpPost httpPost = new HttpPost(G.URL);
			//httpPost.addHeader("X-BugSense-Api-Key", G.API_KEY);
			//httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
			
			List <NameValuePair> nvps = new ArrayList <NameValuePair>();
			nvps.add(new BasicNameValuePair("data", createJSON(G.APP_PACKAGE, G.APP_VERSION, G.PHONE_MODEL, G.ANDROID_VERSION, stacktrace, ExceptTrackHandler.isWifiOn(), ExceptTrackHandler.isMobileNetworkOn(), ExceptTrackHandler.isGPSOn(), ExceptTrackHandler.ScreenProperties(), occuredAt)));
			nvps.add(new BasicNameValuePair("hash", MD5(stacktrace)));
			
			httpPost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
			
			
			HttpResponse response = httpClient.execute(httpPost);
			HttpEntity entity = response.getEntity();
			
			// maybe no internet? 
			// save to send another day
			if (entity == null) {
				throw new Exception("no internet connection maybe");
			}

		} catch (Exception e) {
			Log.e(G.TAG, "Error sending exception stacktrace", e);
			try {
				throw e;
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
			return null;
		}
		
	}



}