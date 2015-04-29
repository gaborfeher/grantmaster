package com.github.gaborfeher.grantmaster.ui;

import com.github.gaborfeher.grantmaster.core.DatabaseSingleton;
import com.github.gaborfeher.grantmaster.logic.wrappers.EntityWrapper;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyEvent;
import javax.persistence.EntityManager;
import javax.validation.ConstraintViolation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for most of the tab controllers in this applications.
 * All the tab controllers that are showing an (editable) table with a list
 * of entities are derived from this class. (The entities in the table are
 * JPA Entities wrapped in their corresponding EntityWrapper subclasses.)
 * @param <T> 
 */
public abstract class ControllerBase<T extends EntityWrapper> implements Initializable {
  /**
   * The main table displaying the data in this tab.
   */
  @FXML private TableView<T> table;
  
  /**
   * The root node of this tab. The only reason we need access to it here
   * is to store a pointer to this controller inside it as user payload. That
   * in turn is used by TabSelectionChangeListener.
   */
  @FXML private Node mainNode;

  private ResourceBundle resourceBundle;
 
  protected abstract T createNewEntity(EntityManager em);
  protected abstract void getItemListForRefresh(EntityManager em, List<T> items);
  
  static protected ControllerBase activeTab = null;
  
  
  public void discardNew() {
    table.getItems().clear();
    onRefresh();
  }
  
  /**
   * Refreshes the content of the table of this controller. The refresh
   * task is posted to the end of the the Java FX event queue to avoid
   * bad interactions with the processing of the current event. This protection
   * is needed for tricky events like cell editing is finished, etc.
   */
  public void onRefresh() {
    Platform.runLater(() -> {
      refreshOtherContent();
      refreshTableContentAndMaintainFocus();
    });
  }
  
  public void onMyTabIsSelected() {
    refreshOtherContent();
    refreshTableContentAndMaintainFocus();
    activeTab = this;
  }
  
  protected void refreshOtherContent() {
  }
  
  /**
   * Refreshes the content of the table immediately.
   */
  protected void refreshTableContentAndMaintainFocus() {
    TableSelectionSaver selectedCell = null;
    if (table != null) {
      selectedCell = new TableSelectionSaver(table);
      table.getSelectionModel().clearSelection();
    }
    ControllerBase.this.refreshTableContent();
    if (selectedCell != null) {
      selectedCell.restore();
    }
    if (table != null) {
      table.requestFocus();
    }
  }
  
  /**
   * Refreshes the content of the table immediately, using queried data
   * from the underlying database. Subclasses may add additional work
   * inside getItemListForRefresh. This method does not care about maintaining
   * correct focus and cell selection of the table.
   */
  protected void refreshTableContent() {
    DatabaseSingleton.INSTANCE.query((EntityManager em) -> {
      ObservableList<T> tableItems = table.getItems();
      if (tableItems.isEmpty() ||
          tableItems.get(0).getState() != EntityWrapper.State.EDITING_NEW) {
        tableItems.clear();
        // Add the editable empty new element at first position.
        T wrapper = createNewEntity(em);
        if (wrapper != null) {
          wrapper.setState(EntityWrapper.State.EDITING_NEW);
          tableItems.add(wrapper);
        }
      } else {
        // Keep the editable empty new element at first position, remove the
        // rest.
        tableItems.remove(1, tableItems.size());
      }
      List<T> queriedItems = new ArrayList<>();
      getItemListForRefresh(em, queriedItems);
      tableItems.addAll(queriedItems);
      addMyselfAsParent(tableItems);
      return true;
    });
  }
  
  protected ObservableList<TableColumn<T, ?>> getTableColumns() {
    return table.getColumns();
  }
  
  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    this.resourceBundle = resourceBundle;
    // Store a pointer to this controller in the main node of the JFX GUI node
    // strucutre.
    mainNode.getProperties().put("controller", this);
    
    if (table != null) {
      table.getSelectionModel().setCellSelectionEnabled(true);
      // Start editing the selected cell if a key is pressed.
      table.setOnKeyPressed(new EventHandler<KeyEvent>() {
        @Override
        public void handle(KeyEvent event) {
          if (!event.getCode().isDigitKey() && !event.getCode().isLetterKey()) {
            return;
          }
          ObservableList<TablePosition> selectedCells = table.getSelectionModel().getSelectedCells();
          if (selectedCells.size() != 1) {
            return;
          }
          TablePosition selectedCell = selectedCells.get(0);
          table.edit(selectedCell.getRow(), selectedCell.getTableColumn());
        }
      });
    }
  }
  
  private void addMyselfAsParent(List<T> items) {
    for (T entity : items) {
      entity.setParent(this);
    }
  }

  public void showBackendFailureDialog(String message) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle("Hiba");
    alert.setContentText("Nem sikerült a létrehozás\n(" + message + ")");
    alert.showAndWait();
  }
  
  public void showValidationFailureDialog(
      Set<ConstraintViolation<T>> constraintViolations) {
    String message = "Problémák:\n";
    for (ConstraintViolation<T> violation : constraintViolations) {
      String violationMessage = violation.getMessage();
      if (violationMessage.length() > 0 && violationMessage.charAt(0) == '%') {
        violationMessage =
            resourceBundle.getString(violationMessage.substring(1));
      }
      message += "*" + violationMessage+ "\n";
    }
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle("Bevitel hiba");
    //alert.setHeaderText("Problémák:");
    //alert.setContentText(message);
    alert.setResizable(true);
    TextArea text = new TextArea(message);
    text.setWrapText(true);
    text.setEditable(false);
    alert.setGraphic(text);
    alert.showAndWait();
  }
  
  static void exportActiveTabToXls(File file) {
    if (activeTab != null) {
      activeTab.exportToXls(file);
    }
  }

  private void exportToXls(File file) {  
    ExcelExporter exporter = new ExcelExporter(table);
    exporter.export(file);
  }
}
