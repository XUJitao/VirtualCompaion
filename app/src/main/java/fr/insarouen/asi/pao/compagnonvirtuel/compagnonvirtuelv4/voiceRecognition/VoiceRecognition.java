package fr.insarouen.asi.pao.compagnonvirtuel.compagnonvirtuelv4.voiceRecognition;
/**
 *  La classe qui lance la reconnaissance vocale.
 *
 *  @author Thibault PICARD, Lise QUESNEL, Alexandre LEVACHER
 *  @version 1.0
 */


        import android.content.Intent;
        import android.speech.RecognizerIntent;
        import android.speech.SpeechRecognizer;
        import fr.insarouen.asi.pao.compagnonvirtuel.compagnonvirtuelv4.ToolManager;

public class VoiceRecognition{

    /**
     * La classe de l'API pour la reconnaissance vocale
     * */
    private SpeechRecognizer sr;

    /**
     * Le ToolManager dans lequel la reconnaissance est lancé
     * @see ToolManager
     * */
    private ToolManager toolManager;

    /**
     * Constructeur de VoiceRecognition
     * @param _toolManager  le ToolManager dans lequel la reconnaissance est lancé
     * @see ToolManager
     * */
    public VoiceRecognition(ToolManager _toolManager){
        toolManager = _toolManager;
        sr = SpeechRecognizer.createSpeechRecognizer(toolManager.getCallingActivity());
        sr.setRecognitionListener(new RecognitionListenerUsed(toolManager));
    }

    /**
     * méthode qui lance la reconnaissance vocale
     *
     * */
    public void run() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getClass().getPackage().getName());
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
        sr.startListening(intent);
        toolManager.setIsListening(true);
    }

    /**
     * méthode qui arrète la reconnaissance vocale
     *
     * */
    public void stop(){
        sr.stopListening();
        toolManager.setIsListening(false);
    }

}
