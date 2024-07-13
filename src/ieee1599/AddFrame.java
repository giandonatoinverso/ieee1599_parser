package ieee1599;

import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/*
@author Giandonato Inverso <giandonato.inverso@studenti.unimi.it>

This class creates a JFrame that allows a measure insertion. Based on the number of values inserted by the user, this class automatically
detects the type of measure (chord or rest).

This is a singleton class, so in the program execution is allowed only one instance of this class. In this way the user can press multiple time
the button that invoke this class but it gets the same instance.
 */
public class AddFrame extends JFrame implements ActionListener {

    Container container = getContentPane();
    JLabel numLabel = new JLabel("Num");
    JLabel denLabel = new JLabel("Den");
    JLabel octaveLabel = new JLabel("Octave");
    JLabel stepLabel = new JLabel("Step");
    JLabel accidentalLabel = new JLabel("Accidental");

    JTextField numTextField = new JTextField();
    JTextField denTextField = new JTextField();
    JTextField octaveTextField = new JTextField();

    String[] note = {"", "C", "D", "E", "F", "G", "A", "B"};
    JComboBox comboBoxStep = new JComboBox(note);

    String[] accidentalList = {"", "natural", "sharp", "flat"};
    JComboBox comboBoxAccidental = new JComboBox(accidentalList);

    JButton addButton = new JButton("Add");
    JButton resetButton = new JButton("Reset");

    EditXml editXml;
    private static volatile AddFrame instance;

    private AddFrame() throws ParserConfigurationException, IOException, SAXException {
        setLayoutManager();
        setLocationAndSize();
        addComponentsToContainer();
        addActionEvent();

        this.editXml = EditXml.getInstance(null);
    }

    public static AddFrame getInstance() throws ParserConfigurationException, IOException, SAXException {
        if(instance == null) {
            synchronized (EditXml.class) {
                if(instance == null)
                    instance = new AddFrame();
            }
        }

        return instance;
    }

    private void setLayoutManager() {
        container.setLayout(null);
    }

    private void setLocationAndSize() {
        numLabel.setBounds(50, 30, 100, 30);
        denLabel.setBounds(50, 80, 100, 30);
        octaveLabel.setBounds(50, 130, 100, 30);
        stepLabel.setBounds(50, 180, 100, 30);
        accidentalLabel.setBounds(50, 230, 100, 30);

        numTextField.setBounds(150, 30, 150, 30);
        denTextField.setBounds(150, 80, 150, 30);
        octaveTextField.setBounds(150, 130, 150, 30);
        comboBoxStep.setBounds(150, 180, 150, 30);
        comboBoxAccidental.setBounds(150, 230, 150, 30);

        addButton.setBounds(50, 300, 100, 30);
        resetButton.setBounds(200, 300, 100, 30);
    }

    private void addComponentsToContainer() {
        container.add(numLabel);
        container.add(denLabel);
        container.add(octaveLabel);
        container.add(stepLabel);
        container.add(accidentalLabel);

        container.add(numTextField);
        container.add(denTextField);
        container.add(octaveTextField);
        container.add(comboBoxStep);
        container.add(comboBoxAccidental);

        container.add(addButton);
        container.add(resetButton);
    }

    private void addActionEvent() {
        addButton.addActionListener(this);
        resetButton.addActionListener(this);
    }

    /*
    num and den parameters are mandatory
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == addButton) {
            Integer num;
            Integer den;
            String octave;
            String step;
            String accidental;

            if(numTextField.getText() == null || numTextField.getText() == "")
                throw new IllegalStateException("numTextField.getText() == null || numTextField.getText() == ");
            else
                num = Integer.valueOf(numTextField.getText());

            if(denTextField.getText() == null || denTextField.getText() == "")
                throw new IllegalStateException("denTextField.getText() == null || denTextField.getText() == ");
            else
                den = Integer.valueOf(denTextField.getText());

            octave = octaveTextField.getText();
            step = comboBoxStep.getSelectedItem().toString();
            accidental = comboBoxAccidental.getSelectedItem().toString();

            Object[] response = this.editXml.addMeasure(num, den, octave, step, accidental);

            if((Boolean) response[0]) {
                JOptionPane.showMessageDialog(this, "New measure inserted");
                this.resetFields();
            } else {
                JOptionPane.showMessageDialog(this, "Errore durante l'inserimento. La durata deve essere inferiore o uguale alla time signature della part di riferimento, cioè: " + response[1]);
                this.resetFields();
            }
        }

        if (e.getSource() == resetButton) {
            this.resetFields();
        }
    }

    private void resetFields() {
        numTextField.setText("");
        denTextField.setText("");
        octaveTextField.setText("");
        comboBoxStep.setSelectedIndex(0);
        comboBoxAccidental.setSelectedIndex(0);
    }
}