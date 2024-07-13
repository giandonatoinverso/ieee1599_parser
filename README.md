# IEEE1599 file parser

## Description

*IEEE1599 file parser* is a software designed to modify IEEE1599 XML files, a format used for representing musical scores. The software allows users to view, edit, and add musical measures (chords and rests) within a score. It leverages design patterns and technologies for efficient XML manipulation and a user-friendly interface built with JTable

## Core classes and responsibilities:

- **TableModel**: this class functions as the data model for the table displaying musical measures. It handles tasks such as extracting measure data, enabling modifications to valid values, and adding or removing measures. It implements the **AbstractTableModel** class from Swing, a GUI widget toolkit in Java.
- **TablePanel**: this class is responsible for creating and managing the user interface panel that houses the table of measures. It facilitates the display and interaction with the table, including buttons for adding or deleting measures.
- **TableFrame**: this class provides the main application window that contains the **TablePanel**. It is designed as a singleton class, ensuring that only one instance of the main window exists throughout the application's lifecycle.
- **LoadData**: this class handles the loading of measure data from the IEEE1599 XML file. It extracts information about measures (chords and rests) and stores them in a list for further processing.
- **EditXml**: this is a singleton class that manages modifications to the XML file. It enables editing existing measures, adding new ones, and saving changes to a new file.
- **XmlParser**: this class parses the XML file and evaluates XPath expressions to extract specific data from the measures. XPath is a language for navigating and querying XML documents, making it well-suited for this task.
- **Chord** and **Rest**: these classes represent musical chords and rests, respectively. They hold the data for each measure, such as duration, notes, and accidentals. These classes implement the **Measure** interface, promoting code reusability and maintainability.
- **AddFrame**: this class creates a dialog window for inserting new measures. It is also a singleton to prevent multiple dialogs from appearing simultaneously.

## Workflow

- **Data Loading**: the *LoadData* class reads the IEEE1599 XML file and creates a list of *Measure* objects (chords and rests), representing the musical measures in the file.
- **Display**: the *TablePanel* class constructs a table using the measure data loaded by *LoadData*. Each row in the table corresponds to a measure, and the columns display the measure's properties (duration, notes, etc.).
- **Editing**: users can modify measure values directly within the table. The *TableModel* class handles these changes and communicates them to the *EditXml* class, which updates the underlying XML file.
- **Adding Measures**: users can add new measures using the dialog window created by *AddFrame*. The entered data is passed to *EditXml*, which inserts the new measure into the XML file and updates the table.
- **Saving**: changes made to the XML file are saved to a new file using the *EditXml* class, preserving the original file.