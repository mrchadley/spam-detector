package SpamDetector;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;

public class Main extends Application
{
    DirectoryChooser directoryChooser = new DirectoryChooser();
    File trainingDirectory;
    File testingDirectory;


    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Spam Detector");
        primaryStage.setScene(new Scene(root, 640, 480));
        primaryStage.show();

        //choose training directory
        directoryChooser.setTitle("Choose Training Directory");
        directoryChooser.setInitialDirectory(new File("."));
        trainingDirectory = directoryChooser.showDialog(primaryStage);

        //train here
        System.out.println("Training Dir: " + trainingDirectory); //log


        //choose testing directory
        directoryChooser.setTitle("Choose Testing Directory");
        directoryChooser.setInitialDirectory(new File("."));
        testingDirectory = directoryChooser.showDialog(primaryStage);

        //test here
        System.out.println("Testing dir: " + testingDirectory); //log
    }


    public static void main(String[] args) {
        launch(args);
    }
}
