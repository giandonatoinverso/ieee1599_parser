import ieee1599.*;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {

        LoadData loader = new LoadData(new File("eleanor_rigby.xml"));
        File myInputFile = loader.getInputFile();

        List<Measure> measureList = loader.getMeasureList();
        TableModel measureModel = new TableModel(measureList, myInputFile);

        TablePanel tablePanel = new TablePanel();
        tablePanel.init(measureModel);
    }
}