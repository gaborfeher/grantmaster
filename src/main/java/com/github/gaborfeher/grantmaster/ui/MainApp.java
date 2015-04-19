package com.github.gaborfeher.grantmaster.ui;

import java.util.Locale;
import java.util.ResourceBundle;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class MainApp extends Application {
  private MainPageController controller;

  @Override
  public void start(Stage stage) throws Exception {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainPage.fxml"));
    loader.setResources(ResourceBundle.getBundle("bundles.MainPage", new Locale("hu")));
    Parent root = loader.load();
    controller = loader.getController();
    controller.setStage(stage);


    Scene scene = new Scene(root);
    scene.getStylesheets().add("/styles/Styles.css");

    stage.setTitle("GrantMaster - Költségvetés Tervező - v0.1");
    stage.setMaximized(true);
    stage.setScene(scene);
    stage.show();
  }

  @Override
  public void stop() throws Exception {
    controller.stop();
  }

  /**
   * The main() method is ignored in correctly deployed JavaFX application.
   * main() serves only as fallback in case the application can not be
   * launched through deployment artifacts, e.g., in IDEs with limited FX
   * support. NetBeans ignores main().
   *
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    launch(args);
  }

}
