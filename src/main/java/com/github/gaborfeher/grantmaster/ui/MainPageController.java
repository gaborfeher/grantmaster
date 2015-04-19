package com.github.gaborfeher.grantmaster.ui;

import com.github.gaborfeher.grantmaster.core.DatabaseConnectionSingleton;
import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.core.RefreshControlSingleton;
import com.github.gaborfeher.grantmaster.core.RefreshMessage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.h2.engine.Constants;

public class MainPageController implements Initializable {
  @FXML private Label pathLabel;
  @FXML Parent root;
  @FXML TabPane mainTabs;

  List<Project> projects;

  Stage stage;
  File path = null;
  
  @FXML ProjectListTabController projectListTabController;
  
  public void stop() {
    System.out.println("STOPPING: closing database connection");
    DatabaseConnectionSingleton.getInstance().close();
  }

  public void addProjectTab(final Project project) throws IOException {
    Tab newTab = new Tab(project.getName());
    mainTabs.getTabs().add(newTab);
    
    final ProjectTabController controller;
    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ProjectTab.fxml"));
    loader.setResources(ResourceBundle.getBundle("bundles.MainPage", new Locale("hu")));
    Parent root1 = loader.load();
    controller = loader.getController();
    controller.init(project);
    newTab.setContent(root1);
    
    newTab.setOnClosed(new EventHandler<Event>() {
      @Override
      public void handle(Event t) {
        RefreshControlSingleton.getInstance().broadcastDestroy(new RefreshMessage(project));
      }
    });
  }
  
  @FXML
  private void handleOpenButtonAction(ActionEvent event) {
    DatabaseConnectionSingleton connection = DatabaseConnectionSingleton.getInstance();
    connection.close();
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Open Resource File");
    fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("H2 Database Files (*" + Constants.SUFFIX_PAGE_FILE + ")", "*" + Constants.SUFFIX_PAGE_FILE));
    path = fileChooser.showOpenDialog(stage);
    if (path == null) {
      return;
    }
    String pathString = path.getAbsolutePath();
    if (!pathString.endsWith(Constants.SUFFIX_MV_FILE)) {
      return;
    }
    pathString = pathString.substring(0, pathString.length() - Constants.SUFFIX_MV_FILE.length());
    
    connection.connectTo(pathString);
    
    RefreshControlSingleton.getInstance().broadcastRefresh(null);

    pathLabel.setText(pathString);
  }



  @Override
  public void initialize(URL url, ResourceBundle rb) {
    projectListTabController.init(this);
  }    

  void setStage(Stage stage) {
    this.stage = stage;
  }
}
