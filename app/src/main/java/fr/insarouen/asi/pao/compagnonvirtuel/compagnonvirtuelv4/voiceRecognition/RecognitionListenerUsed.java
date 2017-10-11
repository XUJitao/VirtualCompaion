package fr.insarouen.asi.pao.compagnonvirtuel.compagnonvirtuelv4.voiceRecognition;

/**
 *  La classe qui gère les différentes étapes de la reconnaissance et récupère véritablement les résultats.
 *
 *  @author Thibault PICARD, Lise QUESNEL, Alexandre LEVACHER
 *  @version 1.0
 */

        import android.os.Bundle;
        import android.speech.SpeechRecognizer;
        import android.util.Log;
        import java.util.ArrayList;
        import fr.insarouen.asi.pao.compagnonvirtuel.compagnonvirtuelv4.ToolManager;


public class RecognitionListenerUsed implements android.speech.RecognitionListener {


    private static final String TAG = "DEBUG"; // à supprimer à la fin du debug;

    /**
     * Le ToolManager dans lequel la reconnaissance est lancé
     * @see ToolManager
     * */
    private ToolManager toolManager;

    /**
     * Constructeur de la RecognitionListenerUsed
     * @param _toolManager  le ToolManager dans lequel la reconnaissance est lancé
     * @see ToolManager
     * */
    public RecognitionListenerUsed(ToolManager _toolManager){          //ATTENTION : il faut changer MainActivity par l'activité qui lance la reconnaissance vocale
        toolManager = _toolManager;
    }

    /**
     * Méthode appelée lorsque la reconnaissance débute l'écoute
     * */
    @Override
    public void onReadyForSpeech(Bundle params) {
        toolManager.toaster("Je vous écoute.");
        Log.d(TAG, "Début de l'écoute...");
    }

    /**
     * Méthode appelée lorsque la reconnaissance entend une voix
     * */
    @Override
    public void onBeginningOfSpeech() {
        Log.d(TAG, "début de l'enregistrement d'une voix...");
    }

    /**
     * Méthode appelée lorsque le niveau sonore entendu change
     * */
    @Override
    public void onRmsChanged(float rmsdB) {
        // Log.d(TAG, "onRmsChanged");
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        Log.d(TAG, "onBufferReceived");
    }

    /**
     * Méthode appelée lorsque la reconnaissance termine l'écoute
     * */
    @Override
    public void onEndOfSpeech() {
        Log.d(TAG, "fin de l'écoute !");

    }

    /**
     * Méthode appelée lorsque la reconnaissance a rencontré une erreur (l'écoute est déjà terminée). Voir la doc de SpeechRecognizer pour plus de détails sur les codes d'erreur.
     * */
    @Override
    public void onError(int error) {
        Log.d(TAG, "Une erreur a été rencontrée durant la reconnaissance vocale ! Code de l'erreur : " + error);
        String errorToShow;
        switch (error){
            case 1 :
            case 2 :
                errorToShow = "Il semblerait qu'il y ait un problème de connection avec le serveur de la reconnaissance vocale.";
                break;
            case 3 :
                errorToShow = "Il semblerait qu'il y ait  une erreur avec le micro.";
                break;
            case 5 :
                errorToShow = "Il semblerait qu'il y ait une autre erreur venant de votre téléphone.";
                break;
            case 6 :
                errorToShow = "Erreur : je n'ai rien entendu.";
                break;
            case 7 :
                errorToShow = "Désolé, je n'ai pas compris.";
                break;
            case 8 :
                errorToShow = "Il semblerait que les serveur de reconnaissance vocale soient occupés pour le moment.";
                break;

            default:
                errorToShow = "La reconnaissance vocale a rencontré une erreur inconnue ! Code de l'erreur : " + error;
                break;
        }

        toolManager.setError(errorToShow);

        //ne pas oublier de dire au voiceRecognition que la reconnaissance vocale n'est plus en cours
        toolManager.setIsListening(false);
        toolManager.toaster("Une erreur est survenue !\n Fin de l'écoute. ");
    }

    /**
     * Méthode appelée lorsque la reconnaissance n'a pas rencontré d'erreur (l'écoute est déjà terminée)
     * */
    @Override
    public void onResults(Bundle results) {
        toolManager.toaster("Fin de l'écoute. ");

        Log.d(TAG, "Les résultats de la reconnaissance vocale sont envoyé au toolManager.");

        // On récupère le résultat de la reconnaissance que l'on stocke dans un ArrayList:
        // Le premier élément est le résultat le plus probable
        // Le deuxième est le deuxième résultat le plus probable
        // ...
        ArrayList<String> recognitionResult = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
       // for (int i=0; i<recognitionResult.size() ; i++) {
         //   recognitionResult.get(i).replace("é", "e");
           // recognitionResult.get(i).replace("è", "e");
      //  }
        toolManager.setRecognitionResult(recognitionResult);

        //ne pas oublier de dire au voiceRecognition que la reconnaissance vocale n'est plus en cours
        toolManager.setIsListening(false);
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        Log.d(TAG, "onPartialResults");
    }

    @Override
    public void onEvent(int eventType, Bundle params) {
        Log.d(TAG, "onEvent " + eventType);
    }
}
