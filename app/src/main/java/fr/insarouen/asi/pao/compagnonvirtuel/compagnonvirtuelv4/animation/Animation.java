package fr.insarouen.asi.pao.compagnonvirtuel.compagnonvirtuelv4.animation;

        import android.app.Activity;
        import android.graphics.drawable.AnimationDrawable;
        import android.util.Log;
        import android.widget.ImageView;

        import fr.insarouen.asi.pao.compagnonvirtuel.compagnonvirtuelv4.R;

/**
 *  Classe d'animation
 *  @author Thibault PICARD, Lise QUESNEL, Alexandre LEVACHER
 *  @version 2.0
 */
public class  Animation {

    /**
     * L'activité dans laquelle Animation est appelée
     * @see Activity
     **/

    private Activity callingActivity;
    private ImageView imageView;
    private AnimationDrawable animationDrawable = null;

    private static final String LOGTAG = "DEBUG";

    /**
     * Constructeur de l'Animation
     * @param callingActivity l'activité appelante
     * @see Activity
     * */

    public Animation (Activity callingActivity) {
        this.callingActivity = callingActivity;
        // Load the ImageView that will host the animation and
        // set its background to our AnimationDrawable XML resource.
        imageView = (ImageView) getCallingActivity().findViewById(R.id.imageView);
    }

    /**
     * Méthode permettant d'obtenir l'activité appelante
     * @return Activity
     * @see Activity
     * */

    public Activity getCallingActivity(){
        return this.callingActivity;
    }

    /**
     * Méthode permettant de lancer l'animation
     *param RessourceAnimation la ressource d'une animation sous forme de string
     * */

    public void lancerAnimation (String RessourceAnimation) {
        int id;
        id = getIDDrawableByName(RessourceAnimation);
        /* On vérifie que la ressource demandé soit disponnible (qu'elle existe). Si elle ne l'ai pas, on lance par défaut la ressource "inactif"*/
        if (id == 0){
            id = getIDDrawableByName("inactif");
            Log.d(LOGTAG, "ERREUR : l'animation demandé n'existe pas ! (" + RessourceAnimation +")");
        }
        if (animationDrawable!=null) {
            animationDrawable.stop();
        }
        imageView.setBackgroundResource(id);
        animationDrawable = (AnimationDrawable) imageView.getBackground();
        animationDrawable.start();
    }

    /**
     * Méthode privée permettant d'obtenir l'ID d'un Drawable à partir de son nom
     * @param name le nom du Drawable
     * @return Integer
     */

    private Integer getIDDrawableByName(String name) {
        String packageName = this.callingActivity.getPackageName();
        int ID = getCallingActivity().getResources().getIdentifier(name, "drawable", packageName);
        return ID;
    }
}
