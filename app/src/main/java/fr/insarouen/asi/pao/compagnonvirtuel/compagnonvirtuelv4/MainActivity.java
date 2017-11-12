package fr.insarouen.asi.pao.compagnonvirtuel.compagnonvirtuelv4;

        import android.app.Activity;
        import android.app.AlertDialog;
        import android.app.Dialog;
        import android.content.Intent;
        import android.content.pm.PackageManager;
        import android.speech.RecognizerIntent;
        import android.os.Bundle;
        import android.view.View;
        import android.widget.Button;
        import android.widget.TextView;

        import java.util.List;

/**
 *  L'activité principale
 *  @author Thibault PICARD, Lise QUESNEL, Alexandre LEVACHER
 *  @version 1.0
 */

public class MainActivity extends Activity {

    /**
     * Méthode permettant de créer l'activité
     * */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Button button = (Button) findViewById(R.id.button);

        //button.setOnClickListener(this);


        ToolManager toolManager = new ToolManager(this);
        TextView nextEvent = (TextView) findViewById(R.id.textView_next_event);
        nextEvent.setText(toolManager.getNextEvent());
        Button speakButton = (Button) findViewById(R.id.btn_launcher_recognition);


        speakButton.setOnClickListener(toolManager);

        if (!isSpeechRecognitionActivityPresented(this)) {
            activateGoogle(this);
        }

    }

    /**
     * La méthode qui est appelée lorque l'utilisateur a appuyé sur le bouton démarrer.
     * Dans un premier temps, on vérifie si il y a bien une application qui puisse gérer la reconnaissance vocale d'installé sur l'appareil de l'utilisateur.
     * Puis, on vérifie si il y a bien une application qui puisse gérer la synthèse vocale d'installée sur l'appareil de l'utilisateur. Même démarche pour la synthèse vocale.
     * */

   /* @Override
    public void onClick(View v) {
        if (isSpeechRecognitionActivityPresented(this)) {
            Intent intent = new Intent(MainActivity.this, ActionActivity.class);
            MainActivity.this.startActivity(intent);
        } else {
            activateGoogle(this);
        }

    }*/

    /**
     * La méthode qui vérifie si il y a bien une application qui puisse gérer la reconnaissance vocale d'installé sur l'appareil de l'utilisateur.
     * @param callerActivity l'activité sur laquelle on se trouve.
     * @return le booléen qui détermine si oui ou non une application qui puisse gérer la reconnaissance vocale est installé sur l'appareil de l'utilisateur.
     * */

    private boolean isSpeechRecognitionActivityPresented(Activity callerActivity) {
        try {
            PackageManager pm = callerActivity.getPackageManager();
            List activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);

            if (activities.size() != 0) {
                return true;
            }
        } catch (Exception e) {

        }

        return false;
    }

    /**
     * La méthode demandant à l'utilisateur d'activer la recherche Google si besoin pour la reconnaissance vocale.
     * @param ownerActivity l'activité sur laquelle on se trouve.
     * */

    private void activateGoogle(final Activity ownerActivity) {

        Dialog dialog = new AlertDialog.Builder(ownerActivity)
                .setMessage("Pour la reconnaissance vocale, il est nécessaire d'activer la recherche Google.")
                .setTitle("Reconnaissance vocale de Google")
                .setPositiveButton("OK",null)
                .create();

        dialog.show();
    }

}
