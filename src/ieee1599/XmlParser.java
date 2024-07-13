package ieee1599;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.xpath.*;

/*
@author Giandonato Inverso <giandonato.inverso@studenti.unimi.it>

Xml parser which implements the method for evaluating and compiling xpath expressions
 */
public class XmlParser {
    private final Document xmlDocument;
    private XPathFactory myXPathFactory;
    private final XPath myXPath;
    private XPathExpression compileExpression;

    public XmlParser(Document input) {
        if(input == null)
            throw new IllegalArgumentException("input == null");

        this.xmlDocument = input;
        this.myXPathFactory = XPathFactory.newInstance();
        this.myXPath = myXPathFactory.newXPath();
    }

    public NodeList evaluateXpath(String evaluateExpression, String compileExpression) {
        if(evaluateExpression == null)
            throw new IllegalArgumentException("expression == null");

        if(compileExpression == null)
            throw new IllegalArgumentException("compileExpression == null");

        try {
            NodeList result = (NodeList) (this.myXPath.evaluate(evaluateExpression,
                    this.xmlDocument, XPathConstants.NODESET));
            this.compileExpression = this.myXPath.compile(compileExpression);

            return result;
        } catch (XPathExpressionException | NumberFormatException exception) {
            throw new IllegalStateException(exception.getMessage());
        }
    }

    public XPathExpression getXPathExpression() {
        return this.compileExpression;
    }
}
