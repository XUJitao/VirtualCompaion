package fr.insarouen.asi.pao.compagnonvirtuel.compagnonvirtuelv4;
/**
 *  La classe qui gère les différents modules du projet.
 * 	C'est elle le point central de l'IHM.
 * 	Nous avons créer cette classe pour séparer la partie concernant l'activité en elle-même des différents modules composant le projet. Pour cela, nous avons rassembler tous les modules dans cette classe.
 *
 *  @author Thibault PICARD, Lise QUESNEL, Alexandre LEVACHER, Guillaume Boutet
 *  @version 2.0
 */

        import android.app.Activity;
        import android.os.Handler;
        import android.os.Looper;
        import android.view.View;
        import android.widget.TextView;
        import android.widget.Toast;
        import java.util.ArrayList;
        import java.util.Observable;
        import java.util.Observer;

        import fr.insarouen.asi.pao.compagnonvirtuel.compagnonvirtuelv4.animation.Animation;
        import fr.insarouen.asi.pao.compagnonvirtuel.compagnonvirtuelv4.chatBot.ChatBot;
        import fr.insarouen.asi.pao.compagnonvirtuel.compagnonvirtuelv4.voiceSynthesis.VoiceSynthesis;
        import fr.insarouen.asi.pao.compagnonvirtuel.compagnonvirtuelv4.voiceRecognition.VoiceRecognition;


public class ToolManager extends Observable implements View.OnClickListener, Observer {

    /**
     * L'activité qui instancie le toolManager, sur laquelle se trouve le compagnon virtuel.
     * */
    private Activity callingActivity;

    /**
     * La classe qui gère le module de reconnaissance vocale
     * @see VoiceRecognition
     * */
    private VoiceRecognition voiceRecognition;

    /**
     * La classe qui gère le module de synthèse vocale
     * @see VoiceSynthesis
     * */
    private VoiceSynthesis voiceSynthesis;

    /**
     * Un booléen qui détermine si la reconnaissance vocale est en cours ou non. Permet de savoir si le bouton doit arrêter ou lancer la reconnaissance vocale.
     * */
    private boolean isListening;

    /**
     * Le tableau dans lequel se trouve les différents résultats de la reconnaissance vocale.
     * */
    private ArrayList<String> recognitionResult;

    /**
     * Un booléen qui détermine si la méthode onError a déjà été appelé ou pas. En effet, onError est souvent appelé plusieurs fois.
     * */
    private boolean hasBeenCalled;

    /**
     * L'erreur de la reconnaissance vocale ou de la synthèse vocale.
     * */
    private String error;

    /**
     * Le booléen qui détermine si la reconnaissance a rencontré une erreur ou pas.
     * */
    private boolean foundError;

    /**
     * La classe qui gère le module correpondant à l'affichage du compagnon virtuel.
     * @see Animation
     * */
    private Animation animation;


    /**
     * La réponse du compagnon virtuel.
     * */
    private String answer;

    /**
     * La classe qui gère le module correpondant à la determination d'une réponse intelligente.
     * @see ChatBot
     * */
    private ChatBot bot;

    /**
     * Le handler qui nous permet de changer l'animation lors de la synthèse vocale (pour faire en sorte que le compagnon ne soit dans l'état "parler" que quand la synthèse vocale est en fonctionnement.
     * */
    private Handler handler;

    /**
     * Le nom de l'animation que le chatBot nous a demandé de lancer.
     * */
    private String additionalAnimation;

    /**
     * Le booléen qui permet de savoir si le chatBot nous a demandé de lancer une animation.
     * */
    private boolean additionalAnimationNeeded;

    /**
     * Le constructeur de ToolManager
     * @param _callingActivity L'activité qui instancie le toolManager, sur laquelle se trouve le compagnon virtuel.
     * */
    public ToolManager(Activity _callingActivity){
        callingActivity = _callingActivity;
        voiceRecognition = new VoiceRecognition(this);
        voiceSynthesis = new VoiceSynthesis(this);
        animation = new Animation(_callingActivity);
        isListening = false;
        this.addObserver(this);
        animerLePersonnage("bonjour");
        answer = "BUG !";
        bot = new ChatBot(this);
        handler = new Handler(Looper.getMainLooper());
        additionalAnimationNeeded = false;
    }

     /** La méthode qui permet d'initialiser le module qui gère l'affichage du compagnon virtuel.
     * @param RessourceAnimation la ressource
     * */
    public void animerLePersonnage(String RessourceAnimation) {
        animation.lancerAnimation(RessourceAnimation);
    }

    /**
     * La méthode qui est appelée lorsque la reconnaissance vocale est terminée. L'utilisation de la classe et de l'interface Observable Observer s'explique par le fait que l'application n'attendait pas d'avoir finit la reconnaissance vocale pour exécuter la suite du code lorsque celui-ci s'effectuait linéairement.
     * */
    @Override
    public void update(Observable observable, Object data){
        if (foundError){
            if(!hasBeenCalled){
                hasBeenCalled = true;
                onError();
            }
        } else {
            onResult();
        }
    }

    /**
     * La méthode qui permet de gérer le cas où la reconnaissance s'est bien passée et que des résultats sont affichés
     *  */
    public void onResult(){
        StringBuilder recognitionToShow = new StringBuilder();
        StringBuilder answerToShow = new StringBuilder();
        for(String result:recognitionResult){
            recognitionToShow.append(result);
            recognitionToShow.append("\n");
        }
        answerToShow.append(answer);
        setRecognition(recognitionToShow.toString());
        setAnswerInTextView(answerToShow.toString());
        voiceSynthesis.toSpeak(answer);
    }

    /**
     * Méthode qui traite les différentes erreurs pouvant être rencontrées au cours de la reconnaissance vocale ou de la synthèse vocale.
     */
    public void onError() {
        animerLePersonnage("erreur");
        voiceSynthesis.toSpeak(error);
        setAnswerInTextView(error);
    }

    /**
     * La méthode qui est lancé lorsqu'on appuie sur le bouton pour lancer ou arrêter la reconnaissance vocale.
     * */
    @Override
    public void onClick(View view) {
        //A CHANGER
        if(!isListening) {
            hasBeenCalled = false;
            isListening = true;
            animerLePersonnage("ecoute");
            foundError = false;
            voiceRecognition.run();
        }
        else{
            voiceRecognition.stop();
            isListening = false;
        }
    }

    /**
     * Méthode appelée lorsque la synthèse vocale est terminée. Elle permet de lancer une animation dans le cas où il y a eu une erreur ou dans la cas où une animation supplémentaire a été demandé par le chatBot.
     * Si aucun des deux cas n'est rencontré, l'animation inactif est lancée.
     * */
    public void additionalAnimation() {
        if (foundError)
            animerLePersonnage("erreur");
        else {
            if (additionalAnimationNeeded) {
                animerLePersonnage(additionalAnimation);
                additionalAnimationNeeded = false;
            } else
                animerLePersonnage("inactif");
        }
    }

    /**
     * La méthode qui permet d'afficher simplement du texte dans une boîte de dialogue.
     * @param textToShow le texte à afficher.
     * */
    public void toaster(String textToShow){
        Toast.makeText(callingActivity, textToShow, Toast.LENGTH_SHORT).show();
    }

    /**
     * La méthode qui permet de modifier l'attribut error
     * @param _error la nouvelle valeur du code de l'erreur rencontrée lors de la reconnaissance vocale. Pour plus de détails sur les codes d'erreur, voir la documentation de la classe SpeechRecognizer.
     * */
    public void setError(String _error){
        error = _error;
        foundError = true;
        setChanged();
        notifyObservers();
    }

    /**
     * La méthode qui permet de modifier le tableau de résultat.
     * @param _recognitionResult la nouvelle valeur du tableau de résultat de la reconnaissance vocale.
     * */
    public void setRecognitionResult(ArrayList<String> _recognitionResult){
        recognitionResult = _recognitionResult;
        makeAnswer(recognitionResult.get(0));
    }

    /**
     * La méthode qui récupère une réponse à partir du meilleur résultat de la reconnaissance vocale.
     * */
    public void makeAnswer(String bestRecognitionResult){

        if (findMotCleCalendar(bestRecognitionResult)){
            // cas d'une requete portant sur le calendrier , on court circuite le chatBot
            GestionCalendar Gcal=new GestionCalendar(callingActivity.getApplicationContext());
            int errorRequeteCalendar =Gcal.traiterRequete(bestRecognitionResult);
            switch (errorRequeteCalendar){
                case 0 : setAnswer("La requète est incorrecte");// : elle doit etre de la forme Ajoute/supprime un rendez vous le JJ/mois/AAAA (à HHhMM si ajout), intituleDuRendezVous
                    break;
                case 1 : setAnswer("Action effectuée");
                    break;
                case 2 : setAnswer("La date est incorrecte");
                    break;
                case 3 : setAnswer(Gcal.prochainRDV());
                    break;
                default : setAnswer("j'ai rencontré une erreur inconnue , veuillez recommencer");

            }
            //penser a mettre des toaster
        }else {

            bot.initiateQuery(bestRecognitionResult);
        }
    }
    /**
     * Méthode qui permet de definir si une requete concerne le calendrier ou pas (afin de court circuiter le chatbot pour la traiter).
     * @param requete la requete recue par le compagnon
     * @return true si la requete concerne la gestion de calendrier, false sinon
     * */
    private boolean findMotCleCalendar(String requete){
        if ( (requete.contains("ajoute")&&requete.contains("rendez-vous"))
                ||(requete.contains("supprime")&&requete.contains("rendez-vous"))
                ||(requete.contains("prochain")&&requete.contains("rendez-vous")) ) {
            return true;
        }else return false;
    }

    /**
     * La méthode qui permet de modifier le booléen isListening
     * @param _isListening la nouvelle valeur du booléen.
     * */
    public void setIsListening(boolean _isListening) {
        isListening = _isListening;
    }

    /**
     * Permet d'afficher du texte dans le TextView présent sur l'activité où se trouve le compagnon virtuel. Cette méthode ainsi que le TextView ne devraient pas se trouver dans le projet final.
     * @param text le texte à afficher dans le TextView.
     * */
    public void setRecognition(String text) {
        TextView mText = (TextView) callingActivity.findViewById(R.id.textView_recognition);
        mText.setText(text);
    }

    /**
     * Permet d'afficher du texte dans le TextView présent sur l'activité où se trouve le compagnon virtuel. Cette méthode ainsi que le TextView ne devraient pas se trouver dans le projet final.
     * @param text le texte à afficher dans le TextView.
     * */
    public void setAnswerInTextView(String text) {
        TextView mText = (TextView) callingActivity.findViewById(R.id.textView_answer);
        mText.setText(text);
    }

    /**
     * Méthode qui permet de définir la réponse du chatBot. Elle notifie aussi qu'une réponse à été trouvée, la méthode update est donc appelée lors de l'appel de cette méthode.
     * @param _answer la réponse à l'utilisateur déterminer par le chatBot
     * */
    public void setAnswer(String _answer){
        answer = _answer;
        setChanged();
        notifyObservers();
    }

    /**
     * La méthode qui permet de récuperer l'activité dans laquelle se trouve le compagnon virtuel.
     * @return L'activité dans laquelle se trouve le compagnon virtuel.
     * */
    public Activity getCallingActivity(){
        return callingActivity;
    }

    /**
     * La méthode qui permet de récupérer le handler sur le thread de toolManager. Ce hander permet la gestion des animations.
     * */
    public Handler getHandler(){
        return handler;
    }

    /**
     * La méthode qui d'ajouter une animation additionnelle (lancée après que le compagnon ait finis de parler)
     * @param _additionalAnimation l'animation à lancer
     */
    public void setAdditionalAnimation(String _additionalAnimation){
        additionalAnimation = _additionalAnimation;
        additionalAnimationNeeded = true;
    }

}

