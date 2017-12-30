/*
 *  Copyright 2013 Zoraida Callejas and Michael McTear
 *
 *  This file is part of the Sandra (Speech ANDroid Apps) Toolkit, from the book:
 *  Voice Application Development for Android, Michael McTear and Zoraida Callejas,
 *  PACKT Publishing 2013 <http://www.packtpub.com/voice-application-development-for-android/book>,
 *  <http://lsi.ugr.es/zoraida/androidspeechbook>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.

 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.

 *  You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package fr.insarouen.asi.pao.compagnonvirtuel.compagnonvirtuelv4.chatBot;

        import java.io.BufferedReader;
        import java.io.IOException;
        import java.io.InputStream;
        import java.io.InputStreamReader;
        import java.net.HttpURLConnection;
        import java.net.URL;
        import java.net.URLEncoder;
        import java.nio.charset.Charset;
        import java.security.NoSuchAlgorithmException;

        import android.app.Activity;
        import android.content.Context;
        import android.os.AsyncTask;
        import android.util.Log;

        import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
        import com.google.android.gms.common.GooglePlayServicesRepairableException;
        import com.google.android.gms.security.ProviderInstaller;

        import javax.net.ssl.HttpsURLConnection;
        import javax.net.ssl.SSLContext;


/**
 * Asynchronous task to retrieve the XML code from an URL.
 * Parameters:  <input, progress, result>
 * 		- It receives a collection of Strings as input parameters
 * 		- It does not produce any type of progress values (void)
 * 		- It produces a String as a result of the background computation
 *
 * @author Zoraida Callejas
 * @author Michael McTear
 * @version 1.3, 08/18/13
 */

public class RetrieveXMLTask extends AsyncTask<String, Void, String> {

    private static final String LOGTAG = "DEBUG";

    public XMLAsyncResponse delegate=null; 	//Object employed to send the results back to the invoking activity

    /*
     * Object used to account for the exceptions that may happen during the background computation.
     *
     * It is not possible to throw Exceptions in the doInBackground method because it must have exactly
     * the same header specified in the AsyncTask class (and thus we cannot add "throws Exception")
     */
    public Exception exception = null;

    private Activity activity;

    public RetrieveXMLTask(Activity activity) {
        this.activity = activity;
    }

    /**
     * Writes a string with the contents of the file in the specified URL
     * Modified from the method with the same name in http://www.edumobile.org/android/android-programming-tutorials/data-fetching/
     *
     * @note Pay attention to include the Internet permission in your manifest: (<uses-permission android:name="android.permission.INTERNET" /> )
     *
     * @note We use HttpURLConnection (from Android), though it is also possible to use DefaultHttpClient (from Apache). For new apps it is recommended to use HttpURLconnection better
     * (the android documentation redirects to this blog: http://android-developers.blogspot.com/2011/09/androids-http-clients.html)
     */
    private String saveXmlInString(String urlString)
    {
        InputStream in = null;
        int response = -1;
        String result=null;
        initializeSSLContext(this.activity);
        URL url=null;
        HttpsURLConnection connection=null;

        try {
            urlString = URLEncoder.encode(urlString, "UTF-8");
            urlString = urlString.replaceAll("%3A", ":");
            urlString = urlString.replaceAll("%2F", "/");
            urlString = urlString.replaceAll("%3F", "?");
            urlString = urlString.replaceAll("%3D", "=");
            urlString = urlString.replaceAll("%26", "&");
            url = new URL(urlString);
            Log.i(LOGTAG,"url is : " + url.toString());
            connection = (HttpsURLConnection) url.openConnection();
            Log.i(LOGTAG, "Connection opened successfully");
            connection.setAllowUserInteraction(false);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("Accept-Charset", "UTF-8");
            connection.setRequestMethod("GET");


            connection.connect();
            Log.i(LOGTAG, "Connected SUCCESSFULLY!!!!!!!!");
            response = connection.getResponseCode();

            Log.i(LOGTAG, "HTTP connection staus" + response);
            if (response == HttpURLConnection.HTTP_OK || response == 301) {
                in = connection.getInputStream();

            }

            result = readStreamToString(in);

	     	 /*
	 	     * http://developer.android.com/reference/java/net/HttpURLConnection.html
	 	     * Once the response body has been read, the HttpURLConnection should be closed by calling disconnect().
	 	     * Disconnecting releases the resources held by a connection so they may be closed or reused.
	 	     */
            connection.disconnect();
        }
        catch (Exception ex)
        {
            Log.i(LOGTAG,"############################################################");
            ex.printStackTrace();
            exception = ex;
        }

        return result;
    }


    /**
     * Creates a string with the contents read from an input stream
     * Follows the method suggested here: http://stackoverflow.com/questions/2492076/android-reading-from-an-input-stream-efficiently
     * @throws Exception When the inputstream is null or there is an error while reading it
     */
    private String readStreamToString(InputStream in) throws IOException {

        if(in==null){
            throw new IOException("InputStream could not be read (in==null)");
        }

        Charset inputCharset = Charset.forName("UTF-8");
        BufferedReader bufRead = new BufferedReader(new InputStreamReader(in,inputCharset));
        StringBuilder text = new StringBuilder();
        String line;

        while ((line = bufRead.readLine()) != null)
            text.append(line); //May throw an IOException
        Log.i(LOGTAG,"readStremToString called: "+ text.toString());
        return text.toString();

    }


    /**
     * Sends the results back to the invoking activity using the AsyncResponse instance "delegate" (see the AsyncResponse class).
     *
     * It is invoked when the background computation is finished.
     */
    @Override
    public void onPostExecute(String xml) {
        if(exception==null) {
            delegate.processXMLContents(xml);
            Log.i(LOGTAG, "processXMLContents called successfully");
        }
        else {
            delegate.processXMLContents("NetworkException - " + exception.getMessage());
            Log.i(LOGTAG, "NetworkException - " + exception.getMessage());
        }
    }


    /**
     * Saves the contents of a file in the specified URL in a string
     * @params urls urls[0] is the url provided by the user, urls[1] is a predefined url that can be used in case the first one is not available
     */
    @Override
    public String doInBackground(String... urls) {

        String xml_contents;
        xml_contents = saveXmlInString(urls[0]);
        Log.i(LOGTAG, xml_contents);

        if(exception!=null)
            xml_contents = saveXmlInString(urls[1]);

        return xml_contents;
    }


    /**
     * Initialize SSL
     * @param mContext
     */
    public void initializeSSLContext(Context mContext){
        try {
            SSLContext.getInstance("TLSv1.2");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            ProviderInstaller.installIfNeeded(mContext.getApplicationContext());
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }
}