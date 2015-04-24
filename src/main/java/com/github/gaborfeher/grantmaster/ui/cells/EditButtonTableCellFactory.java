package com.github.gaborfeher.grantmaster.ui.cells;

import com.github.gaborfeher.grantmaster.logic.wrappers.EntityWrapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

public class EditButtonTableCellFactory<S extends EntityWrapper> implements Callback<TableColumn<S, EntityWrapper.State>, TableCell<S, EntityWrapper.State>> {
  private String extraButtonText;
  private EventHandler<ActionEvent> onAction;
  
  
  @Override
  public TableCell<S, EntityWrapper.State> call(TableColumn<S, EntityWrapper.State> p) {
    List<Node> extraButtons = new ArrayList<>();
    if (extraButtonText != null) {
      Button button = new Button();
      button.setText(extraButtonText);
      button.setOnAction(onAction);
      extraButtons.add(button);
    }
    return new EditButtonTableCell<>(extraButtons);
  }

  /**
   * @return the extraButtonText
   */
  public String getExtraButtonText() {
    return extraButtonText;
  }

  /**
   * @param extraButtonText the extraButtonText to set
   */
  public void setExtraButtonText(String extraButtonText) {
    this.extraButtonText = extraButtonText;
  }

  /**
   * @return the extraButtonAction
   */
  public EventHandler<ActionEvent> getOnAction() {
    return onAction;
  }

  /**
   * @param extraButtonAction the extraButtonAction to set
   */
  public void setOnAction(EventHandler<ActionEvent> extraButtonAction) {
    this.onAction = extraButtonAction;
  }


  
 
  
}
