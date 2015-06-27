package com.github.gaborfeher.grantmaster.ui;

import com.github.gaborfeher.grantmaster.framework.base.TablePageControllerBase;
import com.github.gaborfeher.grantmaster.framework.base.TabSelectionChangeListener;
import com.github.gaborfeher.grantmaster.framework.utils.DatabaseConnection;
import com.github.gaborfeher.grantmaster.framework.utils.DatabaseSingleton;
import com.github.gaborfeher.grantmaster.framework.utils.Utils;
import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.logic.wrappers.CurrencyWrapper;
import com.github.gaborfeher.grantmaster.logic.wrappers.GlobalBudgetCategoryWrapper;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
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
  
  /**
   * Number of static tabs shown. (The ones that are not open/close-able
   * project tabs.
   */
  private static final int NUM_SYSTEM_TABS = 4;
  
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
      alert.setTitle(Utils.getString("SaveBeforeCloseTitle"));
      alert.setHeaderText(Utils.getString("SaveBeforeCloseQuestion"));
      ButtonType saveButtonType = new ButtonType(Utils.getString("SaveBeforeCloseSave"));
      ButtonType discardButtonType = new ButtonType(Utils.getString("SaveBeforeCloseClose"));
      ButtonType cancelButtonType = new ButtonType(Utils.getString("SaveBeforeCloseCancel"));
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
    DatabaseSingleton.INSTANCE.close();
    return true;
  }
  
  private void closeProjectTabs() {
    mainTabs.getSelectionModel().selectFirst();
    mainTabs.getTabs().remove(NUM_SYSTEM_TABS, mainTabs.getTabs().size());
  }
  
  private void resetAndRefreshTabs() {
    closeProjectTabs();
    mainTabs.getSelectionModel().selectFirst();
    TabSelectionChangeListener.activateTab(mainTabs.getTabs().get(0));
  }
 
  public void addProjectTab(final Project project) throws IOException {
    for (int i = NUM_SYSTEM_TABS; i < mainTabs.getTabs().size(); ++i) {
      Tab alreadyOpenTab = mainTabs.getTabs().get(i);
      if (project.getName().equals(alreadyOpenTab.getText())) {
        // The requested project is already open.
        mainTabs.getSelectionModel().select(alreadyOpenTab);
        return;
      }
    }
    // Do the Java FX magic of opening the tab.
    Tab newTab = new Tab(project.getName());
    mainTabs.getTabs().add(newTab);
    final ProjectTabController controller;
    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ProjectTab.fxml"));
    loader.setResources(Utils.getResourceBundle());
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
    FileChooser fileChooser = getFileChooserForHdbFiles(Utils.getString("OpenDatabase"));
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
      alert.setTitle(Utils.getString("OpenDatabase"));
      alert.setHeaderText(Utils.getString("OpenDatabaseError"));
      alert.showAndWait();
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
      pathLabel.setText(Utils.getString("MainPage.StatusNewDatabase"));
    }
  }
  
  private File selectFileForSaving() {
    FileChooser fileChooser = getFileChooserForHdbFiles(Utils.getString("SaveDatabase"));
    File selectedFile = fileChooser.showSaveDialog(stage);
    if (selectedFile == null) {
      return null;
    }
    if (!selectedFile.getAbsolutePath().endsWith(".hdb")) {
      selectedFile = new File(selectedFile.getAbsolutePath() + ".hdb");
      // The save dialog normally checks if the file exists, but now we need
      // to check again for the .hdb file.
      if (selectedFile.exists()) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(Utils.getString("SaveDatabase"));
        alert.setHeaderText(Utils.getString("SaveOverrideQuestion") + "\n" + selectedFile.getAbsolutePath());
        if (alert.showAndWait().get() != ButtonType.OK) {
          return null;
        } 
      }
    }
    return selectedFile;
  }
  
  @FXML
  private void handleSaveButtonAction(ActionEvent event) {
    if (!DatabaseSingleton.INSTANCE.isConnected()) {
      return;
    }
    if (openedFile == null) {
      openedFile = selectFileForSaving();
      if (openedFile == null) {
        return;
      }
    }
    try {
      DatabaseSingleton.INSTANCE.saveDatabase(openedFile);
      pathLabel.setText(openedFile.getAbsolutePath());
    } catch (IOException ex) {
      logger.error(null, ex);
    }    
  }
  
  @FXML
  private void handleSaveAsButtonAction(ActionEvent event) {
    if (!DatabaseSingleton.INSTANCE.isConnected()) {
      return;
    }
    File selectedFile = selectFileForSaving();
    if (selectedFile == null) {
      return;
    }
    openedFile = selectedFile;
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
    fileChooser.setTitle(Utils.getString("ExportToExcel"));
    File exportFile = fileChooser.showSaveDialog(stage);
    if (exportFile == null) {
      return;
    }
    if (!exportFile.getAbsolutePath().endsWith(".xls")) {
      exportFile = new File(exportFile.getAbsolutePath() + ".xls");
      if (exportFile.exists()) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(Utils.getString("ExportToExcel"));
        alert.setHeaderText(
            Utils.getString("SaveOverrideQuestion") +
            "\n" + 
            exportFile.getAbsolutePath());
        if (alert.showAndWait().get() != ButtonType.OK) {
          exportFile = null;
        } 
      }
    }
    
    TablePageControllerBase.exportActiveTabToXls(exportFile);
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