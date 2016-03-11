package SpamDetector;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

public class Main extends Application
{
    //Initializing file manager
    DirectoryChooser directoryChooser = new DirectoryChooser();

    File dataDirectory;

    //Initializing maps
    Map<String, Integer> hamMap = new TreeMap<>();
    Map<String, Integer> spamMap = new TreeMap<>();
    Map<String, Double> probabilityMap = new TreeMap<>();

    String spamClassName = "";

    //setting up test file data and other variables
    LinkedList<TestFile> testFiles = new LinkedList<>();
    double accuracy = 0.0;
    double precision = 0.0;
    DecimalFormat df = new DecimalFormat("0.00000");

    //creating tableview
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
        //variables for accuracy and precision + logic
        double correctGuesses = 0.0;
        double correctSpam = 0.0;
        double spamGuesses = 0.0;
        for(TestFile testFile : testFiles)
        {
            String ya;
            System.out.println(testFile.getActualClass());

            //check spam correctness
            if(testFile.getActualClass().intern() == "spam")
            {
                if (testFile.getSpamProbability() >= 0.5)
                {
                    correctGuesses++;
                    correctSpam++;
                }
            }
            else if(testFile.getActualClass().intern() == "ham")
            {
                //check ham correctness
                if(testFile.getSpamProbability() < 0.5)
                    correctGuesses++;
            }
                //check spam guesses
            if (testFile.getSpamProbability() >= 0.5) {
                spamGuesses++;
            }


        }
        accuracy = (double)correctGuesses / ((double)correctGuesses + (double)spamGuesses);
        precision = (double)correctSpam / (double)spamGuesses;

        System.out.println("accuracy: " + accuracy + ", precision: " + precision);

        //configuring table layout. adding columns.
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

        GridPane editArea = new GridPane();
        editArea.setPadding(new Insets(10, 10, 10, 10));
        editArea.setVgap(10);
        editArea.setHgap(10);

        //bottom pane stuff
        Label accuracyLabel = new Label("Accuracy:");
        editArea.add(accuracyLabel, 0, 0);
        TextField accuracyField = new TextField();
        accuracyField.setPromptText(df.format(accuracy));
        editArea.add(accuracyField, 1, 0);
        accuracyField.setEditable(false);

        Label precisionLabel = new Label("Precision:");
        editArea.add(precisionLabel, 0, 1);
        TextField precisionField = new TextField();
        precisionField.setPromptText(df.format(precision));
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
    //counting words from data
    void Train() throws IOException {
        CountWords(new File(dataDirectory + "/train/ham"), hamMap);
        CountWords(new File(dataDirectory + "/train/spam"), spamMap);

        spamClassName = new File(dataDirectory + "/train/spam").getName();

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
    //probability calculations below
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
        LinkedList<File> files = new LinkedList<>();
        LinkedList<TestFile> testFiles = new LinkedList<>();
        File testDir = new File(dataDirectory + "/test");
        if (testDir.exists())
        {
            files = AddToList(testDir, files);

            for(File file : files)
            {



                LinkedList<String> words = new LinkedList<>();

                Scanner scan = new Scanner(file);
                while(scan.hasNext())
                {
                    String word = scan.next();

                    if(probabilityMap.containsKey(word) && !words.contains(word))
                    {
                        words.add(word);
                        //System.out.println(word);
                    }
                }

                TestFile test = new TestFile(file.getName(), CalcProbability(words), file.getParentFile().getName());
                testFiles.add(test);
                //System.out.println(test.getFilename() + ", " + test.getActualClass() + ", " + test.getSpamProbRounded());
            }

            /*

            for (File dir : testDir.listFiles()) {
                if (dir.isDirectory()) {
                    for (File file : dir.listFiles()) {
                        Scanner scanner = new Scanner(file);
                        LinkedList<String> words = new LinkedList<>();

                        if (scanner.hasNext()) {
                            String word = scanner.next();
                            System.out.println(word);
                            if (probabilityMap.containsKey(word) && !words.contains(word)) {
                                words.add(word);
                            }
                        }
                        double n = 0;
                        for (String word : words) {
                            n += Math.log(1 - probabilityMap.get(word) - Math.log(probabilityMap.get(word)));
                            //System.out.println(word + ": " + n);
                        }
                        double probability = 1 / (1 + Math.pow(Math.E, n));

                        testFiles.add(new TestFile(file.getName(), probability, dir.getName()));
                    }
                }
            }*/
        }

        return testFiles;
    }
    LinkedList<File> AddToList(File fileName, LinkedList<File> testFiles)
    {
        if(fileName.isDirectory())
        {
            for (File file : fileName.listFiles())
            {
                AddToList(file, testFiles);
            }
        }
        else
        {
            testFiles.add(fileName);
        }
        return testFiles;
    }

    double CalcProbability(LinkedList<String> words)
    {
        double n = 0;
        for(String word : words)
        {
            n = Math.log(1 - probabilityMap.get(word) - Math.log(probabilityMap.get(word)));
        }
        double pr = 1.0/(1.0 + Math.pow(Math.E, n));
        return pr;
    }
}
