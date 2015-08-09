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
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.HostServices;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javax.persistence.EntityManager;
import org.slf4j.LoggerFactory;

public class MainPageController implements Initializable {
  private static final org.slf4j.Logger logger = LoggerFactory.getLogger(MainPageController.class);

  /**
   * Number of static tabs shown. (The ones that are not open/close-able
   * project tabs.
   */
  private static final int NUM_SYSTEM_TABS = 5;

  private static final int ACTIVATE_TAB_AT_THIS_POS_AFTER_OPEN_PROJECT = 0;

  @FXML private TextField pathLabel;
  @FXML Parent root;
  @FXML TabPane mainTabs;

  List<Project> projects;

  Stage stage;

  @FXML ProjectListTabController projectListTabController;
  @FXML AboutTabController aboutTabController;

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
    mainTabs.getSelectionModel().select(ACTIVATE_TAB_AT_THIS_POS_AFTER_OPEN_PROJECT);
    TabSelectionChangeListener.activateTab(mainTabs.getTabs().get(ACTIVATE_TAB_AT_THIS_POS_AFTER_OPEN_PROJECT));
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

  private void tryOpenFile(File path) {
    List<String> errors = new ArrayList<>();
    DatabaseConnection connection = DatabaseConnection.openDatabase(path, errors);
    if (connection != null) {
      DatabaseSingleton.INSTANCE.setConnection(connection);
      resetAndRefreshTabs();
      pathLabel.setText(DatabaseSingleton.INSTANCE.getCurrentlyOpenArchiveFile().getAbsolutePath());
    } else {
      Alert alert = new Alert(AlertType.ERROR);
      alert.setTitle(Utils.getString("MainPage.OpenDatabase"));
      alert.setHeaderText(Utils.getString("MainPage.OpenDatabaseError"));
      String content = "";
      for (String error : errors) {
        content += Utils.getString(error) + "\n";
      }
      alert.setContentText(content);
      alert.showAndWait();
    }
  }

  private void showError(String title, String header, String content) {
    Alert alert = new Alert(AlertType.ERROR);
    if (title != null) {
      alert.setTitle(Utils.getString(title));
    }
    if (header != null) {
      alert.setHeaderText(Utils.getString(header));
    }
    if (content != null) {
      alert.setContentText(Utils.getString(content));
    }
    alert.showAndWait();
  }

  @FXML
  private void handleOpenButtonAction(ActionEvent event) {
    if (!allowCloseDatabase()) {
      return;
    }
    FileChooser fileChooser = getFileChooserForHdbFiles(Utils.getString("MainPage.OpenDatabase"));
    File path = fileChooser.showOpenDialog(stage);
    if (path == null) {
      return;
    }
    tryOpenFile(path);
  }

  @FXML
  private void handleNewButtonAction(ActionEvent event) {
    if (!allowCloseDatabase()) {
      return;
    }
    DatabaseConnection connection = DatabaseConnection.createNewDatabase();
    if (connection != null) {
      closeProjectTabs();
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
    FileChooser fileChooser = getFileChooserForHdbFiles(Utils.getString("MainPage.SaveDatabase"));
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
        alert.setTitle(Utils.getString("MainPage.SaveDatabase"));
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
    boolean success = false;
    if (DatabaseSingleton.INSTANCE.getCurrentlyOpenArchiveFile() == null) {
      File pathToSave = selectFileForSaving();
      if (pathToSave == null) {
        return;
      }
      success = DatabaseSingleton.INSTANCE.saveAsDatabase(pathToSave);
    } else {
      success = DatabaseSingleton.INSTANCE.saveDatabase();
    }
    if (!success) {
      showError("MainPage.SaveDatabase", "MainPage.SaveDatabaseError", null);
      return;
    }
    pathLabel.setText(DatabaseSingleton.INSTANCE.getCurrentlyOpenArchiveFile().getAbsolutePath());
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
    if (!DatabaseSingleton.INSTANCE.saveAsDatabase(selectedFile)) {
      showError("MainPage.SaveDatabase", "MainPage.SaveDatabaseError", null);
      return;
    }
    pathLabel.setText(DatabaseSingleton.INSTANCE.getCurrentlyOpenArchiveFile().getAbsolutePath());
  }

  @FXML
  private void handleExportSheetButtonAction(ActionEvent event) {
    FileChooser fileChooser = new FileChooser();
    fileChooser.getExtensionFilters().setAll(new FileChooser.ExtensionFilter("Excel spreadsheets (*.xls)", "*.xls"));
    fileChooser.setSelectedExtensionFilter(fileChooser.getExtensionFilters().get(0));
    fileChooser.setTitle(Utils.getString("MainPage.ExportToExcel"));
    File exportFile = fileChooser.showSaveDialog(stage);
    if (exportFile == null) {
      return;
    }
    if (!exportFile.getAbsolutePath().endsWith(".xls")) {
      exportFile = new File(exportFile.getAbsolutePath() + ".xls");
      if (exportFile.exists()) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(Utils.getString("MainPage.ExportToExcel"));
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

  @FXML
  private void handleContextHelpButtonAction(ActionEvent event) {
   final Popup popup = new Popup();

    Tab activeTab = TabSelectionChangeListener.getActiveTab();
    if (activeTab == null || activeTab.getUserData() == null) {
      return;
    }
    // The help text is currently stored in userData.
    // TODO(gaborfeher): Find a better place.
    String helpText = (String) activeTab.getUserData();
    Label popupLabel = new Label(helpText);
    popupLabel.setStyle("-fx-border-color: black;");
    popup.setAutoHide(true);
    popup.setAutoFix(true);
    // Calculate popup placement coordinates.
    Node eventSource = (Node) event.getSource();
    Bounds sourceNodeBounds = eventSource.localToScreen(eventSource.getBoundsInLocal());
    popup.setX(sourceNodeBounds.getMinX() - 5.0);
    popup.setY(sourceNodeBounds.getMaxY() + 5.0);
    popup.getContent().addAll(popupLabel);
    popup.show(stage);
  }

  @Override
  public void initialize(URL url, ResourceBundle rb) {
    projectListTabController.init(this);
    mainTabs.getSelectionModel().selectedItemProperty().addListener(new TabSelectionChangeListener());
    mainTabs.getSelectionModel().select(4);
  }

  void setStage(Stage stage) {
    this.stage = stage;
  }

  void setHostServices(HostServices hostServices) {
    aboutTabController.setHostServices(hostServices);
  }

}
