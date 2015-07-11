/*
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
package com.github.gaborfeher.grantmaster.framework.base;

import com.github.gaborfeher.grantmaster.framework.utils.DatabaseSingleton;
import com.github.gaborfeher.grantmaster.framework.utils.Utils;
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
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyEvent;
import javax.persistence.EntityManager;
import javax.validation.ConstraintViolation;

/**
 * Base class for most of the tab controllers in this applications.
 * All the tab controllers that are showing an (editable) table with a list
 * of entities are derived from this class. (The entities in the table are
 * objects implementing EditableTableRowItems. In current implementation, they
 * are all JPA Entities wrapped in their corresponding EntityWrapper
 * subclasses.)
 * @param <T> 
 */
public abstract class TablePageControllerBase<T extends EditableTableRowItem>
    implements Initializable {
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
  }
  
  // TODO(gaborfeher): Some subclasses are using detached entitites in this,
  // so they don't pick up subsequent changes after the first resresh.
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
    TablePageControllerBase.this.refreshTableContent();
    if (selectedCell != null) {
      selectedCell.restore();
    }
    if (table != null) {
      table.requestFocus();
    }
  }
  
  /**
   * Refreshes the content of the table immediately, using queried data
   * from the underlying database. The data is retrieved using
   * getItemListForRefresh, which subclasses may override. This method does not
   * care about maintaining correct focus and cell selection state of the table.
   */
  protected void refreshTableContent() {
    DatabaseSingleton.INSTANCE.query((EntityManager em) -> {
      // The basic idea is to remove all the items from |table| and then add
      // them back. Apparently this is the best way to update a table in
      // Java FX.
      ObservableList<T> tableItems = table.getItems();
      // There is one more trick. The first row of the table is never stored
      // in the database, it's the row that the user can use to enter data for
      // a new entity. That row is saved and restored here, using the following
      // variable.
      T newEntityWrapper = null;
      if (!tableItems.isEmpty() &&
          tableItems.get(0).getState() == RowEditState.EDITING_NEW) {
        newEntityWrapper = tableItems.get(0);
      } else {
        newEntityWrapper = createNewEntity(em);
        if (newEntityWrapper != null) {
          newEntityWrapper.setState(RowEditState.EDITING_NEW);
        }
      }

      tableItems.clear();

      if (newEntityWrapper != null) {
        tableItems.add(newEntityWrapper);
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
    alert.setTitle(Utils.getString("BackendErrorTitle"));
    alert.setContentText(Utils.getString("BackendErrorText") + "\n(" + message + ")");
    alert.showAndWait();
  }
  
  public void showFailureDialog(String title, String content) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle(Utils.getString(title));
    alert.getDialogPane().setContent(new Label(Utils.getString(content)));
    alert.showAndWait();
  }
  
  public void showValidationFailureDialog(
      Set<ConstraintViolation<T>> constraintViolations) {
    String message = Utils.getString("ValidationProblems") + ":\n";
    for (ConstraintViolation<T> violation : constraintViolations) {
      String violationMessage = violation.getMessage();
      if (violationMessage.length() > 0 && violationMessage.charAt(0) == '%') {
        violationMessage =
            resourceBundle.getString(violationMessage.substring(1));
      }
      message += "*" + violationMessage+ "\n";
    }
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle(Utils.getString("ValidationTitle"));
    alert.setResizable(true);
    TextArea text = new TextArea(message);
    text.setWrapText(true);
    text.setEditable(false);
    alert.setGraphic(text);
    alert.showAndWait();
  }
  
  public static void exportActiveTabToXls(File file) {
    TablePageControllerBase activeController = TabSelectionChangeListener.getActiveTabController();
    if (activeController != null) {
      activeController.exportToXls(file);
    }
  }

  private void exportToXls(File file) {  
    ExcelExporter exporter = new ExcelExporter(table);
    exporter.export(file);
  }
}
