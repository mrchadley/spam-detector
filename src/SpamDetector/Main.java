package SpamDetector;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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


    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Spam Detector");
        primaryStage.setScene(new Scene(root, 640, 480));
        primaryStage.show();

        //choose data directory
        directoryChooser.setTitle("Choose data Directory");
        directoryChooser.setInitialDirectory(new File("."));
        dataDirectory = directoryChooser.showDialog(primaryStage);



        //train here
        Train();





        //test here

    }


    public static void main(String[] args) {
        launch(args);
    }

    void Train() throws IOException {
        CountWords(new File(dataDirectory + "/train/ham"), hamMap);
        CountWords(new File(dataDirectory + "/train/spam"), spamMap);

        CalculateSpamProbability();

        System.out.println(hamMap);
        System.out.println(spamMap);
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

    void CalculateSpamProbability()
    {
        for(String word : spamMap.keySet())
        {

            //wordinspam = spamMap.get(word)
            //wordinham = hamMap.get(word)
            //spamfiles = spamMap.size()
            //hamfiles = hamMap.size()
            // (wordinspam/spamfiles)/((wordinspam/spamfiles)+(wordinham/hamfiles))

            double probInSpam = (double)spamMap.get(word) / (double)spamMap.size();
            double probInHam = (double)hamMap.get(word) / (double)hamMap.size();
            double probability = probInSpam / (probInSpam + probInHam);

            probabilityMap.put(word, probability);
        }
    }
}
