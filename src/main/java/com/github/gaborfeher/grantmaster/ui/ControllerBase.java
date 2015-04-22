package com.github.gaborfeher.grantmaster.ui;

import com.github.gaborfeher.grantmaster.core.RefreshControlSingleton;
import com.github.gaborfeher.grantmaster.core.Utils;
import com.github.gaborfeher.grantmaster.logic.wrappers.EntityWrapper;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.TableView;

public abstract class ControllerBase<T extends EntityWrapper> implements Initializable {
  @FXML protected TableView<T> table;
  
  @FXML private Node mainNode;
 
  protected abstract T createNewEntity();
  public abstract void refresh();
  
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
}
