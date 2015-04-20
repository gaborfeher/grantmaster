package com.github.gaborfeher.grantmaster.ui;

import com.github.gaborfeher.grantmaster.core.DatabaseConnectionSingleton;
import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.core.RefreshControlSingleton;
import com.github.gaborfeher.grantmaster.core.TransactionRunner;
import com.github.gaborfeher.grantmaster.logic.wrappers.CurrencyManager;
import com.github.gaborfeher.grantmaster.logic.wrappers.BudgetCategoryWrapper;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;
import java.util.Locale;
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
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.persistence.EntityManager;
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

  private void closeProjectTabs() {
    mainTabs.getTabs().remove(4, mainTabs.getTabs().size());
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
        controller.destroy();
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
      Alert alert = new Alert(AlertType.ERROR);
      alert.setTitle("Fájl megnyitás");
      alert.setHeaderText("Megnyitáshoz a fájl kiterjesztése .mv.db kell, hogy legyen.");
      alert.showAndWait();
      return;
    }
    if (!path.exists()) {
      Alert alert = new Alert(AlertType.ERROR);
      alert.setTitle("Fájl megnyitás");
      alert.setHeaderText("A fájl nem létezik.");
      alert.showAndWait();
      return;
    }

    pathString = pathString.substring(0, pathString.length() - Constants.SUFFIX_MV_FILE.length());
    if (!connection.connectTo(pathString)) {
      Alert alert = new Alert(AlertType.ERROR);
      alert.setTitle("Adatbázis megnyitás");
      alert.setHeaderText("Hiba az adatbázisfájl megnyitása közben.");
      alert.setContentText("Próbálj meg minden alkalmazást bezárni,\namiben ez a fájl meg van nyitva.");
      alert.showAndWait();
      return;
    }
    closeProjectTabs();
    RefreshControlSingleton.getInstance().broadcastRefresh();
    pathLabel.setText(pathString);
  }
  
  @FXML
  private void handleNewButtonAction(ActionEvent event) {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Adatbázis létrehozása");
    fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("H2 Database Files (*" + Constants.SUFFIX_PAGE_FILE + ")", "*" + Constants.SUFFIX_PAGE_FILE));
    path = fileChooser.showSaveDialog(stage);
    if (path == null) {
      return;
    }
    String pathString = path.getAbsolutePath();
    if (!pathString.endsWith(Constants.SUFFIX_MV_FILE)) {
      pathString += Constants.SUFFIX_MV_FILE;
    }
    path = new File(pathString);
    if (path.exists()) {
      try {
        Files.delete(path.toPath());
      } catch (IOException ex) {
        Logger.getLogger(MainPageController.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
    
    DatabaseConnectionSingleton connection = DatabaseConnectionSingleton.getInstance();
    connection.close();
    closeProjectTabs();
    
    pathString = pathString.substring(0, pathString.length() - Constants.SUFFIX_MV_FILE.length());
    connection.connectTo(pathString);

    boolean result = connection.runInTransaction(new TransactionRunner() {
      @Override
      public boolean run(EntityManager em) {
        CurrencyManager.createDefaultCurrencies(em);
        BudgetCategoryWrapper.createDefaultBudgetCategories(em);
        return true;
      }
    });
    if (!result) {
      return;
    }
    RefreshControlSingleton.getInstance().broadcastRefresh();
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
