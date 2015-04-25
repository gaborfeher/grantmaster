package com.github.gaborfeher.grantmaster.ui;

import com.github.gaborfeher.grantmaster.core.DatabaseSingleton;
import com.github.gaborfeher.grantmaster.logic.wrappers.EntityWrapper;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
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
      TablePosition selectedCell = null;
      if (table != null) {
        if (table.getSelectionModel().getSelectedCells().size() > 0) {
          selectedCell = table.getSelectionModel().getSelectedCells().get(0);
        }
        table.getSelectionModel().clearSelection();
      }
      ControllerBase.this.refreshContent();
      if (table != null) {
        if (selectedCell != null && selectedCell.getRow() < table.getItems().size()) {
          table.getSelectionModel().clearAndSelect(
              selectedCell.getRow(), selectedCell.getTableColumn());
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
      // table.getSelectionModel().clearSelection();
      List<T> items = table.getItems();
      if (items != null) {
        items.clear();
        T wrapper = createNewEntity();
        if (wrapper != null) {
          wrapper.setState(EntityWrapper.State.EDITING_NEW);
          items.add(wrapper);
        }
      }
      getItemListForRefresh(em, items);
      if (items != null) {
        addMyselfAsParent(items);
      }
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
