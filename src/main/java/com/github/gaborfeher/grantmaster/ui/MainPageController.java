package com.github.gaborfeher.grantmaster.ui;

import com.github.gaborfeher.grantmaster.core.DatabaseConnectionSingleton;
import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.core.RefreshControlSingleton;
import com.github.gaborfeher.grantmaster.core.RefreshMessage;
import com.github.gaborfeher.grantmaster.logic.wrappers.CurrencyManager;
import com.github.gaborfeher.grantmaster.logic.wrappers.ExpenseTypeWrapper;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
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
    fileChooser.setTitle("Adatbázis megnyitása");
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

  
  @FXML
  private void handleNewButtonAction(ActionEvent event) {

    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Adatbázis létrehozása");
    fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("H2 Database Files (*" + Constants.SUFFIX_PAGE_FILE + ")", "*" + Constants.SUFFIX_PAGE_FILE));
    path = fileChooser.showOpenDialog(stage);
    if (path == null) {
      return;
    }
    String pathString = path.getAbsolutePath();
    if (!pathString.endsWith(Constants.SUFFIX_MV_FILE)) {
      pathString += Constants.SUFFIX_MV_FILE;
    }
    path = new File(pathString);
    if (path.exists()) {
      Alert alert = new Alert(AlertType.CONFIRMATION);
      alert.setTitle("Fájl felülírás");
      alert.setHeaderText("Ez a fájl már létezik");
      alert.setContentText("Ha ezt választod, elveszik a tartalma");
      Optional<ButtonType> result = alert.showAndWait();
      if (result.get() == ButtonType.OK){
        try {
          Files.delete(path.toPath());
        } catch (IOException ex) {
          Logger.getLogger(MainPageController.class.getName()).log(Level.SEVERE, null, ex);
        }
      } else {
        return;
      }
    }
    
    DatabaseConnectionSingleton connection = DatabaseConnectionSingleton.getInstance();
    connection.close();
    
    pathString = pathString.substring(0, pathString.length() - Constants.SUFFIX_MV_FILE.length());
    connection.connectTo(pathString);

    connection.em().getTransaction().begin();
    CurrencyManager.createDefaultCurrencies();
    ExpenseTypeWrapper.createDefaultExpenseTypes();
    connection.em().getTransaction().commit();
    
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
