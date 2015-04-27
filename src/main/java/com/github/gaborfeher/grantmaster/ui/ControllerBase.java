package com.github.gaborfeher.grantmaster.ui;

import com.github.gaborfeher.grantmaster.core.DatabaseSingleton;
import com.github.gaborfeher.grantmaster.logic.wrappers.EntityWrapper;
import java.net.URL;
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
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyEvent;
import javax.persistence.EntityManager;
import javax.validation.ConstraintViolation;

public abstract class ControllerBase<T extends EntityWrapper> implements Initializable {
  @FXML private TableView<T> table;
  
  @FXML private Node mainNode;

  private ResourceBundle resourceBundle;
 
  protected abstract T createNewEntity(EntityManager em);
  protected abstract void getItemListForRefresh(EntityManager em, List<T> items);
  
  static class TableSelectionSaver<T extends EntityWrapper> {
    TableView<T> table = null;
    TableColumn selectedColumn = null;
    Object selectedEntityId = null;  // null means first line (new item)
    int selectedRow = 0;

    TableSelectionSaver(TableView<T> table) {
      this.table = table;
      if (table.getSelectionModel().getSelectedCells().size() > 0) {
        TablePosition selectedCell =
            table.getSelectionModel().getSelectedCells().get(0);
        selectedColumn = selectedCell.getTableColumn();
        selectedEntityId = table.getItems().get(selectedCell.getRow()).getId();
        selectedRow = selectedCell.getRow();
      }
    }
    
    void restore() {
      if (selectedColumn != null) {
        if (selectedEntityId != null) {
          int row = 0;
          while (row < table.getItems().size() && 
                 !selectedEntityId.equals(table.getItems().get(row).getId())) {
            row++;
          }
          if (row < table.getItems().size()) {
            selectedRow = row;
          }
        }
        if (selectedRow < table.getItems().size()) {
          table.getSelectionModel().clearAndSelect(
              selectedRow, selectedColumn);
        }
      } else {
        table.getSelectionModel().clearSelection();
      }
    }
  }
  
  public void discardNew() {
    table.getItems().clear();
    onRefresh();
  }
  
  public void onRefresh() {
    Platform.runLater(() -> {
      TableSelectionSaver selectedCell = null;
      if (table != null) {
        selectedCell = new TableSelectionSaver(table);
        table.getSelectionModel().clearSelection();
      }
      ControllerBase.this.refreshContent();
      if (selectedCell != null) {
        selectedCell.restore();
      }
      if (table != null) {
        table.requestFocus();
      }
    });
  }
  
  protected void refreshContent() {
    DatabaseSingleton.INSTANCE.query((EntityManager em) -> {
      ObservableList<T> items = table.getItems();
      if (items.isEmpty() ||
          items.get(0).getState() != EntityWrapper.State.EDITING_NEW) {
        items.clear();
        // Add the editable empty new element at first position.
        T wrapper = createNewEntity(em);
        if (wrapper != null) {
          wrapper.setState(EntityWrapper.State.EDITING_NEW);
          items.add(wrapper);
        }
      } else {
        // Keep the editable empty new element at first position, remove the
        // rest.
        items.remove(1, items.size());
      }
      getItemListForRefresh(em, items);
      addMyselfAsParent(items);
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
}
