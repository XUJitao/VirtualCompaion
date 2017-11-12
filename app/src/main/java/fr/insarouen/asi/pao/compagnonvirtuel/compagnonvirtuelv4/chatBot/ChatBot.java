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

import android.content.ActivityNotFoundException;
import android.provider.AlarmClock;
import android.provider.Settings.Secure;

import java.io.StringReader;
import java.net.URI;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.ContactsContract;
import android.util.Log;


import fr.insarouen.asi.pao.compagnonvirtuel.compagnonvirtuelv4.GestionCalendar;
import fr.insarouen.asi.pao.compagnonvirtuel.compagnonvirtuelv4.MainActivity;
import fr.insarouen.asi.pao.compagnonvirtuel.compagnonvirtuelv4.ToolManager;

import static android.os.Build.VERSION_CODES.M;
import static android.webkit.ConsoleMessage.MessageLevel.LOG;
import static android.webkit.ConsoleMessage.MessageLevel.WARNING;
import static java.net.Proxy.Type.HTTP;

/**
 * Chatbot/VPA that uses the technology of Pandorabots to understand the user queries and provide information
 * in a specialized or general topic
 *
 * @author Michael McTear
 * @author Zoraida Callejas
 * @author Thibault PICARD
 * @author Lise Quesnel
 * @author Alexandre Levacher
 * @version 3.0, 15/11/15
 */

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ChatBot implements XMLAsyncResponse {

    private static final String LOGTAG = "DEBUG";

    /**
     * Le id propre à l'appareil de l'utilisateur
     */
    private String android_id;

    /**
     * L'ID propre au chatbot hebergé sur pandorabots qui est utilisé
     */
    String id = "f36afe5d1e378105"; //"bb9d8db85e36d4b9";	//Id of the agent in Pandorabots

    String specializedTopic = null;    //Whether the bot can hold a generic or specialized conversation
    String queryText = null;            //Query to be performed
    Exception exception = null;        //If there is an exception when obtaining the results from Pandorabots, it is saved here.
    //This way it is the class that uses the bot who has the responsibility to manage the exception (e.g. show a message to the user)

    /**
     * L'activité appelante
     */
    Activity callingActivity;

    /**
     * Le toolManager associé à la classe Bot;
     *
     * @see ToolManager
     */
    private ToolManager toolManager;

    /**
     * Constructor for a bot with a specialized topic
     *
     * @param _callingActivity Context for the creation of the bot
     * @param id               Id of the bot in the Pandorabots service
     * @param specializedTopic Topic of the conversation with the bot
     */
    public ChatBot(Activity _callingActivity, String id, String specializedTopic) {
        this.callingActivity = _callingActivity;
        this.id = id;
        this.specializedTopic = specializedTopic;
        this.android_id = Secure.getString(toolManager.getCallingActivity().getContentResolver(),
                Secure.ANDROID_ID);
    }

    /**
     * Constructor for a bot with a generic topic
     *
     * @param _toolManager le toolManager associé à la classe
     */
    public ChatBot(ToolManager _toolManager) {
        toolManager = _toolManager;
        this.callingActivity = toolManager.getCallingActivity();
        this.android_id = Secure.getString(toolManager.getCallingActivity().getContentResolver(),
                Secure.ANDROID_ID);
    }

    /**
     * Sends a text corresponding to the user input to the bot on the Pandorabots site.
     *
     * @param query user input
     */
    public void initiateQuery(String query) {

        RetrieveXMLTask retrieveXML = new RetrieveXMLTask();    //AsyncTask to retrieve the contents of the XML file fro mthe URL
        retrieveXML.delegate = this;    //It is crucial in order to retrieve the data from the asyncrhonous task (see the AsyncResponse and RetrieveXMLTask classes)

        String fullQuery;
        // insert %20 for spaces in query
        //query = query.replaceAll(" ", "%20");
        query = query.replaceAll(" ", "+");
        //Uses AIML files from A.L.I.C.E
        fullQuery = "https://www.pandorabots.com/pandora/talk-xml?input=" + query + "&botid=" + id + "&custid=" + android_id;
        Log.i(LOGTAG, "Query to pandorabots: " + fullQuery);

		/*
         * Start a background asynchronous query to Pandorabots,
		 * When this process is finished, the "processXMLContents" method is invoked (see below).
		 */
        retrieveXML.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, fullQuery, fullQuery); //An Executor that can be used to execute tasks in parallel.


        if (retrieveXML.getStatus() == (AsyncTask.Status.PENDING)) {
            //Indicates that the task has not been executed yet
            Log.i(LOGTAG, "Connecting to Pandorabots: Pending");
        } else if (retrieveXML.getStatus() == (AsyncTask.Status.RUNNING)) {
            //Indicates that the task is running
            Log.i(LOGTAG, "Connecting to Pandorabots: Running");
        } else if (retrieveXML.getStatus() == (AsyncTask.Status.FINISHED)) {
            //Indicates that AsyncTask.onPostExecute has finished
            Log.i(LOGTAG, "Connecting to Pandorabots: Finished");
        }
    }

    /**
     * Processes the results from Pandorabots in response to the query in the method <code>initiateQuery</code>
     */
    @Override
    public void processXMLContents(String result) {

        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            InputSource s = new InputSource(new StringReader(result));
            Document doc = dBuilder.parse(s);

            doc.getDocumentElement().normalize();
            XPathFactory factory = XPathFactory.newInstance();
            XPath xpath = factory.newXPath();

            // Result is from query to Bot on Pandorabots
            if (result.contains("<that>")) {
                String output = (String) xpath.evaluate("//that", doc, XPathConstants.STRING);
                output = output.replaceAll("<br> ", " ");
                processOutput(output);
                Log.i(LOGTAG, "processOutput called");
            } else {
                Log.d(LOGTAG, "ERREUR : la réponse ne contient pas de balise <that>.");
            }
        } catch (Exception e) {
            exception = e;
        }
    }

    /**
     * Parses the response from the Pandora service
     *
     * @param output la réponse du chatbot qu'il faut analyser
     * @throws Exception when the bot is not able to synthesize a message or the result cannot be parsed
     */
    public void processOutput(String output) throws Exception {

        if (output != null) {
			/*
			 * When <oob> tags are present, we assume that they have been marked up in the AIML
			 * file with one of the tags: <search>, <launch>, <url>, and <dial>.
			 *
			 * Nous avons rajouté le tag <animation>
			 */
            if (output.contains("<oob>")) {
                String[] parts = output.split("</oob>");
                String oob = parts[0];
                String textToSpeak = parts[1];

                String oobContent = oob.split("<oob>")[1].split("</oob>")[0];

                Log.d(LOGTAG, "Une action doit être effectuée : OOB = " + oobContent + " | Texte = " + textToSpeak);

                process_oobContent(oobContent, textToSpeak);

			/*
			 * If it does not, the only task for the bot is to parse and synthesize the response.
			 * For this, it must extract the message contained within the <that> tag.
			 */
            } else {
                Log.d(LOGTAG, "La réponse ne comporte que du texte à lire par le compagnon : " + output);
                toolManager.setAnswer(output);
                //myTts.speak(output, "EN");
            }
        } else
            throw new Exception("Invalid result from the bot");
    }

    /**
     * Processes the contents of the oob tag and carries out the corresponding action: synthesizing a message, carrying
     * out a web search, launching a web site, launching an app or dialing the phone
     *
     * @param oobContent  la partie correspondant aux actions à effectuer
     * @param textToSpeak la partie correspondant au texte à dire
     * @throws Exception
     */
    private void process_oobContent(String oobContent, String textToSpeak) throws Exception {
        //myTts.speak(textToSpeak, "EN");

        String query;

        if (oobContent.contains("<url>"))
            // perform a web search
            if (oobContent.contains("<search>")) { //cherche ou recherche
                queryText = oobContent.split("<search>")[1].split("</search>")[0];
                googleQuery(queryText);
            }

            // request to launch a web site named in input
            else { //ouvre
                query = "www." + (oobContent.split("<url>")[1].split("</url>")[0]).toLowerCase() + ".com";
                launchUrl(query);
            }


        // request to add an alarm clock
        if (oobContent.contains("<alarmclock>")) {
            if(oobContent.contains("<add>")) {
                addAnAlarm(oobContent);
            }
            else if (oobContent.contains("<delete>")){
                deleteAnAlarm(oobContent);
            }
        }

        // request to launch an app
        if (oobContent.contains("<launch>")) { //lance
            String app;
            app = oobContent.split("<launch>")[1].split("</launch>")[0];
            launchApp(app);
        }

        // request to send sms
        if (oobContent.contains("<send>")) { //'envoyer' or 'envoie'
            if (oobContent.contains("<people>")) {
                sendSMS(oobContent);
            }
        }

        // request to search a place in the map
        if (oobContent.contains("<maps>")) { // 'googlemap'
            String address = oobContent.split("<maps>")[1].split("</maps>")[0];
            launchGoogleMap(address);
        }

        // request to launch phone
        //appelle
        if (oobContent.contains("<phone>")) {
            if (oobContent.contains("<people>")) {
                makePhoneCall(oobContent);
            }
        }


        // request pour lancer une animation
        if (oobContent.contains("<animation>")) {
            String animation = oobContent.split("<animation>")[1].split("</animation>")[0];
            Log.i(LOGTAG, "On précise au toolManager que l'on veut lancer l'animation : |" + animation + "|");
            // On précise qu'il y a une animation a lancer
            toolManager.setAdditionalAnimation(animation);
            toolManager.setAnswer(textToSpeak);

        }

        // request to add an event in calendar
        if (oobContent.contains("<appointment>")) {
            GestionCalendar Gcal = new GestionCalendar(callingActivity.getApplicationContext());
            if (oobContent.contains("<next>")) {
                Gcal.prochainRDV();
            }
            else {
                if (oobContent.contains("<add>")) {
                    setAppointement(Gcal,oobContent,"</add>");
                }
                else if (oobContent.contains("<delete>")) {
                    setAppointement(Gcal,oobContent,"</delete>");
                }
            }
        }
    }

    /**
     * set change of appointment
     *
     * @param Gcal working calendar
     * @param oobContent appointment information
     * @param operationType type of operation
     */
    private void setAppointement(GestionCalendar Gcal,String oobContent, String operationType) {
        Calendar beginTime = Calendar.getInstance();
        beginTime.setTimeInMillis(System.currentTimeMillis());
        String title = setBeginTimeAndGetTitle(oobContent, beginTime, operationType);
        Calendar endTime = Calendar.getInstance();

        if (operationType.contains("</add>")) {
            endTime.setTime(beginTime.getTime());
            Gcal.ajouterRDV(title, beginTime, endTime);
        }
        else {
            endTime.set(beginTime.get(Calendar.YEAR), beginTime.get(Calendar.MONTH), beginTime.get(Calendar.DAY_OF_MONTH), 23, 59);
            Gcal.supprimerRDV(title, beginTime, endTime);
        }
    }

    /**
     * deal with oobContent to set begin time and get title of event
     *
     * @param oobContent information of date, time and event
     * @param beginTime system time
     * @param operationType the type of appointment operation
     * @return title of event
     */
    private String setBeginTimeAndGetTitle(String oobContent, Calendar beginTime, String operationType) {
        String date = oobContent.split("<eventdate>")[1].split("</eventdate>")[0];
        Log.i(LOGTAG, "addEvent: " + date);
        String title = "";
        int day, month, year, hour, minute;
        day = month = year = hour = minute = 0;
        int i = 0;


        String[] words = date.split(" ");
        String sDay = words.length == 0 ? date : words[i];
        try {
            day = Integer.parseInt(sDay);
            i++;
        }
        catch (Exception e) {
            day = beginTime.get(Calendar.DAY_OF_MONTH);
        }

        HashMap<String, Integer> months = new HashMap<>();
        months.put("janvier", 0);
        months.put("février", 1);
        months.put("mars", 2);
        months.put("avril", 3);
        months.put("mai", 4);
        months.put("juin", 5);
        months.put("juillet", 6);
        months.put("août", 7);
        months.put("septembre", 8);
        months.put("octobre", 9);
        months.put("novembre", 10);
        months.put("décembre", 11);
        try {
            month = months.get(words[i]);
            i++;
        } catch (Exception e) {
            month = beginTime.get(Calendar.MONTH);
        }
        try {
            year = Integer.parseInt(words[i]);
            i++;
        } catch (Exception e) {
            year = beginTime.get(Calendar.YEAR);
        }



        if(oobContent.contains("<eventtime>")) {
            String time = oobContent.split("<eventtime>")[1].split("</eventtime>")[0];
            String[] hr = time.split("h");
            try {
                hour = Integer.parseInt(hr[0]);
            }
            catch (Exception e) {
                hour = 0;
            }
            minute = hr.length > 1 ? Integer.parseInt(hr[1]) : 0;
            if(oobContent.contains("<event>")) {
                title = oobContent.split("<event>")[1].split("</event>")[0];
            }
            else title = oobContent.split("</eventtime>")[1].split(operationType)[0];
        }
        else {
            hour = beginTime.get(Calendar.HOUR_OF_DAY);
            minute = beginTime.get(Calendar.MINUTE);
            if (i < words.length) {
                title = date.substring(date.indexOf(words[i]));
            }
        }
        if(operationType.contains("</delete>")) {
            beginTime.set(year, month, day, 0, 0);
        }
        else beginTime.set(year, month, day, hour, minute);
        return title;
    }

    /**
     * Performs a Google search query. The value of specialized topic is appended to words in query
     *
     * @param googleSearchText The text to search
     */
    private void googleQuery(String googleSearchText) {
        // insert + for spaces in query
        googleSearchText = googleSearchText.replaceAll(" ", "+");
        String searchEngine = "https://www.google.com/search";
        String queryString = searchEngine + "?source=ig&rlz=&q=" + googleSearchText;
        launchUrl(queryString);
    }

    /**
     * Launches an app
     *
     * @param app name of the app
     * @throws Exception when the app cannot be launched
     */
    private void launchApp(String app) throws Exception {

        final PackageManager pm = callingActivity.getPackageManager();
        //get a list of installed apps.
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        String appToCall = null;
        for (ApplicationInfo packageInfo : packages) {
            if (packageInfo.packageName.contains(app.toLowerCase())) {
                if (pm.getLaunchIntentForPackage(packageInfo.packageName) != null) {
                    appToCall = packageInfo.packageName;
                    Log.d(LOGTAG, "L'application suivante est lancée : " + appToCall);
                    break;
                }
            }
        }
        Intent launchApp = pm.getLaunchIntentForPackage(appToCall);
        if (launchApp != null)
            callingActivity.startActivity(launchApp);
        else
            throw new Exception("Unable to launch " + app + " app");
    }

    /**
     * Launches a web page
     *
     * @param url url to the web page
     */
    private void launchUrl(String url) {
        if (!url.startsWith("http://") && !url.startsWith("https://"))
            url = "http://" + url;
        Log.d(LOGTAG, "L'URL suivante est lancée : " + Uri.parse(url));
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        callingActivity.startActivity(browserIntent);
    }

    /**
     * Launches Google Map
     *
     * @param address address to search
     */
    private void launchGoogleMap(String address) {
        address = "geo:0,0?q=" + address;
        address.replace(" ", "+");
        Uri uriAddress = Uri.parse(address);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(uriAddress);
        callingActivity.startActivity(intent);
    }

    /**
     * Send SMS
     *
     * @param oobContent information about people to send and content
     */
    private void sendSMS(String oobContent) {
        String sendSmsTo = oobContent.split("<people>")[1].split("</people>")[0];
        if (!Character.isLetter(sendSmsTo.charAt(0))) {
            if (oobContent.contains("<message>")) {
                String smsContent = oobContent.split("<message>")[1].split("</message>")[0];
                Log.d(LOGTAG, "send sms to" + sendSmsTo + " for " + smsContent);
                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + sendSmsTo));
                intent.setData(Uri.parse("sms_body" + smsContent));
                callingActivity.startActivity(intent);
            }
        } else {
            Cursor c = callingActivity.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
            String name, number = "";
            String numberToSendSms = "";
            String id;
            c.moveToFirst();
            boolean trouve = false;
            while (!trouve) {
                if (c.getString(0) != null) {


                    name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    id = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));
                    if (Integer.parseInt(c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                        Cursor pCur = callingActivity.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id},
                                null);
                        while (pCur.moveToNext()) {
                            number = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        }
                        pCur.close();
                    }


                    String[] words = (oobContent.split("<people>")[1].split("</people>")[0]).split(" ");
                    StringBuilder sb = new StringBuilder();
                    if (words[0].length() > 0) {
                        sb.append(Character.toUpperCase(words[0].charAt(0)) + words[0].subSequence(1, words[0].length()).toString().toLowerCase());
                        for (int j = 1; j < words.length; j++) {
                            sb.append(" ");
                            sb.append(Character.toUpperCase(words[j].charAt(0)) + words[j].subSequence(1, words[j].length()).toString().toLowerCase());
                        }
                    }
                    String nom = sb.toString();

                    if (name.equals(nom)) {
                        numberToSendSms = number;
                        trouve = true;
                    }
                }
                c.moveToNext();
            }

            c.close();


            if (oobContent.contains("<message>")) {
                String smsContent = oobContent.split("<message>")[1].split("</message>")[0];
                Log.d(LOGTAG, "send sms to " + sendSmsTo + " to say " + smsContent);
                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + numberToSendSms));
                intent.putExtra("sms_body", smsContent);
                callingActivity.startActivity(intent);
            }
        }
    }

    /**
     * make phone call
     *
     * @param oobContent names or phone number to call
     */
    private void makePhoneCall(String oobContent) {
        String a_appeller = oobContent.split("<people>")[1].split("</people>")[0];
        if (!Character.isLetter(a_appeller.charAt(0))) {
            Log.d(LOGTAG, "phone |" + oobContent);
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + a_appeller));
            callingActivity.startActivity(intent);
        } else {
            Cursor c = callingActivity.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
            String name, number = "";
            String numberToCall = "";
            String id;
            c.moveToFirst();
            boolean trouve = false;
            while (!trouve) {
                if (c.getString(0) != null) {


                    name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    id = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));
                    if (Integer.parseInt(c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                        Cursor pCur = callingActivity.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id},
                                null);
                        while (pCur.moveToNext()) {
                            number = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        }
                        pCur.close();
                    }


                    String[] words = (oobContent.split("<people>")[1].split("</people>")[0]).split(" ");
                    StringBuilder sb = new StringBuilder();
                    if (words[0].length() > 0) {
                        sb.append(Character.toUpperCase(words[0].charAt(0)) + words[0].subSequence(1, words[0].length()).toString().toLowerCase());
                        for (int j = 1; j < words.length; j++) {
                            sb.append(" ");
                            sb.append(Character.toUpperCase(words[j].charAt(0)) + words[j].subSequence(1, words[j].length()).toString().toLowerCase());
                        }
                    }
                    String nom = sb.toString();

                    if (name.equals(nom)) {
                        numberToCall = number;
                        trouve = true;
                    }
                }
                c.moveToNext();
            }

            c.close();


            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + numberToCall));
            callingActivity.startActivity(intent);

        }
    }

    /**
     * add an alarm
     *
     * @param oobContent time to set an alarm
     */
    private void addAnAlarm(String oobContent) {
        int hours = Integer.parseInt(oobContent.split("<add>")[1].split("h")[0]);
        String minu = oobContent.split("h")[1].split("</add>")[0];
        int minutes = minu.length() == 0 ? 0 : Integer.parseInt(minu);
        Log.i(LOGTAG, "minutes= " + minutes);
        Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM)
                .putExtra(AlarmClock.EXTRA_HOUR, hours)
                .putExtra(AlarmClock.EXTRA_MINUTES, minutes);
        if(oobContent.contains("<repetition>")){
            String days = oobContent.split("<repetition>")[1].split("</repetition>")[0];
            ArrayList<Integer> daysNumber = new ArrayList<>();
            if(days.contains("jours")) {
                for (int i = 1; i <= 7; i++)
                    daysNumber.add(i);
            } else {
                if (days.contains("dimanche")) daysNumber.add(1);
                if (days.contains("lundi")) daysNumber.add(2);
                if (days.contains("mardi")) daysNumber.add(3);
                if (days.contains("mercredi")) daysNumber.add(4);
                if (days.contains("jeudi")) daysNumber.add(5);
                if (days.contains("vendredi")) daysNumber.add(6);
                if (days.contains("samedi")) daysNumber.add(7);
            }
            intent.putExtra(AlarmClock.EXTRA_DAYS,daysNumber);
        }
        try {
            callingActivity.startActivity(intent);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * delete an alarm
     *
     * @param oobContent time to set an alarm
     */
    private void deleteAnAlarm(String oobContent) {
        int hours = Integer.parseInt(oobContent.split("<delete>")[1].split("h")[0]);
        int minutes = Integer.parseInt(oobContent.split("h")[1].split("</delete>")[0]);
        Intent intent = new Intent(AlarmClock.ACTION_DISMISS_ALARM)
                .putExtra(AlarmClock.EXTRA_HOUR, hours)
                .putExtra(AlarmClock.EXTRA_MINUTES, minutes);
        callingActivity.startActivity(intent);
    }
}
