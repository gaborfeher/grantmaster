/**
 * This file is a part of GrantMaster.
 * Copyright (C) 2015  Gábor Fehér <feherga@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.gaborfeher.grantmaster.ui;

import com.github.gaborfeher.grantmaster.framework.utils.Utils;
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
    System.out.println("getHostServices() " + getHostServices());
    controller.setHostServices(getHostServices());
    Scene scene = new Scene(root);
    scene.getStylesheets().add("/styles/Styles.css");
    stage.setTitle(Utils.getString("AppName"));
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
