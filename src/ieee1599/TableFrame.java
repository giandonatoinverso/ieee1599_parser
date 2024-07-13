package ieee1599;

import javax.swing.*;

/*
@author Giandonato Inverso <giandonato.inverso@studenti.unimi.it>

Singleton of a generic JFrame
 */
public class TableFrame extends JFrame {
    private static volatile TableFrame instance;

    public static TableFrame getInstance() {
        if(instance == null) {
            synchronized (TableFrame.class) {
                if(instance == null)
                    instance = new TableFrame();
            }
        }

        return instance;
    }

    private TableFrame() {}
}
