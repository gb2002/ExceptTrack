/*
Copyright (c) 2009 nullwire aps

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
Mads Kristiansen, mads.kristiansen@nullwire.com
Glen Humphrey
Evan Charlton
Peter Hewitt
*/

package com.twww.excepttrack;

public class G {
	// Since the exception handler doesn't have access to the context,
	// or anything really, the library prepares these values for when
	// the handler needs them.
	public static String FILES_PATH 				= "null";
	public static String APP_VERSION 				= "unknown";
	public static String APP_PACKAGE 				= "unknown";
	//public static String URL						= "http://10.39.255.98:8080/api/errors";
	public static String URL						= "http://www.twww.com/bugsense/";
	public static String TAG						= "BugSenseHandler";
	public static String ANDROID_VERSION			= null;
	public static String PHONE_MODEL				= null;
	public static String PHONE_BRAND				= null;
	public static String API_KEY					= null;

	public static String TraceVersion				= "0.6.0";
}