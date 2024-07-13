package ieee1599;

import org.xml.sax.SAXException;

import javax.swing.*;
import javax.swing.table.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.List;

/*
@author Giandonato Inverso <giandonato.inverso@studenti.unimi.it>
@source https://github.com/aterai/java-swing-tips/blob/master/MultipleButtonsInTableCell/src/java/example/MainPanel.java

Create a JFrame which contains a JPanel which in turn contains a JTable composed starting from the data model previously
created with the measures.
 */
public class TablePanel extends JPanel {
    public TablePanel() {}

    /*
    Create JTable object with data model and modify index 4,5,6 columns with custom objects (step, accidental and operation buttons)
     */
    private TablePanel(TableModel model) throws ParserConfigurationException, IOException, SAXException {
        super(new BorderLayout());

        JTable table = new JTable(model) {
            @Override public void updateUI() {
                super.updateUI();
                setRowHeight(45);
                setAutoCreateRowSorter(true);

                /* Step */
                String[] note = {"C", "D", "E", "F", "G", "A", "B"};
                JComboBox comboBoxStep = new JComboBox(note);

                TableColumn step = getColumnModel().getColumn(4);
                DefaultTableCellRenderer rendererStep = new DefaultTableCellRenderer();
                step.setCellRenderer(rendererStep);
                step.setCellEditor(new DefaultCellEditor(comboBoxStep));

                /* Accidental */
                String[] accidental = {"natural", "sharp", "flat", ""};
                JComboBox comboBoxAccidental = new JComboBox(accidental);

                TableColumn actualAccidental = getColumnModel().getColumn(5);
                DefaultTableCellRenderer rendererActualAccidental = new DefaultTableCellRenderer();
                actualAccidental.setCellRenderer(rendererActualAccidental);
                actualAccidental.setCellEditor(new DefaultCellEditor(comboBoxAccidental));

                /* Operations buttons */
                TableColumn column = getColumnModel().getColumn(6);
                column.setCellRenderer(new ButtonsRenderer());
                try {
                    column.setCellEditor(new OperationsButtons(this));
                } catch (ParserConfigurationException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (SAXException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        EditXml editXmlInstance = EditXml.getInstance(null);
        editXmlInstance.setTable(table);

        this.setLayout(new BorderLayout());

        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton addMeasure = new JButton("Add measure");
        addMeasure.setAction(new AddAction(table));

        JToolBar toolbar = new JToolBar();
        toolbar.add(addMeasure);
        add(new JScrollPane(toolbar), BorderLayout.NORTH);

        setPreferredSize(new Dimension(1600, 720));
    }

    /*
    Adds the JPanel in a singleton JFrame to prevent multiple creation of this JPanel
     */
    public void init(TableModel model) throws ParserConfigurationException, IOException, SAXException {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (UnsupportedLookAndFeelException ignored) {
            Toolkit.getDefaultToolkit().beep();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            ex.printStackTrace();
            return;
        }

        TableFrame MyFrame = TableFrame.getInstance();
        MyFrame.setTitle("Modifica note e pause");
        MyFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        MyFrame.getContentPane().add(new TablePanel(model));
        MyFrame.pack();
        MyFrame.setLocationRelativeTo(null);
        MyFrame.setVisible(true);
    }
}

/*
Class that contains a list of buttons for various operations
 */
class ButtonsList extends JPanel {
    private final List<JButton> buttonsList = List.of(new JButton("delete"));

    protected ButtonsList() {
        super();
        setOpaque(true);
        for (JButton button : buttonsList) {
            button.setFocusable(false);
            button.setRolloverEnabled(false);
            add(button);
        }
    }

    protected List<JButton> getButtons() {
        return buttonsList;
    }
}

class ButtonsRenderer implements TableCellRenderer {
    private final ButtonsList panel = new ButtonsList() {
        @Override public void updateUI() {
            super.updateUI();
            setName("Table.cellRenderer");
        }
    };

    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        panel.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
        return panel;
    }
}

/*
Class that implements the methods for a data deletion action from the table.

All the data of the modified row are collected and, depending on the type, a Chord or Rest object is created to be
deleted using the EditXml class.

They are then physically removed from the visual table
 */
class DeleteAction extends AbstractAction {
    private final JTable table;
    private final EditXml editXml;

    protected DeleteAction(JTable table) throws ParserConfigurationException, IOException, SAXException {
        super("delete");
        this.table = table;
        this.editXml = EditXml.getInstance(null);
    }

    @Override public void actionPerformed(ActionEvent e) {
        int row = table.convertRowIndexToModel(table.getEditingRow());

        String event_ref = (String) table.getModel().getValueAt(row, 0);
        Integer num = (Integer) table.getModel().getValueAt(row, 1);
        Integer den = (Integer) table.getModel().getValueAt(row, 2);
        String octave = (String) table.getModel().getValueAt(row, 3);
        String step = (String) table.getModel().getValueAt(row, 4);
        String actualAccidental = (String) table.getModel().getValueAt(row, 5);

        if(octave == null && step == null && actualAccidental == null) {
            JOptionPane.showMessageDialog(table, "Clicca su ok per cancellare questa pausa");
            try {
                this.editXml.deleteRow(new Rest(event_ref, num, den));
            } catch (TransformerException ex) {
                throw new RuntimeException(ex);
            }
            TableModel model = (TableModel) table.getModel();
            model.removeRow(row);
        }
        else {
            JOptionPane.showMessageDialog(table, "Clicca su ok per cancellare questa nota");
            try {
                this.editXml.deleteRow(new Chord(event_ref, num, den, Integer.parseInt(octave), step, actualAccidental));
            } catch (TransformerException ex) {
                throw new RuntimeException(ex);
            }
            TableModel model = (TableModel) table.getModel();
            model.removeRow(row);
        }
    }
}

/*
Class that implements the methods for adding data to the table. The class just creates a new JFrame for the operation
 */
class AddAction extends AbstractAction {
    private final JTable table;
    private EditXml editXml;

    protected AddAction(JTable table) throws ParserConfigurationException, IOException, SAXException {
        super("Add measure");
        this.table = table;
        this.editXml = EditXml.getInstance(null);
    }

    @Override public void actionPerformed(ActionEvent e) {
        AddFrame frame = null;
        try {
            frame = AddFrame.getInstance();
        } catch (ParserConfigurationException ex) {
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } catch (SAXException ex) {
            throw new RuntimeException(ex);
        }
        frame.setTitle("Add measure Form");
        frame.setVisible(true);
        frame.setBounds(10, 10, 370, 600);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setResizable(false);
    }
}


class OperationsButtons extends AbstractCellEditor implements TableCellEditor {
    protected final ButtonsList buttonsList = new ButtonsList();
    protected final JTable table;


    private class EditingStopHandler extends MouseAdapter implements ActionListener {
        @Override public void mousePressed(MouseEvent e) {
            Object o = e.getSource();
            if (o instanceof TableCellEditor) {
                actionPerformed(new ActionEvent(o, ActionEvent.ACTION_PERFORMED, ""));
            } else if (o instanceof JButton) {
                ButtonModel m = ((JButton) e.getComponent()).getModel();
                if (m.isPressed() && table.isRowSelected(table.getEditingRow()) && e.isControlDown()) {
                    buttonsList.setBackground(table.getBackground());
                }
            }
        }

        @Override public void actionPerformed(ActionEvent e) {
            EventQueue.invokeLater(OperationsButtons.this::fireEditingStopped);
        }
    }

    protected OperationsButtons(JTable table) throws ParserConfigurationException, IOException, SAXException {
        super();
        this.table = table;
        List<JButton> Buttonlist = buttonsList.getButtons();
        Buttonlist.get(0).setAction(new DeleteAction(table));

        EditingStopHandler handler = new EditingStopHandler();
        for (JButton button : Buttonlist) {
            button.addMouseListener(handler);
            button.addActionListener(handler);
        }
        buttonsList.addMouseListener(handler);
    }

    @Override public Component getTableCellEditorComponent(JTable tbl, Object value, boolean isSelected, int row, int column) {
        buttonsList.setBackground(tbl.getSelectionBackground());
        return buttonsList;
    }

    @Override public Object getCellEditorValue() {
        return "";
    }
}