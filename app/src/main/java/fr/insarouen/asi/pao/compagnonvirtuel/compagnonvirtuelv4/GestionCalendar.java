package fr.insarouen.asi.pao.compagnonvirtuel.compagnonvirtuelv4;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Objects;


/**
 *  Cette Classe est le module de gestion des fonctions liées au traitement du calendrier
 *  @author Guillaume Boutet
 *  @version 1.0
 */

public class GestionCalendar {


    /**
     * L'ID du calendrier associé au Compagnon Virtuel
     * */
    private long IDCalendrier;

    /**
     * Contexte de la classe , permettant d'utiliser les ContentProviders pour acceder au calendrier de l'appareil
     * */
    private Context context;
    private static final String LOGTAG = "DEBUG";


    /**
     * Le constructeur de GestionCalendar
     * @param appContext context de l'activité qui instancie le GestionCalendar.
     * */
    public GestionCalendar(Context appContext) {
        context = appContext;
        setCalendrierCompagnonVirtuel();

    }

    /** La méthode qui permet d'initialiser le calendrier associé au compagnon virtuel
     * trouve l'ID du calendrier correspondant si il existe , ou créé le calendrier si il n'existe pas deja.
     * */
    private void setCalendrierCompagnonVirtuel() {

        String[] lesCalendriers = new String[]{
                Calendars._ID,
                Calendars.NAME,
                Calendars.ACCOUNT_NAME,
                Calendars.ACCOUNT_TYPE};
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "la permission de lecture du calendrier n'est pas accordée", Toast.LENGTH_SHORT).show();
            return;
        }
        Cursor calCursor = context.getContentResolver().query(Calendars.CONTENT_URI, lesCalendriers, Calendars.VISIBLE + " = 1", null, Calendars._ID + " ASC");

        calCursor.moveToFirst();
        boolean calendarFound = false;
        do { // parcours des calendriers de l'appareil pour trouver l'id du calendrier du compagnon
            long id = calCursor.getLong(0);
            String name = calCursor.getString(1);
            if (Objects.equals(name, "CalendrierCompagnonVirtuel")) {
                IDCalendrier = id;
                calendarFound = true;
                Log.i(LOGTAG, "Calendrier trouvé, Id: |" + id + "| Nom : " + name);
            }

        } while (calCursor.moveToNext());
        if (!calendarFound) {// si on a pas trouvé le calendrier on le créé
            Uri target = Uri.parse(Calendars.CONTENT_URI.toString());
            target = target.buildUpon().appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                    .appendQueryParameter(Calendars.ACCOUNT_NAME, "CompagnonVirtuel")
                    .appendQueryParameter(Calendars.ACCOUNT_TYPE, "com.google").build();

            ContentValues values = new ContentValues();
            values.put(Calendars.ACCOUNT_NAME, "CompagnonVirtuel");
            values.put(Calendars.ACCOUNT_TYPE, "com.google");
            values.put(Calendars.NAME, "CalendrierCompagnonVirtuel");
            values.put(Calendars.CALENDAR_DISPLAY_NAME, "CalendrierCompagnonVirtuel");
            values.put(Calendars.CALENDAR_COLOR, 0x00FF00);
            values.put(Calendars.CALENDAR_ACCESS_LEVEL, Calendars.CAL_ACCESS_ROOT);
            values.put(Calendars.OWNER_ACCOUNT, "CompagnonVirtuel");
            values.put(Calendars.VISIBLE, 1);
            values.put(Calendars.SYNC_EVENTS, 1);
            values.put(Calendars.CALENDAR_TIME_ZONE, "Europe/France");

            context.getContentResolver().insert(target, values);

            lesCalendriers = new String[]{
                    Calendars._ID,
                    Calendars.NAME,
                    Calendars.ACCOUNT_NAME,
                    Calendars.ACCOUNT_TYPE};
            calCursor = context.getContentResolver().query(Calendars.CONTENT_URI, lesCalendriers, Calendars.VISIBLE + " = 1", null, Calendars._ID + " ASC");
            calCursor.moveToFirst();
            do {// on set la valeur d'IDCalendrier
                long id = calCursor.getLong(0);
                String name = calCursor.getString(1);
                if (Objects.equals(name, "CalendrierCompagnonVirtuel")) {
                    IDCalendrier = id;
                    Log.i(LOGTAG, "ID du calendrier ajouté |" + id);
                }
            } while (calCursor.moveToNext());
        }
        calCursor.close();

    }

    /**
     * Méthode qui permet de récuperer les evenements du calendrier
     * @return cursor itérant sur les evenements du calendrier
     * */
    public Cursor getEvents() {
        String[] projectionEvent = new String[]{"calendar_id", "title", "description",
                "dtstart", "dtend", "eventLocation", "_id"};

        Cursor cursor = context.getContentResolver().query(Uri.parse("content://com.android.calendar/events"),
                projectionEvent,
                null,
                null,
                null);
        return cursor;
    }

    /**
     * Méthode qui permet d'ajouter un rendez vous dans le calendrier .
     * @param titre l'intitulé du rendez vous
     * @param beginTime la date de debut du rdv
     * @param endTime la date de fin du rdv
     * */
    public void ajouterRDV(String titre, Calendar beginTime, Calendar endTime) {
        long startMillis = beginTime.getTimeInMillis();
        long endMillis = endTime.getTimeInMillis();

        ContentResolver cr = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(Events.DTSTART, startMillis);
        values.put(Events.DTEND, endMillis);
        values.put(Events.TITLE, titre);
        values.put(Events.EVENT_TIMEZONE, "France");
        values.put(Events.CALENDAR_ID, IDCalendrier);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "la permission d'ecrire dans le calendrier n'est pas accordée", Toast.LENGTH_SHORT).show();
            return;
        }
        cr.insert(Events.CONTENT_URI, values);
        Toast.makeText(context, "Le rendez-vous a bien été ajouté", Toast.LENGTH_SHORT).show();
    }

    /**
     * Méthode qui permet de supprimer un rendez vous dans le calendrier .
     * @param titre l'intitulé du rendez vous
     * @param beginDate la date de debut de recherche du rdv a supprimer
     * @param endDate la date de fin de recherche du rdv a supprimer
     * */
    public void supprimerRDV(String titre, Calendar beginDate,Calendar endDate){
        Cursor cursor =getEvents();
        long beginMillis=beginDate.getTimeInMillis();
        long endMillis=endDate.getTimeInMillis();
        cursor.moveToFirst();
        for (int i = 0;i< cursor.getCount();i++){
            if ((cursor.getLong(0)==IDCalendrier)&&(Objects.equals(cursor.getString(1), titre))&&(cursor.getLong(3)>beginMillis)&&(cursor.getLong(4)<endMillis)) {
                long eventID= cursor.getLong(6);
                Uri deleteUri = null;
                deleteUri = ContentUris.withAppendedId(Events.CONTENT_URI, eventID);
                context.getContentResolver().delete(deleteUri, null, null);
                Toast.makeText(context, "Le rendez-vous a été supprimé", Toast.LENGTH_SHORT).show();
            }
            cursor.moveToNext();
        }
        cursor.close();

    }
}
