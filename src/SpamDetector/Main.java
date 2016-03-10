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
        train();





        //test here

    }


    public static void main(String[] args) {
        launch(args);
    }

    public void train() throws IOException {
        CountWords(new File(dataDirectory + "/train/ham"), hamMap);
        CountWords(new File(dataDirectory + "/train/spam"), spamMap);

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
                    String word = scanner.next();
                    if (!words.contains(word)) {
                        words.add(word);
                    }
                }
                //adds strings from words list to the map so that it only adds increases
                //the count if it shows up in the file, not for each time it shows in a file
                for (String string : words) {
                    if (map.containsKey(string)) {
                        map.put(string, map.get(string) + 1);
                    } else {
                        map.put(string, 1);
                    }
                }
            }
        }catch(IOException e){
        }
    }
}
