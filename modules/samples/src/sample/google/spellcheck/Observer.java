package sample.google.spellcheck;


/**
 * interface sample.google.spellcheck.Observer
 *
 * @author Nadana Gunarathna
 */
public interface Observer {
    // updates the message to the main test display area
    public void update(String message);

    //updates the error message to the error message display area
    public void updateError(String message);

    public void clear();
}
