package SpamDetector;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Main extends Application
{
    DirectoryChooser directoryChooser = new DirectoryChooser();

    File dataDirectory;

    Map<String, Integer> hamMap = new TreeMap<>();
    Map<String, Integer> spamMap = new TreeMap<>();
    Map<String, Double> probabilityMap = new TreeMap<>();

    LinkedList<TestFile> testFiles = new LinkedList<>();
    double accuracy;
    double precision;

    private BorderPane layout;
    private TableView<TestFile> table = new TableView<>();

    @Override
    public void start(Stage primaryStage) throws Exception{
        primaryStage.setTitle("Spam Detector");

        //choose data directory
        directoryChooser.setTitle("Choose data Directory");
        directoryChooser.setInitialDirectory(new File("."));
        dataDirectory = directoryChooser.showDialog(primaryStage);



        //train here
        Train();

        //test here
        testFiles = TestProbability();

        int correctGuesses = 0;
        int correctSpam = 0;
        int spamGuesses = 0;
        for(TestFile testFile : testFiles)
        {
            //check spam correctness
            if(testFile.getActualClass() == "spam" && testFile.getSpamProbability() >= 0.5)
            {
                correctGuesses++;
                correctSpam++;
            }
            //check ham correctness
            else if(testFile.getActualClass() == "ham" && testFile.getSpamProbability() < 0.5)
            {
                correctGuesses++;
            }
            //check spam guesses
            if(testFile.getSpamProbability() >= 0.5)
            {
                spamGuesses++;
            }
        }
        accuracy = (double)correctGuesses / (double)testFiles.size();
        precision = (double)correctSpam / (double)spamGuesses;


        final Label label = new Label("Address Book");
        label.setFont(new Font("Arial", 25));

        table.setEditable(false);
        table.getItems().addAll(testFiles);

        TableColumn<TestFile, String> fileCol = new TableColumn<>("File");
        fileCol.setMinWidth(280);
        fileCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFilename()));

        TableColumn<TestFile, String> actualClassCol = new TableColumn<>("Actual Class");
        actualClassCol.setMinWidth(110);
        actualClassCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getActualClass()));

        TableColumn<TestFile, String> spamProbCol = new TableColumn<>("Spam Probability");
        spamProbCol.setMinWidth(250);
        spamProbCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSpamProbRounded()));



        table.getColumns().addAll(fileCol, actualClassCol, spamProbCol);

        final VBox vbox = new VBox();
        vbox.setSpacing(5);
        vbox.setPadding(new Insets(10, 0, 0, 10));
        vbox.getChildren().addAll(label, table);

        GridPane editArea = new GridPane();
        editArea.setPadding(new Insets(10, 10, 10, 10));
        editArea.setVgap(10);
        editArea.setHgap(10);

        Label accuracyLabel = new Label("Accuracy:");
        editArea.add(accuracyLabel, 0, 0);
        TextField accuracyField = new TextField();
        accuracyField.setPromptText("");
        editArea.add(accuracyField, 1, 0);
        accuracyField.setEditable(false);

        Label precisionLabel = new Label("Precision:");
        editArea.add(precisionLabel, 0, 1);
        TextField precisionField = new TextField();
        precisionField.setPromptText("");
        editArea.add(precisionField, 1, 1);
        precisionField.setEditable(false);


        layout = new BorderPane();
        layout.setTop(table);
        layout.setBottom(editArea);

        Scene scene = new Scene(layout, 640, 480);
        primaryStage.setScene(scene);
        primaryStage.show();


    }


    public static void main(String[] args) {
        launch(args);
    }

    void Train() throws IOException {
        CountWords(new File(dataDirectory + "/train/ham"), hamMap);
        CountWords(new File(dataDirectory + "/train/spam"), spamMap);

        CalculateProbabilityMap();

        System.out.println(hamMap);
        System.out.println(spamMap);
        System.out.println(probabilityMap);
    }
    void CountWords(File dir, Map<String, Integer> map) throws IOException
    {
        try {
            for (File file : dir.listFiles()) {
                LinkedList<String> words = new LinkedList<>(); //holds a string if it shows up in the file
                Scanner scanner = new Scanner(file);
                while (scanner.hasNext()) {
                    String string = scanner.next();
                    if (Filter(string) && !words.contains(string)) {
                        words.add(string);
                    }
                }
                //adds strings from words list to the map so that it only adds increases
                //the count if it shows up in the file, not for each time it shows in a file
                for (String str: words) {
                    if (map.containsKey(str)) {
                        map.put(str, map.get(str) + 1);
                    } else {
                        map.put(str, 1);
                    }
                }
            }
        }catch(IOException e){
        }
    }
    boolean Filter(String string)
    {
        String pattern = "^[a-zA-Z]*$";
        if(string.matches(pattern))
            return true;
        return false;
    }

    void CalculateProbabilityMap()
    {
        for(String word : spamMap.keySet())
        {
            //wordinspam = spamMap.get(word)
            //wordinham = hamMap.get(word)
            //spamfiles = spamMap.size()
            //hamfiles = hamMap.size()
            //probability = (wordinspam/spamfiles)/((wordinspam/spamfiles)+(wordinham/hamfiles))


            double probInSpam = (double)spamMap.get(word) / (double)spamMap.size();
            double probInHam = 0.0;
            if(hamMap.containsKey(word))
                probInHam = (double)hamMap.get(word) / (double)hamMap.size();
            double probability = probInSpam / (probInSpam + probInHam);

            probabilityMap.put(word, probability);
        }
    }

    LinkedList<TestFile> TestProbability() throws IOException
    {
        LinkedList<TestFile> testFiles = new LinkedList<>();
        File testDir = new File(dataDirectory + "/test");
        if (testDir.exists()) {
            for (File dir : testDir.listFiles()) {
                if (dir.isDirectory()) {
                    for (File file : dir.listFiles()) {
                        Scanner scanner = new Scanner(file);
                        LinkedList<String> words = new LinkedList<>();

                        if (scanner.hasNext()) {
                            String word = scanner.next();
                            if (probabilityMap.containsKey(word) && !words.contains(word)) {
                                words.add(word);
                            }
                        }
                        double n = 0;
                        for (String word : words) {
                            n += Math.log(1 - probabilityMap.get(word) - Math.log(probabilityMap.get(word)));
                        }
                        double probability = 1 / (1 + Math.pow(Math.E, n));

                        testFiles.add(new TestFile(file.getName(), probability, dir.getName()));
                    }
                }
            }
        }
        return testFiles;
    }
}
