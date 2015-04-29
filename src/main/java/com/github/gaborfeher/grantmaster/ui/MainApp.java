package com.github.gaborfeher.grantmaster.ui;

import com.github.gaborfeher.grantmaster.core.Utils;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainApp extends Application {
  private static final Logger logger = LoggerFactory.getLogger(MainApp.class);
  
  public final static String VERSION_STRING = "v0.7";
  
  private MainPageController controller;

  @Override
  public void init() throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
      @Override
      public void uncaughtException(Thread t, Throwable e) {
        logger.error("Exception in thread \"" + t.getName() + "\"", e);
      }
    });
    super.init();
  }
  
  @Override
  public void start(Stage stage) throws Exception {
    logger.info("startup");
    
    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainPage.fxml"));
    loader.setResources(Utils.getResourceBundle());
    Parent root = loader.load();
    controller = loader.getController();
    controller.setStage(stage);

    Scene scene = new Scene(root);
    scene.getStylesheets().add("/styles/Styles.css");
    stage.setTitle(Utils.getString("AppName") + " - " + VERSION_STRING);
    stage.setMaximized(true);
    stage.setScene(scene);
    stage.show();
    
    scene.getWindow().setOnCloseRequest((WindowEvent ev) -> {
      if (!controller.shutdown()) {
        ev.consume();  // Prevent the closing of this application.
      } else {
        logger.info("shutdown");
      }
    });
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
