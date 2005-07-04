package sample.mtom.client;

import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

public class MTOMClient extends JFrame{

    public MTOMClient(String title) throws HeadlessException {
        super(title);

        this.getContentPane().add(new UserInterface(this));
        this.show();

    }

    public static void main(String[] args) {
        MTOMClient form = new MTOMClient("MTOM Sample Client");
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        form.setLocation(screenSize.width / 4 - 20,
                screenSize.height / 4);
        form.setSize(screenSize.width / 2 - 80, screenSize.height / 2);
        form.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        form.show();
    }
}
