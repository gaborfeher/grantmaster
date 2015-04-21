package com.github.gaborfeher.grantmaster.ui;

import com.github.gaborfeher.grantmaster.core.RefreshControlSingleton;
import com.github.gaborfeher.grantmaster.core.Utils;
import com.github.gaborfeher.grantmaster.logic.wrappers.EntityWrapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;

public abstract class ControllerBase<T extends EntityWrapper> extends RefreshControlSingleton.MessageObserver {
  @FXML
  protected TableView<T> table;
 
  protected abstract T createNewEntity();
  
  @FXML
  public void addButtonAction(ActionEvent event) {
    T wrapper = createNewEntity();
    wrapper.setState(EntityWrapper.State.EDITING_NEW);
    if (Utils.prepareForEditing()) {
      table.getItems().add(wrapper);
    }
  }
}
