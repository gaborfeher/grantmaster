package com.github.gaborfeher.grantmaster.ui;

import com.github.gaborfeher.grantmaster.core.DatabaseConnection;
import com.github.gaborfeher.grantmaster.core.DatabaseSingleton;
import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.logic.wrappers.CurrencyWrapper;
import com.github.gaborfeher.grantmaster.logic.wrappers.GlobalBudgetCategoryWrapper;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.persistence.EntityManager;
import org.slf4j.LoggerFactory;

public class MainPageController implements Initializable {
  private static final org.slf4j.Logger logger = LoggerFactory.getLogger(MainPageController.class);
  
  @FXML private TextField pathLabel;
  @FXML Parent root;
  @FXML TabPane mainTabs;

  List<Project> projects;

  Stage stage;
  
  @FXML ProjectListTabController projectListTabController;
  
  /**
   * The database file which is open. null for newly created databases.
   * This is the file what the user sees at save/open, not the temporary
   * directory where we extract the databases.
   */
  File openedFile;
  
  private boolean allowCloseDatabase() {
    if (DatabaseSingleton.INSTANCE.getUnsavedChange()) {
      Alert alert = new Alert(AlertType.NONE);
      alert.setTitle("Mentés");
      alert.setHeaderText("Mentsük az adatokat bezárás előtt?");
      ButtonType saveButtonType = new ButtonType("Mentés");
      ButtonType discardButtonType = new ButtonType("Bezárás");
      ButtonType cancelButtonType = new ButtonType("Mégse");
      alert.getButtonTypes().add(saveButtonType);
      alert.getButtonTypes().add(discardButtonType);
      alert.getButtonTypes().add(cancelButtonType);
      ((Button) alert.getDialogPane().lookupButton(cancelButtonType)).setDefaultButton(true);
      ButtonType userChoice = alert.showAndWait().get();
      if (userChoice == cancelButtonType) {
        return false;
      } else if (userChoice == discardButtonType) {
        return true;
      } else if (userChoice == saveButtonType) {
        handleSaveButtonAction(null);
        return !DatabaseSingleton.INSTANCE.getUnsavedChange();
      } else {
        logger.warn("allowCloseDatabase(): unknown userChoice: {}", userChoice);
        return false;
      }
    }
    return true;
  }
  
  public boolean shutdown() {
    if (!allowCloseDatabase()) {
      return false;
    }
    DatabaseSingleton.INSTANCE.cleanup();
    return true;
  }
  
  private void closeProjectTabs() {
    mainTabs.getSelectionModel().selectFirst();
    mainTabs.getTabs().remove(4, mainTabs.getTabs().size());
  }
  
  private void resetAndRefreshTabs() {
    closeProjectTabs();
    mainTabs.getSelectionModel().selectFirst();
    TabSelectionChangeListener.activateTab(mainTabs.getTabs().get(0));
  }
 
  public void addProjectTab(final Project project) throws IOException {
    Tab newTab = new Tab(project.getName());
    mainTabs.getTabs().add(newTab);
    
    final ProjectTabController controller;
    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ProjectTab.fxml"));
    loader.setResources(ResourceBundle.getBundle("bundles.MainPage", new Locale("hu")));
    Parent projectPage = loader.load();
    controller = loader.getController();
    controller.init(project);
    newTab.setContent(projectPage);
    mainTabs.getSelectionModel().select(newTab);
  }
  
  private TabPane findTabPaneChild(Parent page) {
    for (Node child : page.getChildrenUnmodifiable()) {
      if (child instanceof TabPane) {
        return (TabPane) child;
      }
    }
    return null;
  }
  
  private FileChooser getFileChooserForHdbFiles(String title) {
    FileChooser fileChooser = new FileChooser();
    fileChooser.getExtensionFilters().setAll(new FileChooser.ExtensionFilter("zip-compressed hsqldb files", "*.hdb"));
    fileChooser.setSelectedExtensionFilter(fileChooser.getExtensionFilters().get(0));
    fileChooser.setTitle(title);
    return fileChooser;
  }
  
  @FXML
  private void handleOpenButtonAction(ActionEvent event) {
    if (!allowCloseDatabase()) {
      return;
    }
    FileChooser fileChooser = getFileChooserForHdbFiles("Adatbázis megnyitása");
    File path = fileChooser.showOpenDialog(stage);
    if (path == null) {
      return;
    }
    DatabaseConnection connection = DatabaseConnection.openDatabase(path);
    if (connection != null) {
      DatabaseSingleton.INSTANCE.setConnection(connection);
      openedFile = path;
      resetAndRefreshTabs();
      pathLabel.setText(openedFile.getAbsolutePath());
    } else {
      Alert alert = new Alert(AlertType.ERROR);
      alert.setTitle("Adatbázis megnyitás");
      alert.setHeaderText("Hiba az adatbázisfájl megnyitása közben.");
      alert.showAndWait();
      return;
    }
  }
  
  @FXML
  private void handleNewButtonAction(ActionEvent event) {
    if (!allowCloseDatabase()) {
      return;
    }
    DatabaseConnection connection = DatabaseConnection.createNewDatabase();
    if (connection != null) {
      closeProjectTabs();
      openedFile = null;
      DatabaseSingleton.INSTANCE.setConnection(connection);
      boolean result = DatabaseSingleton.INSTANCE.transaction((EntityManager em) -> {
        CurrencyWrapper.createDefaultCurrencies(em);
        GlobalBudgetCategoryWrapper.createDefaultBudgetCategories(em);
        return true;
      });
      if (!result) {
        DatabaseSingleton.INSTANCE.setConnection(null);
        return;
      }
      resetAndRefreshTabs();
      pathLabel.setText("új adatbázis");
    }
  }
  
  @FXML
  private void handleSaveButtonAction(ActionEvent event) {
    if (openedFile == null && DatabaseSingleton.INSTANCE.isConnected()) {
      FileChooser fileChooser = getFileChooserForHdbFiles("Adatbázis mentése");
      openedFile = fileChooser.showSaveDialog(stage);
      if (openedFile == null) {
        return;
      }
      if (!openedFile.getAbsolutePath().endsWith(".hdb")) {
        openedFile = new File(openedFile.getAbsolutePath() + ".hdb");
        if (openedFile.exists()) {
          Alert alert = new Alert(AlertType.CONFIRMATION);
          alert.setTitle("A fájl már létezik");
          alert.setHeaderText("Felülírhatom ezt a fájlt?\n" + openedFile.getAbsolutePath());
          if (alert.showAndWait().get() != ButtonType.OK) {
            openedFile = null;
          } 
        }
      }
    }
    if (openedFile == null) {
      return;
    }
    try {
      DatabaseSingleton.INSTANCE.saveDatabase(openedFile);
      pathLabel.setText(openedFile.getAbsolutePath());
    } catch (IOException ex) {
      logger.error(null, ex);
    }    
  }
  
  @FXML
  private void handleExportSheetButtonAction(ActionEvent event) {
    FileChooser fileChooser = new FileChooser();
    fileChooser.getExtensionFilters().setAll(new FileChooser.ExtensionFilter("Excel spreadsheets (*.xls)", "*.xls"));
    fileChooser.setSelectedExtensionFilter(fileChooser.getExtensionFilters().get(0));
    fileChooser.setTitle("Exportálás Excel táblázatba");
    File exportFile = fileChooser.showSaveDialog(stage);
    if (exportFile == null) {
      return;
    }
    if (!exportFile.getAbsolutePath().endsWith(".xls")) {
      exportFile = new File(exportFile.getAbsolutePath() + ".xls");
      if (exportFile.exists()) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("A fájl már létezik");
        alert.setHeaderText("Felülírhatom ezt a fájlt?\n" + exportFile.getAbsolutePath());
        if (alert.showAndWait().get() != ButtonType.OK) {
          exportFile = null;
        } 
      }
    }
    
    ControllerBase.exportActiveTabToXls(exportFile);
  }

  @Override
  public void initialize(URL url, ResourceBundle rb) {
    projectListTabController.init(this);
    mainTabs.getSelectionModel().selectedItemProperty().addListener(new TabSelectionChangeListener());
  }    

  void setStage(Stage stage) {
    this.stage = stage;
  }

 
}
