package ieee1599;

import org.xml.sax.SAXException;

import javax.swing.table.AbstractTableModel;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.util.List;

/*
@author Giandonato Inverso <giandonato.inverso@studenti.unimi.it>

This class is responsible for representing a data model for a table. Each row is a measure (chord or rest).
The class implements methods for extracting data from the measure container and changing the appropriate values ​​of a measure
only if allowed by the typology.

The class implements methods for deleting a measure and adding a new one
 */
public class TableModel extends AbstractTableModel {
    private final List<Measure> measureList;
    EditXml myXml;

    private final String[] columnNames = new String[] {
            "Event_ref", "Num", "Den", "Octave", "Step", "actualAccidental", "operations"
    };
    private final Class[] columnClass = new Class[] {
            String.class, Integer.class, Integer.class, Integer.class, String.class, String.class, String.class
    };

    public TableModel(List<Measure> measureList, File InputFile) throws ParserConfigurationException, IOException, SAXException {
        this.measureList = measureList;
        myXml = EditXml.getInstance(InputFile);
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columnClass[columnIndex];
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public int getRowCount() {
        return measureList.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Measure row;
        try {
            row = measureList.get(rowIndex);
        } catch(IndexOutOfBoundsException ex) {
            return null;
        }

        if (0 == columnIndex) {
            return row.getEvent_ref();
        }
        else if (1 == columnIndex) {
            return row.getNum();
        }
        else if (2 == columnIndex) {
            return row.getDen();
        }
        else if (3 == columnIndex) {
            return row.getOctave();
        }
        else if (4 == columnIndex) {
            return row.getStep();
        }
        else if (5 == columnIndex) {
            return row.getActualAccidental();
        }
        return null;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if(columnIndex == 0)    //event_ref
            return false;

        Measure row;
        try {
            row = measureList.get(rowIndex);
        } catch(IndexOutOfBoundsException ex) {
            return false;
        }

        if(row.getType() == 0)  //chord
            return true;
        else
            return columnIndex == 1 || columnIndex == 2 || columnIndex == 6;    //rest
    }

    @Override
    public void setValueAt(Object input, int rowIndex, int columnIndex) {
        if(columnIndex == 6)    //delete button
            return;

        Measure row;
        try {
            row = measureList.get(rowIndex);
        } catch(IndexOutOfBoundsException ex) {
            return;
        }

        if(row.getType() == 0) {
            if (1 == columnIndex) {
                row.setNum((Integer) input);
            }
            else if (2 == columnIndex) {
                row.setDen((Integer) input);
            }
            else if (3 == columnIndex) {
                row.setOctave((Integer) input);
            }
            else if (4 == columnIndex) {
                row.setStep((String) input);
            }
            else if (5 == columnIndex) {
                row.setActualAccidental((String) input);
            }
        }
        else {
            if (1 == columnIndex) {
                row.setNum((Integer) input);
            }
            else if (2 == columnIndex) {
                row.setDen((Integer) input);
            }
        }

        try {
            this.myXml.editMeasure(row);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }

    public void removeRow(int row) {
        measureList.remove(row);
    }

    public void addMeasure(Measure measure) {
        this.measureList.add(measure);
    }
}