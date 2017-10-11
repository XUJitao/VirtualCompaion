package fr.insarouen.asi.pao.compagnonvirtuel.compagnonvirtuelv4.voiceSynthesis;

        import android.os.Handler;
        import android.speech.tts.UtteranceProgressListener;
        import android.util.Log;

        import fr.insarouen.asi.pao.compagnonvirtuel.compagnonvirtuelv4.ToolManager;

/**
 * Le listener qui nous permet de maitriser les différentes étapes de la synthèse vocale.
 */
public class VoiceSynthesisListener extends UtteranceProgressListener {


    private static final String TAG = "DEBUG"; // à supprimer à la fin du debug

    /**
     * Le toolManager associé au voiceSynthesisListener
     * @see ToolManager
     * */
    ToolManager toolManager;

    /**
     * Constructeur de VoiceSynthesisListener
     * */
    public VoiceSynthesisListener(ToolManager _toolManager){
        toolManager = _toolManager;
    }

    /**
     * La méthode qui est appelée lorsque la synthèse vocale est terminée.
     * @param utteranceld l'ID donné lors de l'appel de la methode speak
     * */
    @Override
    public void onDone(String utteranceld){
        Handler handler = toolManager.getHandler();
        Log.d(TAG, "Fin de la synthèse vocale !");
        Runnable run = new Runnable() {
            public void run() {
                toolManager.additionalAnimation();
            }
        };
        handler.post(run);

    }

    /**
     * La méthode qui est appelée lorsque la synthèse vocale à rencontrer une erreur.
     * @param utteranceId l'ID donné lors de l'appel de la methode speak
     * @param  errorCode le numéro de l'erreur rencontré lors de la synthèse vocale.
     * */
    @Override
    public void onError(String utteranceId, int errorCode){
        Log.d(TAG, "Une erreur s'est produite durant la synthèse vocale ! Code de l'erreur : " + errorCode);
        String errorToShow = "Une erreur s'est produite durant la synthèse vocale : erreur " + errorCode;
        toolManager.setError(errorToShow);
    }

    /**
     * La méthode qui est appelée lorsque la synthèse vocale à rencontrer une erreur. DEPRECATED API >= 21
     * @param utteranceId l'ID donné lors de l'appel de la methode speak
     * */
    @Override
    public void onError(String utteranceId){Log.d(TAG, "Une erreur s'est produite durant la synthèse vocale !");}

    /**
     * La méthode qui est appelée lorsque la synthèse vocale débute.
     * @param utteranceld l'ID donné lors de l'appel de la methode speak
     * */
    @Override
    public void onStart(String utteranceld){
        Handler handler = toolManager.getHandler();

        Log.d(TAG, "Début de la synthèse vocale...");

        Runnable run = new Runnable() {
            public void run() {
                toolManager.animerLePersonnage("parler");
            }
        };
        handler.post(run);
    }

    @Override
    public void onStop(String utteranceld, boolean interrupted){Log.d(TAG, "STOP : "+interrupted);}

}