package Driver;
import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
import javafx.scene.Scene;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.layout.HBox;
import javafx.scene.image.ImageView;

import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.layout.HBox;
import javafx.scene.control.Button;
import javafx.scene.control.Label;



public class HandleEvent extends Application{
    @Override
    public void start(Stage primaryStage) throws Exception{
        FlowPane pi = new FlowPane();
        Button startButton = new Button("Start");
        FlowPane p1 = new FlowPane();
        pi.getChildren().add(startButton);
        Button backButton = new Button("Back");
        pi.getChildren().add(backButton);
        pi.setPrefSize(400,400);
        Scene scene = new Scene(pi);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Handle Event ");
        primaryStage.show();


    }
    public static void main(String[] args) {
        launch(args);
    }
}

 class OKdeHandlerClass implements EventHandler<ActionEvent>{
    @Override
    public void handle(ActionEvent e){
        System.out.println("Size is decreased");
    }
 }
 class OkHandlerClass implements EventHandler<ActionEvent>{
    @Override
            public void handle(ActionEvent event){
        System.out.println("Size is increased ");
    }
}
