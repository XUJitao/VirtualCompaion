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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;

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
     * Méthode qui permet de traiter une requete dans le cas de la gestion d'un calendrier
     * @param requete la requete interceptée
     * @return Un code decrivant le bon déroulement ou non du traitement de la requete
     * */
    public int traiterRequete(String requete) {
        if (!(requete.matches("ajoute un rendez-vous le [0-3]?[0-9](.*)[1-2][0-9][0-9][0-9] à [0-9]?[0-9]h[0-9]?[0-9]?(.*)")
                | requete.matches("supprime un rendez-vous le [0-3]?[0-9](.*)[1-2][0-9][0-9][0-9] intitulé (.*)")
                | (requete.contains("rendez-vous") && requete.contains("prochain")))) {// regex precisant la forme d'une requete correcte
            return 0; // requete non conforme
        } else {
            int mois = 12;
            int jour = 32;
            int year = 1970;
            int heure = 24;
            int minutes = 60;
            String[] lesMois = {"janvier", "fevrier", "mars", "avril", "mai", "juin", "juillet", "août", "septembre",
                    "octobre", "novembre", "décembre"};
            String[] requeteSplit = requete.split(" ");
            if (Objects.equals(requeteSplit[0], ("ajoute"))) {
                //cas d'ajout d'un rdv , set les parametres de l'ajout
                int indiceDebutTitre = requeteSplit.length + 1;
                String titre = "";
                for (int j = 0; j < requeteSplit.length; j++) {
                    for (int i = 0; i < 12; i++) {
                        if (Objects.equals(requeteSplit[j], lesMois[i])) {
                            mois = i;
                            jour = Integer.parseInt(requeteSplit[j - 1]);
                            year = Integer.parseInt(requeteSplit[j + 1]);
                            String[] hhmm = (requeteSplit[j + 3]).split("h");
                            if (hhmm.length == 1) {
                                heure = Integer.parseInt(hhmm[0]);
                                minutes = 0;
                            } else if (hhmm.length == 2) {
                                heure = Integer.parseInt(hhmm[0]);
                                minutes = Integer.parseInt(hhmm[1]);
                            }
                            indiceDebutTitre = j + 4;

                        }
                    }
                    if (j >= indiceDebutTitre) {
                        titre = titre + requeteSplit[j] + " ";
                    }
                }


                if (mois > 11 || mois < 0 || jour < 1 || jour > 31 || year < 1971 || heure < 0 || heure > 23) {
                    return 2;
                }// date invalide
                Calendar beginTime = Calendar.getInstance();
                beginTime.set(year, mois, jour, heure, minutes);
                Calendar endTime = Calendar.getInstance();
                endTime.set(year, mois, jour, heure + 1, minutes);
                ajouterRDV(titre, beginTime, endTime);
                return 1;// la requete a bien été traitée
            } else if (Objects.equals(requeteSplit[0], "supprime")) {
                // cas d'une suppression de rdv , set les parametres de la suppression
                int indiceDebutTitre = requeteSplit.length + 1;
                String titre = "";
                for (int j = 0; j < requeteSplit.length; j++) {
                    for (int i = 0; i < 12; i++) {
                        if (Objects.equals(requeteSplit[j], lesMois[i])) {
                            mois = i;
                            jour = Integer.parseInt(requeteSplit[j - 1]);
                            year = Integer.parseInt(requeteSplit[j + 1]);
                            indiceDebutTitre = j + 3;
                        }
                    }
                    if (j >= indiceDebutTitre) {
                        titre = titre + requeteSplit[j] + " ";
                    }
                }
                if (mois > 11 || mois < 0 || jour < 1 || jour > 31 || year < 1971) {
                    return 2;
                }// date invalide
                Calendar beginDate = Calendar.getInstance();
                beginDate.set(year, mois, jour, 0, 1);
                Calendar endDate = Calendar.getInstance();
                endDate.set(year, mois, jour, 23, 59);
                supprimerRDV(titre, beginDate, endDate);
                return 1;// la requete a bien été traitée
            } else if (requete.contains("prochain")) {
                return 3;// cas de la demande du prochain rendez vous , traitée dans le toolManager directement

            }
            return 42;
        }
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
            }
            cursor.moveToNext();
        }
        cursor.close();
    }

    /**
     * Méthode qui permet d'ajouter un rendez vous dans le calendrier .
     * @return Le nom du rendez Vous
     * */
    public String prochainRDV(){
        String rendezVous;
        Cursor cursor =getEvents();
        Calendar date=Calendar.getInstance();
        long dateMillis=date.getTimeInMillis();
        cursor.moveToFirst();
        int nbEvents=cursor.getCount();
        if (nbEvents==0){
            rendezVous= "Vous n'avez pas d'événements prévus par le Compagnon Virtuel";
        }else {
            if (nbEvents>1) {
                TreeMap<Long, Integer> dateMap = new TreeMap<>();
                dateMap.put(dateMillis, -1);
                dateMap.put(cursor.getLong(3), cursor.getInt(6));
                while (cursor.moveToNext()) {
                    dateMap.put(cursor.getLong(3), cursor.getInt(6));
                    }
                List<Long> keys = new ArrayList<Long>(dateMap.keySet());
                int i = 0;
                while (keys.get(i) <= dateMillis) {
                    i = i + 1;
                }
                Long nextEventKey = keys.get(i);
                int idNextEvent = dateMap.get(nextEventKey);
                cursor.moveToFirst();
                while (cursor.getInt(6) != idNextEvent) {
                    cursor.moveToNext();
                }
            }
            if (cursor.getLong(3)<dateMillis){
                rendezVous= "Vous n'avez pas d'événements prévus par le Compagnon Virtuel";
            }else {
                Calendar calendar = new GregorianCalendar();
                Date dateRDV = new Date(cursor.getLong(3));
                calendar.setTime(dateRDV);
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH) + 1;
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minutes = calendar.get(Calendar.MINUTE);
                String titre = cursor.getString(1);
                rendezVous = "Votre prochain rendez-vous intitulé " + titre + " est le " + day + "/" + month + "/" + year + " à " + hour + " heure " + minutes;
            }
        }
            cursor.close();
            return rendezVous;
    }


}
