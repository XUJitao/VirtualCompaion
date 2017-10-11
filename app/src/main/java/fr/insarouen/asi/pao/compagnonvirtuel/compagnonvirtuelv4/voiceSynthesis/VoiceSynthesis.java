package fr.insarouen.asi.pao.compagnonvirtuel.compagnonvirtuelv4.voiceSynthesis;

        import android.os.Build;
        import android.os.Bundle;
        import android.speech.tts.TextToSpeech;

        import java.util.HashMap;

        import fr.insarouen.asi.pao.compagnonvirtuel.compagnonvirtuelv4.ToolManager;

/**
 *  Classe de synthèse vocale
 *  @author Thibault PICARD, Lise QUESNEL, Alexandre LEVACHER
 *  @version 1.0
 */

public class VoiceSynthesis implements TextToSpeech.OnInitListener {

    /**
     * L'objet TextToSpeech qui nous permet de faire de la synthese vocale
     */
    private TextToSpeech mTTS;

    /**
     * Le listener qui nous permet de lancer des actions durant différentes étapes de la synthese vocale.
     */
    private VoiceSynthesisListener voiceSynthesisListener;

    /**
     * Le Bundle dont a besoin la méthode speak (API > 21)
     */
    Bundle params;

    /**
     * Le Bundle dont a besoin la méthode speak (API < 21)
     */
    HashMap<String,String> parameters;

    /**
     * Le booléen qui nous permet de vérifier que l'appareil est sous une version d'android inférieur à 21.
     */
    boolean lessThan21;


    /**
     * Constructeur de VoiceRecognition
     * @param toolManager le toolManager associé à voiceSynthesis
     * @see ToolManager
     * */
    public VoiceSynthesis(ToolManager toolManager) {
        mTTS = new TextToSpeech(toolManager.getCallingActivity(), this);
        voiceSynthesisListener = new VoiceSynthesisListener(toolManager);
        mTTS.setOnUtteranceProgressListener(voiceSynthesisListener);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            lessThan21 = true;
            //Si l'API du telephone est inférieur à 21 alors la méthode speak nécessite un Hashmap
            parameters = new HashMap<String, String>();
            parameters.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "1234");
        } else {
            lessThan21 = false;
            //Sinon la méthode nécessite un Bundle.
            params = new Bundle();
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "1234");
        }
    }

    /**
     * Lance la synthèse vocale en appelant une méthode différente suivant la version API de l'utilisateur
     * @param text   Texte que l'application doit retranscrire sous forme vocale
     */
    public void toSpeak(String text) {
        if (lessThan21) {
            //Fonction deprecated utilisable pour les API < niveau 21
            mTTS.speak(text, TextToSpeech.QUEUE_ADD, parameters);
        } else {
            //Fonction utilisable pour les API > niveau 21
            mTTS.speak(text, TextToSpeech.QUEUE_ADD, params, "1234567");
        }
    }

    /**
     * Implémentation de la méthode abstraite onInit(int) de OnIinitListener pour l'initialisation
     * @param status entier
     */
    public void onInit(int status) {
    }

}
