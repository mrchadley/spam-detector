package SpamDetector;

import javafx.collections.*;
import javafx.collections.ObservableList;

import java.util.LinkedList;

//getting all data from test files for use
public class DataSource {


    LinkedList<TestFile> testFiles = new LinkedList<>();
    public DataSource(LinkedList<TestFile> testFiles)
    {
        this.testFiles = testFiles;
    }

    public ObservableList<TestFile> getAllData() {
        ObservableList<TestFile> data = FXCollections.observableArrayList();

        for(TestFile file : testFiles)
        {
            data.add(file);
        }

        return data;
    }
}