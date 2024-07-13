package ieee1599;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/*
@author Giandonato Inverso <giandonato.inverso@studenti.unimi.it>

Singleton class which allows the modification of the "measures" of an IEEE1599 file as well as the addition of new
"measures" at the end of the last "part".
*/
public class EditXml {
    private static volatile EditXml instance;
    private final Document xml;
    private final String pathFile;
    private final XmlParser xmlParser;
    private final Map<String, Float> timeSignature;
    private JTable table;

    public static EditXml getInstance(File inputFile) throws ParserConfigurationException, IOException, SAXException {
        if(instance == null) {
            synchronized (EditXml.class) {
                if(instance == null)
                    instance = new EditXml(inputFile);
            }
        }

        return instance;
    }

    private EditXml(File inputFile) throws ParserConfigurationException, IOException, SAXException {
        if(inputFile == null)
            throw new IllegalArgumentException("xml input is null");

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        this.pathFile = inputFile.getAbsolutePath();
        this.xml = docBuilder.parse(inputFile);
        this.xmlParser = new XmlParser(this.xml);
        this.timeSignature = new HashMap<>();
        this.setTimeSignature();
    }

    /*
    For each "part" the time signature is extracted and stored
     */
    private void setTimeSignature() {
        try {
            NodeList data = this.getTimeSignature();

            for (int i = 0; i < data.getLength(); i++) {
                String current_event_ref = this.xmlParser.getXPathExpression().evaluate(data.item(i));
                NodeList children = data.item(i).getChildNodes();

                for(int a = 0; a < children.getLength(); a++) {
                    Node myNode = (Node) children.item(a);
                    if(myNode.getNodeName().equals("#text"))
                        myNode.getParentNode().removeChild(myNode);
                }

                for(int a = 0; a < children.getLength(); a++) {
                    Node myNode = (Node) children.item(a);
                    Element myElement = (Element) myNode;
                    if ("time_indication".equals(myNode.getNodeName())) {
                        Integer num = Integer.valueOf(((Element) myNode).getAttribute("num"));
                        Integer den = Integer.valueOf(((Element) myNode).getAttribute("den"));
                        this.timeSignature.put(current_event_ref, (float) (num/den));
                    }
                }
            }

        } catch (XPathExpressionException | NumberFormatException ex) {
            throw new IllegalStateException(ex.getMessage());
        }
    }

    /*** EDIT ***/

    /*
    Find the node relating to the measure to be modified through the unique identifier event_ref.

    Depending on the type of measure, invoke the modification methods for a Chord or Rest
     */
    public void editMeasure(Measure measure) throws TransformerException {
        Node node = this.findMeasure(measure);
        if(node == null)
            throw new IllegalStateException("node == null");

        if(measure.getType() == 0) {
            Object[] response = this.editChordXml(node, measure.getNum(), measure.getDen(), Integer.parseInt(measure.getOctave()), measure.getStep(), measure.getActualAccidental());
            if((Boolean) response[0])
                this.saveFile();
            else
                this.editError(response[1]);
        }
        else {
            Object[] response = this.editRestXml(node, measure.getNum(), measure.getDen());
            if((Boolean) response[0])
                this.saveFile();
            else
                this.editError(response[1]);
        }
    }

    /*
    Return xml file node of measure object in input
     */
    private Node findMeasure(Measure measure) {
        try {
            NodeList data = this.getData(measure);

            for (int i = 0; i < data.getLength(); i++) {
                String current_event_ref = this.xmlParser.getXPathExpression().evaluate(data.item(i));
                if(current_event_ref.equals(measure.getEvent_ref())) {
                    return data.item(i);
                }
            }
        } catch (XPathExpressionException | NumberFormatException ex) {
            throw new IllegalStateException(ex.getMessage());
        }

        return null;
    }

    private NodeList getData(Measure measure) {
        if(measure.getType() == 0)
            return this.getChords();
        else
            return this.getRests();
    }

    /*
    Edit the node of type Chord and check if the changes respects the time signature of the current part
     */
    private Object[] editChordXml(Node chord, Integer num, Integer den, Integer octave, String step, String actualAccidental) {
        NodeList myList = chord.getChildNodes();

        for(int a = 0; a < myList.getLength(); a++) {
            Node myNode = (Node) myList.item(a);
            if(myNode.getNodeName().equals("#text"))
                myNode.getParentNode().removeChild(myNode);
        }

        for(int a = 0; a < myList.getLength(); a++) {
            Node myNode = (Node) myList.item(a);
            Element myElement = (Element) myNode;
            switch (myNode.getNodeName()) {
                case "duration" -> {
                    ((Element) myNode).setAttribute("num", String.valueOf(num));
                    ((Element) myNode).setAttribute("den", String.valueOf(den));
                }
                case "notehead" -> {
                    NodeList myPitchList = myNode.getChildNodes();
                    for(int j = 0; j < myPitchList.getLength(); j++) {
                        Node myNodePitch = (Node) myPitchList.item(j);
                        if(myNodePitch.getNodeName().equals("pitch")) {
                            NodeList myPitchNodeList = myNodePitch.getChildNodes();
                            ((Element) myPitchNodeList).setAttribute("octave", String.valueOf(octave));
                            ((Element) myPitchNodeList).setAttribute("step", String.valueOf(step));
                            ((Element) myPitchNodeList).setAttribute("actual_accidental", actualAccidental);
                        }
                    }
                }
            }
        }

        return this.checkEdit(chord);
    }

    /*
    Edit the node of type Rest and check if the changes respects the time signature of the current part
     */
    private Object[] editRestXml(Node rest, Integer num, Integer den) {
        NodeList myList = rest.getChildNodes();

        for(int a = 0; a < myList.getLength(); a++) {
            Node myNode = (Node) myList.item(a);
            if(myNode.getNodeName().equals("#text"))
                myNode.getParentNode().removeChild(myNode);
        }

        for(int a = 0; a < myList.getLength(); a++) {
            Node myNode = (Node) myList.item(a);
            Element myElement = (Element) myNode;
            if ("duration".equals(myNode.getNodeName())) {
                ((Element) myNode).setAttribute("num", String.valueOf(num));
                ((Element) myNode).setAttribute("den", String.valueOf(den));
            }
        }

        return this.checkEdit(rest);
    }

    /*
    Given a time duration for a measure, it checks whether it is less than or equal to the value relating to
    the current "part". To determine the current part, the longest common string is found by iterating between the
    list of "part" names and the unique identifier (event_ref) of the measure
     */
    private Object[] checkEdit(Node myNode) {
        float sum = this.getDurationSum(myNode);
        float time = 0;

        Element myElement = (Element) myNode;
        String event_ref = myElement.getAttribute("event_ref");

        String longest = "";

        for(String key : this.timeSignature.keySet()) {
            Set<String> longestCommonSubstrings = this.longestCommonSubstrings(key, event_ref);
            String temp = (String) longestCommonSubstrings.toArray()[0];
            if(temp.length() > longest.length())
                longest = temp;
        }

        for (Map.Entry<String, Float> entry : this.timeSignature.entrySet()) {
            String key = entry.getKey();
            if(key.contains(longest)) {
                time = entry.getValue();
            }
        }

        if(time == 0)
            throw new IllegalStateException("time signature not computable for the modified node");

        return new Object[]{sum <= time, time};
    }

    /*
    Calculates the sum of the time duration relative to the whole "part" of the input node
     */
    private float getDurationSum(Node node) {
        float sum = 0;
        NodeList measures = node.getParentNode().getChildNodes();
        for(int a = 0; a < measures.getLength(); a++) {
            Node myNode = (Node) measures.item(a);
            if(myNode.getNodeName().equals("#text"))
                myNode.getParentNode().removeChild(myNode);
        }

        for(int a = 0; a < measures.getLength(); a++) {
            Node measure = (Node) measures.item(a);
            NodeList myList = measure.getChildNodes();
            for(int b = 0; b < myList.getLength(); b++) {
                Node myNode = (Node) myList.item(b);
                if(myNode.getNodeName().equals("#text"))
                    myNode.getParentNode().removeChild(myNode);
            }

            for(int b = 0; b < myList.getLength(); b++) {
                Node myNode = (Node) myList.item(b);
                Element myElement = (Element) myNode;
                if ("duration".equals(myNode.getNodeName())) {
                    int num = Integer.parseInt(((Element) myNode).getAttribute("num"));
                    int den = Integer.parseInt(((Element) myNode).getAttribute("den"));

                    sum += (float) num/den;
                }
            }
        }

        return sum;
    }

    private void editError(Object obj) {
        JOptionPane.showMessageDialog(this.table, "Errore durante la modifica. " +
                "La somma delle durate delle note e delle pause deve essere inferiore o uguale alla time signature della part di riferimento, cioè: " + obj.toString() +
                " Cambiare il valore appena inserito");
    }

    /*
    Creates a new xml file starting from the xml file opened by the program
     */
    private void saveFile() throws TransformerException {
        Transformer xformer = TransformerFactory.newInstance().newTransformer();
        String newFile = this.pathFile.replace(".xml", "");
        newFile += "_new.xml";
        xformer.transform(new DOMSource(this.xml), new StreamResult(new File(newFile)));
    }

    /*** ADD ***/
    /*
    Entering a new measure.

    If the time signature of the input values ​​is less than or equal to the time signature of the last "part" then it is inserted
    a new measure in the XML file and a new row in the visual table. Otherwise, an array containing the time signature is returned
    reference to be respected.
     */
    public Object[] addMeasure(Integer num, Integer den, String octave, String step, String accidental) {
        Float time = (Float) this.timeSignature.values().toArray()[this.timeSignature.size()-1]; //time signature of last "part"

        if((float) (num/den) > time)
            return new Object[]{Boolean.FALSE, time};

        String event_ref = this.addMeasureXml(num, den, octave, step, accidental);
        this.addMeasureTable(event_ref, num, den, octave, step, accidental);

        return new Object[]{Boolean.TRUE};
    }

    /*
    Find the last "part" to get the string part_item_ref, the last "measure" to get the current progressive number
    of the measures and the last "voice" to get the string voice_item_ref.

    Create the measure and voice element and then depending on the type of inputs create the Chord or Rest element.
    Adds the element to the file and after saving it returns the event_ref string of the inserted measure
     */
    private String addMeasureXml(Integer num, Integer den, String octave, String step, String accidental) {
        try {
            NodeList dataParts = this.getParts();
            Node part = dataParts.item(dataParts.getLength()-1);
            String part_item_ref = this.xmlParser.getXPathExpression().evaluate(dataParts.item(dataParts.getLength()-1));

            NodeList dataMeasures = this.getMeasures();
            Integer number = Integer.valueOf(this.xmlParser.getXPathExpression().evaluate(dataMeasures.item(dataMeasures.getLength()-1)));

            NodeList dataVoices = this.getVoices();
            String voice_item_ref = this.xmlParser.getXPathExpression().evaluate(dataVoices.item(dataVoices.getLength()-1));

            Element newMeasure = this.xml.createElement("measure");
            newMeasure.setAttribute("number", String.valueOf(number+1));

            Element newVoice = this.xml.createElement("voice");
            newVoice.setAttribute("voice_item_ref", voice_item_ref);

            String event_ref;

            if(octave != null && step != null && accidental != null) {
                Element chord = this.newChordElement(num, den , octave, step, accidental, number, part_item_ref);
                newVoice.appendChild(chord);
                event_ref = chord.getAttribute("event_ref");
            } else {
                Element rest = this.newRestElement(num, den, number, part_item_ref);
                newVoice.appendChild(rest);
                event_ref = rest.getAttribute("event_ref");
            }

            newMeasure.appendChild(newVoice);
            part.appendChild(newMeasure);

            this.saveFile();

            return event_ref;

        } catch (XPathExpressionException | NumberFormatException ex) {
            throw new IllegalStateException(ex.getMessage());
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }

    /*
    Create a new Element for an element of type Chord
     */
    private Element newChordElement(Integer num, Integer den, String octave, String step, String accidental, Integer number, String part_item_ref) {
        if(octave == null)
            throw new IllegalArgumentException("octave == null");
        if(step == null)
            throw new IllegalArgumentException("step == null");
        if(accidental == null)
            throw new IllegalArgumentException("accidental == null");

        Element chord = this.xml.createElement("chord");
        chord.setAttribute("event_ref", part_item_ref+"_voice0"+"_measure"+String.valueOf(number+1)+"_ev0");

        Element duration = this.xml.createElement("duration");
        duration.setAttribute("num", String.valueOf(num));
        duration.setAttribute("den", String.valueOf(den));

        Element notehead = this.xml.createElement("notehead");
        Element pitch = this.xml.createElement("pitch");
        pitch.setAttribute("octave", octave);
        pitch.setAttribute("step", step);
        pitch.setAttribute("actual_accidental", accidental);

        notehead.appendChild(pitch);
        chord.appendChild(duration);
        chord.appendChild(notehead);

        return chord;
    }

    /*
    Create a new Element for an element of type Rest
     */
    private Element newRestElement(Integer num, Integer den, Integer number, String part_item_ref) {
        Element rest = this.xml.createElement("rest");
        rest.setAttribute("event_ref", part_item_ref+"_voice0"+"_measure"+String.valueOf(number+1)+"_ev0");

        Element duration = this.xml.createElement("duration");
        duration.setAttribute("num", String.valueOf(num));
        duration.setAttribute("den", String.valueOf(den));

        rest.appendChild(duration);

        return rest;
    }

    /*
    Creation of a Chord or Rest object according to the input data and insertion in the TableModel
     */
    private void addMeasureTable(String event_ref, Integer num, Integer den, String octave, String step, String accidental) {
        if(octave != null && step != null && accidental != null) {
            Chord newchord = new Chord(event_ref, num, den, Integer.parseInt(octave), step, accidental);
            TableModel model = (TableModel) this.table.getModel();
            model.addMeasure(newchord);
        } else {
            Rest newRest = new Rest(event_ref, num, den);
            TableModel model = (TableModel) this.table.getModel();
            model.addMeasure(newRest);
        }
    }

    /*** DELETE ***/
    public void deleteRow(Measure measure) throws TransformerException {
        Node node = this.findMeasure(measure);
        if(node == null)
            throw new IllegalStateException("node == null");

        this.deleteMeasureXml(node);
        this.saveFile();
    }

    private void deleteMeasureXml(Node node) {
        Node measure = node.getParentNode();
        measure.removeChild(node);
    }


    private NodeList getTimeSignature() {
        try {
            return this.xmlParser.evaluateXpath("/ieee1599/logic/los/staff_list/staff/time_signature", "@event_ref");

        } catch (NumberFormatException ex) {
            throw new IllegalStateException(ex.getMessage());
        }
    }

    private NodeList getParts() {
        try {
            return this.xmlParser.evaluateXpath("/ieee1599/logic/los/part", "@id");

        } catch (NumberFormatException ex) {
            throw new IllegalStateException(ex.getMessage());
        }
    }

    private NodeList getMeasures() {
        try {
            return this.xmlParser.evaluateXpath("/ieee1599/logic/los/part/measure", "@number");

        } catch (NumberFormatException ex) {
            throw new IllegalStateException(ex.getMessage());
        }
    }

    private NodeList getVoices() {
        try {
            return this.xmlParser.evaluateXpath("/ieee1599/logic/los/part/measure/voice", "@voice_item_ref");

        } catch (NumberFormatException ex) {
            throw new IllegalStateException(ex.getMessage());
        }
    }

    private NodeList getChords() {
        try {
            return this.xmlParser.evaluateXpath("/ieee1599/logic/los/part/measure/voice/chord", "@event_ref");
        } catch (NumberFormatException ex) {
            throw new IllegalStateException(ex.getMessage());
        }
    }

    private NodeList getRests() {
        try {
            return this.xmlParser.evaluateXpath("/ieee1599/logic/los/part/measure/voice/rest", "@event_ref");

        } catch (NumberFormatException ex) {
            throw new IllegalStateException(ex.getMessage());
        }
    }

    /*
    Utility method that find longest common substrings of two strings
     */
    private Set<String> longestCommonSubstrings(String s, String t) {
        int[][] table = new int[s.length()][t.length()];
        int longest = 0;
        Set<String> result = new HashSet<>();

        for (int i = 0; i < s.length(); i++) {
            for (int j = 0; j < t.length(); j++) {
                if (s.charAt(i) != t.charAt(j))
                    continue;

                table[i][j] = (i == 0 || j == 0) ? 1 : 1 + table[i - 1][j - 1];
                if (table[i][j] > longest) {
                    longest = table[i][j];
                    result.clear();
                }
                if (table[i][j] == longest) {
                    result.add(s.substring(i - longest + 1, i + 1));
                }
            }
        }
        return result;
    }

    public void setTable(JTable table) {
        this.table = table;
    }
}
