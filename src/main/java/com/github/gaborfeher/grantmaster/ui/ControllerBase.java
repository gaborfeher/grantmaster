package com.github.gaborfeher.grantmaster.ui;

import com.github.gaborfeher.grantmaster.core.DatabaseSingleton;
import com.github.gaborfeher.grantmaster.logic.wrappers.EntityWrapper;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javax.persistence.EntityManager;

public abstract class ControllerBase<T extends EntityWrapper> implements Initializable {
  @FXML protected TableView<T> table;
  
  @FXML private Node mainNode;
 
  protected abstract T createNewEntity();
  protected abstract void getItemListForRefresh(EntityManager em, List<T> items);
  
  public void onRefresh() {
    Platform.runLater(() -> {
      TableColumn selectedColumn = null;
      Object selectedEntityId = null;  // null means first line (new item)
      int selectedRow = 0;
      if (table != null) {
        if (table.getSelectionModel().getSelectedCells().size() > 0) {
          TablePosition selectedCell =
              table.getSelectionModel().getSelectedCells().get(0);
          selectedColumn = selectedCell.getTableColumn();
          selectedEntityId = table.getItems().get(selectedCell.getRow()).getId();
          selectedRow = selectedCell.getRow();
        }
        table.getSelectionModel().clearSelection();
      }
      ControllerBase.this.refreshContent();
      if (table != null) {
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
    });
  }
  
  protected void refreshContent() {
    DatabaseSingleton.INSTANCE.query((EntityManager em) -> {
      if (table == null) {
        getItemListForRefresh(em, null);  // TODO(gaborfeher): Eliminate this.
        return true;
      }
      ObservableList<T> items = table.getItems();
      if (items.isEmpty() ||
          items.get(0).getState() != EntityWrapper.State.EDITING_NEW) {
        items.clear();
        // Add the editable empty new element at first position.
        T wrapper = createNewEntity();
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
  
  @Override
  public void initialize(URL url, ResourceBundle rb) {
    mainNode.getProperties().put("controller", this);
    if (table != null) {
      table.getSelectionModel().setCellSelectionEnabled(true);
    }
  }
  
  private void addMyselfAsParent(List<T> items) {
    for (T entity : items) {
      entity.setParent(this);
    }
  }
}
