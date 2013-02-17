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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;
import android.os.AsyncTask;
import android.util.Log;

public class ExceptTrack {
	
	/**
	 * Build the Device properties section of the submission
	 * 
	 * @return JSONOBJect of phone properties
	 */
	public static JSONObject buildJSONDeviceProperties()
	{
		JSONObject application_json = new JSONObject();
		String[] screenProperties = ExceptTrackHandler.ScreenProperties();
		
		try {
			application_json.put("uniqueId", G.UNIQUE_ID);
			application_json.put("phone", G.PHONE_MODEL);
			application_json.put("appVer", G.APP_VERSION);
			application_json.put("appName", G.APP_PACKAGE);
			application_json.put("osVer", G.ANDROID_VERSION); //os_ver
			application_json.put("wifi_on", ExceptTrackHandler.isWifiOn());
			application_json.put("mobile_net_on",ExceptTrackHandler.isMobileNetworkOn());
			application_json.put("gps_on", ExceptTrackHandler.isGPSOn());
			application_json.put("screenWidth", screenProperties[0]);
			application_json.put("screenHeight", screenProperties[1]);
			application_json.put("screenOrientation", screenProperties[2]);
			application_json.put("screenDpiXY", screenProperties[3] + ":"+ screenProperties[4]);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		
		return application_json;
		
	}
	
	public static JSONObject buildJSONException(String stackTrace, String occuredAt)
	{
		JSONObject exception_json = new JSONObject();
		
		
		try {
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
			reader.close();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		return exception_json;
		
	}
	public static JSONObject buildJSONLog(String stackTrace, String occuredAt)
	{
		JSONObject log_json = new JSONObject();
		
		
		try {
			log_json.put("occured_at", occuredAt);
			log_json.put("message", "N/A"); //get message
			log_json.put("where","N/A");  
			log_json.put("klass", "N/A");
			log_json.put("backtrace", stackTrace);
	
		} catch (JSONException e) {
			e.printStackTrace();
		} 
		
		return log_json;
		
	}
	public static String createStackTraceJSON(String stackTrace, String occuredAt, boolean stacktrace) throws Exception {
		JSONObject json = new JSONObject();
		JSONObject application_json = buildJSONDeviceProperties();
		JSONObject request_json = new JSONObject();
		JSONObject exception_json=null;
		if (stacktrace)
		{
			exception_json = buildJSONException(stackTrace, occuredAt);	
		}
		else
		{
			exception_json = buildJSONLog(stackTrace, occuredAt);
			
		}
		
		JSONObject client_json = new JSONObject();
		json.put("request", request_json);
		json.put("exception", exception_json);
		json.put("application_environment", application_json);
		client_json.put("version", G.LIBRARY_VERSION);
		client_json.put("name", G.LIBRARY_NAME);
		json.put("client", client_json);

		return json.toString();
	}

    public static String MD5 (String data) throws Exception {
		MessageDigest m = MessageDigest.getInstance("MD5");

		m.update(data.getBytes(), 0, data.length());
		return new BigInteger(1, m.digest()).toString(16);
	}

	public static String getClass(String in) {
		String out = "";
		int endOfFirstLine = in.indexOf(":");
		if (endOfFirstLine != -1 && endOfFirstLine+1 < in.length() ) {
			out = in.substring(0, endOfFirstLine);
		}
		return out;
	}
	
/**
 * Submits  log and handles error generation and uses current date
 * 
 * 
 */
	public static void submitLog() {
		
		Date currentDateTime = new Date();
		try {
			submitLog(currentDateTime);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	/**
	 * Submits message and handles error generation and uses current date
	 * @param message What you want to post to the exception tracker
	 * 
	 */
		public static void submitMessage(String message) {
			
			Date currentDateTime = new Date();
			try {
				submitMessage(currentDateTime,message);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	/**
	 * Submits log with supplied date and also throws exception if their is a problem	
	 * @param occuredAt
	 * @throws Exception
	 */
	public static void submitLog(Date occuredAt) throws Exception {
		//Modification to run off thread
		//Convert Date to string
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String occuredAtString = df.format(occuredAt);
		String logfile = readLog();
		String results = createStackTraceJSON(logfile,occuredAtString,false);
		(new SubmitErrorTask()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,null,results);
	}
	
	
	/**
	 * Submits message with supplied date and also throws exception if their is a problem	
	 * @param message What you want to post to the exception tracker
	 * @throws Exception
	 */
	public static void submitMessage(Date occuredAt,String message) throws Exception {
		//Modification to run off thread
		//Convert Date to string
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String occuredAtString = df.format(occuredAt);
		String results = createStackTraceJSON(message,occuredAtString,false);
		(new SubmitErrorTask()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,null,results);
	}
	

	public static void submitError(int sTimeout, Date occuredAt, final String stacktrace) throws Exception {
		//Modification to run off thread
		//Convert Date to string
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String occuredAtString = df.format(occuredAt);
		String results = createStackTraceJSON(stacktrace,occuredAtString,true);
		
		(new SubmitErrorTask()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,String.valueOf(sTimeout),results);
	}
	
private static String readLog()
{
	StringBuilder log=new StringBuilder();
	 try {
	      Process process = Runtime.getRuntime().exec("logcat -d");
	      BufferedReader bufferedReader = new BufferedReader(
	      new InputStreamReader(process.getInputStream()));
	                       
	      
	      String line;
	      while ((line = bufferedReader.readLine()) != null) {
	        log.append(line +"\n");
	      }
	     
	    } catch (IOException e) {
	    }
	
	return log.toString();
	
}
	//Update questions
protected static class SubmitErrorTask extends AsyncTask<String, Integer, Boolean>
	
	{
		@Override
		protected Boolean doInBackground(String...passedParams)
		{
			
			//Log.d(G.TAG, "Transmitting stack trace: " + stacktrace);	
			Log.d(G.TAG, "Host " + G.URL);	
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
			nvps.add(new BasicNameValuePair("data",passedParams[1]));
			nvps.add(new BasicNameValuePair("hash", MD5(passedParams[1])));
			
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
				e1.printStackTrace();
			}
		}
			return null;
		}
		
	}



}