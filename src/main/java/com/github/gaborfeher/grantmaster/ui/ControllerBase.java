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
  protected abstract void refresh(EntityManager em);
  
  public void refresh() {
    DatabaseConnectionSingleton.getInstance().runWithEntityManager(new TransactionRunner() {
      @Override
      public boolean run(EntityManager em) {
        ControllerBase.this.refresh(em);
        addMyselfAsParentToTable();
        return true;
      }
    });
  }
  
  @Override
  public void initialize(URL url, ResourceBundle rb) {
    mainNode.getProperties().put("controller", this);
  }
  
  @FXML
  public void addButtonAction(ActionEvent event) {
    T wrapper = createNewEntity();
    wrapper.setState(EntityWrapper.State.EDITING_NEW);
    wrapper.setParent(this);
    if (Utils.prepareForEditing()) {
      table.getItems().add(wrapper);
    }
  }
  
  protected void addMyselfAsParentToTable() {
    if (table != null) {
      for (T entity : table.getItems()) {
        entity.setParent(this);
      }
    }
  }
}
