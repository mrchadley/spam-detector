package SpamDetector;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

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
    private TableView table = new TableView();

    @Override
    public void start(Stage primaryStage) throws Exception{
        primaryStage.setTitle("Spam Detector");


        final Label label = new Label("Address Book");
        label.setFont(new Font("Arial", 25));

        table.setEditable(false);

        TableColumn col1 = new TableColumn("File");
        col1.setMinWidth(280);

        TableColumn col2 = new TableColumn("Actual Class");
        col2.setMinWidth(110);

        TableColumn col3 = new TableColumn("Spam Probability");
        col3.setMinWidth(250);

        table.getColumns().addAll(col1, col2, col3);

        final VBox vbox = new VBox();
        vbox.setSpacing(5);
        vbox.setPadding(new Insets(10, 0, 0, 10));
        vbox.getChildren().addAll(label, table);

        layout = new BorderPane();
        layout.setTop(table);

        Scene scene = new Scene(layout, 640, 480);
        primaryStage.setScene(scene);
        primaryStage.show();

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
