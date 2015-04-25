package com.github.gaborfeher.grantmaster.ui;

import com.github.gaborfeher.grantmaster.core.DatabaseSingleton;
import com.github.gaborfeher.grantmaster.core.TransactionRunner;
import com.github.gaborfeher.grantmaster.logic.wrappers.EntityWrapper;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.TableView;
import javax.persistence.EntityManager;

public abstract class ControllerBase<T extends EntityWrapper> implements Initializable {
  @FXML protected TableView<T> table;
  
  @FXML private Node mainNode;
 
  protected abstract T createNewEntity();
  protected abstract void refresh(EntityManager em, List<T> items);
  
  public void refresh() {
    DatabaseSingleton.INSTANCE.query(new TransactionRunner() {
      @Override
      public boolean run(EntityManager em) {
        if (table == null) {
          refresh(em, null);
          return true;
        }
        table.getSelectionModel().clearSelection();
        List<T> items = table.getItems();
        if (items != null) {
          items.clear();
          T wrapper = createNewEntity();
          if (wrapper != null) {
            wrapper.setState(EntityWrapper.State.EDITING_NEW);
            items.add(wrapper);
          }
        }
        refresh(em, items);
        if (items != null) {
          addMyselfAsParent(items);
        }
        return true;
      }
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
