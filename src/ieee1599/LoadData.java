package ieee1599;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/*
@author Giandonato Inverso <giandonato.inverso@studenti.unimi.it>

Loader class for extract and store "measure" data from IEEE1599 file
 */
public class LoadData {
    private final File inputFile;
    private final Document xmlDocument;
    private final List<Measure> measureList;
    private final XmlParser xmlParser;

    public LoadData(File file) throws ParserConfigurationException, IOException, SAXException {
        if(file == null)
            throw new IllegalArgumentException("file == null");

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance(); //istanzia un parser XML che produce alberi DOM a partire da documenti XML.
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder(); // crea istanza di DOM Document a partire da un documento XML
        this.inputFile = file;
        this.xmlDocument = docBuilder.parse(file);
        this.measureList = new ArrayList<>();
        this.xmlParser = new XmlParser(this.xmlDocument);
    }

    public Document getXmlDocument() {
        return this.xmlDocument;
    }

    public File getInputFile() {
        return this.inputFile;
    }

    public List<Measure> getMeasureList() throws XPathExpressionException {
        if(this.measureList.isEmpty())
            this.extractMeasure();

        return this.measureList;
    }

    /*
    Extract all "voice" elements from ieee1599 file

    For every element extracted, if it has only child called "duration", it extracts "num" and "den" elements, if it has
    more than one child extracts "num" and "den" in "duration" element and "octave", "step" and "actualAccidental" in
    notehead element

    Then, depending on current element, create Chord object or Rest object and insert it in MeasureList Array
     */
    private void extractMeasure() throws XPathExpressionException {
        String event_ref;
        Integer num = 0;
        Integer den = 0;
        Integer octave = 0;
        String step = "";
        String actualAccidental = "";

        NodeList voices = this.xmlParser.evaluateXpath("/ieee1599/logic/los/part/measure/voice", "@event_ref");

        for (int i = 0; i < voices.getLength(); i++) {
            NodeList children = voices.item(i).getChildNodes();

            for(int a = 0; a < children.getLength(); a++) {
                Node myNode = (Node) children.item(a);
                if(myNode.getNodeName().equals("#text"))
                    myNode.getParentNode().removeChild(myNode);
            }

            for(int k = 0; k < children.getLength(); k++) {
                event_ref = this.xmlParser.getXPathExpression().evaluate(children.item(k));

                NodeList myList = children.item(k).getChildNodes();

                for(int a = 0; a < myList.getLength(); a++) {
                    Node myNode = (Node) myList.item(a);
                    if(myNode.getNodeName().equals("#text"))
                        myNode.getParentNode().removeChild(myNode);
                }

                for(int a = 0; a < myList.getLength(); a++) {
                    Node myNode = (Node) myList.item(a);
                    Element myElement = (Element) myNode;

                    if(myList.getLength() == 1) {
                        if ("duration".equals(myNode.getNodeName())) {
                            num = Integer.valueOf(((Element) myNode).getAttribute("num"));
                            den = Integer.valueOf(((Element) myNode).getAttribute("den"));
                        }
                    } else {
                        switch (myNode.getNodeName()) {
                            case "duration" -> {
                                num = Integer.valueOf(((Element) myNode).getAttribute("num"));
                                den = Integer.valueOf(((Element) myNode).getAttribute("den"));
                            }
                            case "notehead" -> {
                                NodeList myPitchList = myNode.getChildNodes();
                                for(int j = 0; j < myPitchList.getLength(); j++) {
                                    Node myNodePitch = (Node) myPitchList.item(j);
                                    if(myNodePitch.getNodeName().equals("pitch")) {
                                        NodeList myPitchNodeList = myNodePitch.getChildNodes();
                                        octave = Integer.valueOf(((Element) myPitchNodeList).getAttribute("octave"));
                                        step = String.valueOf(((Element) myPitchNodeList).getAttribute("step"));
                                        actualAccidental = String.valueOf(((Element) myPitchNodeList).getAttribute("actual_accidental"));
                                    }
                                }
                            }
                        }
                    }
                }

                if(myList.getLength() == 1)
                    this.measureList.add(new Rest(event_ref, num, den));
                else
                    this.measureList.add(new Chord(event_ref, num, den, octave, step, actualAccidental));

            }
        }
    }

    @Deprecated
    private void extractChord() throws XPathExpressionException {
        String event_ref;
        Integer num = 0;
        Integer den = 0;
        Integer octave = 0;
        String step = "";
        String actualAccidental = "";

        NodeList chords = this.xmlParser.evaluateXpath("/ieee1599/logic/los/part/measure/voice/chord", "@event_ref");

        for (int i = 0; i < chords.getLength(); i++) {
            event_ref = this.xmlParser.getXPathExpression().evaluate(chords.item(i));

            NodeList myList = chords.item(i).getChildNodes();

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
                        num = Integer.valueOf(((Element) myNode).getAttribute("num"));
                        den = Integer.valueOf(((Element) myNode).getAttribute("den"));
                    }
                    case "notehead" -> {
                        NodeList myPitchList = myNode.getChildNodes();
                        for(int j = 0; j < myPitchList.getLength(); j++) {
                            Node myNodePitch = (Node) myPitchList.item(j);
                            if(myNodePitch.getNodeName().equals("pitch")) {
                                NodeList myPitchNodeList = myNodePitch.getChildNodes();
                                octave = Integer.valueOf(((Element) myPitchNodeList).getAttribute("octave"));
                                step = String.valueOf(((Element) myPitchNodeList).getAttribute("step"));
                                actualAccidental = String.valueOf(((Element) myPitchNodeList).getAttribute("actual_accidental"));
                            }
                        }
                    }
                }
            }

            this.measureList.add(new Chord(event_ref, num, den, octave, step, actualAccidental));
        }
    }

    @Deprecated
    private void extractRest() throws XPathExpressionException {
        String event_ref;
        Integer num = 0;
        Integer den = 0;

        NodeList rests = this.xmlParser.evaluateXpath("/ieee1599/logic/los/part/measure/voice/rest", "@event_ref");

        for (int i = 0; i < rests.getLength(); i++) {
            event_ref = this.xmlParser.getXPathExpression().evaluate(rests.item(i));

            NodeList myList = rests.item(i).getChildNodes();

            for(int a = 0; a < myList.getLength(); a++) {
                Node myNode = (Node) myList.item(a);
                if(myNode.getNodeName().equals("#text"))
                    myNode.getParentNode().removeChild(myNode);
            }

            for(int a = 0; a < myList.getLength(); a++) {
                Node myNode = (Node) myList.item(a);
                Element myElement = (Element) myNode;
                if ("duration".equals(myNode.getNodeName())) {
                    num = Integer.valueOf(((Element) myNode).getAttribute("num"));
                    den = Integer.valueOf(((Element) myNode).getAttribute("den"));
                }
            }

            this.measureList.add(new Rest(event_ref, num, den));
        }
    }
}
