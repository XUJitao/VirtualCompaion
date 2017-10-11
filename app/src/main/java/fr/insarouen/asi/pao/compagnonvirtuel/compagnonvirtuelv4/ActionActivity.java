package fr.insarouen.asi.pao.compagnonvirtuel.compagnonvirtuelv4;

        import android.app.Activity;
        import android.os.Bundle;
        import android.widget.Button;

/**
 *  Classe Activité gérant les actions
 *  @author Thibault PICARD, Lise QUESNEL, Alexandre LEVACHER
 *  @version 1.0
 */

public class ActionActivity extends Activity {

    /**
     * Méthode permettant de créer l'activité
     * @param savedInstanceState needed
     * */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_action);

        ToolManager toolManager = new ToolManager(this);

        Button speakButton = (Button) findViewById(R.id.btn_launcher_recognition);


        speakButton.setOnClickListener(toolManager);

    }

}

