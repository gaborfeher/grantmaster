package com.github.gaborfeher.grantmaster.ui;

import com.github.gaborfeher.grantmaster.core.DatabaseConnectionSingleton;
import com.github.gaborfeher.grantmaster.core.TransactionRunner;
import com.github.gaborfeher.grantmaster.core.Utils;
import com.github.gaborfeher.grantmaster.logic.wrappers.EntityWrapper;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
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
    System.out.println("refresh");
    System.out.flush();
    DatabaseConnectionSingleton.getInstance().runWithEntityManager(new TransactionRunner() {
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
          wrapper.setState(EntityWrapper.State.EDITING_NEW);
          items.add(wrapper);
        }
        refresh(em, items);
        if (items != null) {
          addMyselfAsParent(items);
        }
        return true;
      }
    });
    System.out.println("end refresh");
    System.out.flush();
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
