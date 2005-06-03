package sample.google.spellcheck;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 *  interface sample.google.spellcheck.Observer
 * @author Nadana Gunarathna
 *
 */
public interface Observer {
    public void update(String suggestion);
    }
