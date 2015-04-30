package com.github.gaborfeher.grantmaster.ui.cells;

import com.github.gaborfeher.grantmaster.logic.wrappers.EntityWrapper;
import com.github.gaborfeher.grantmaster.ui.framework.RowEditState;
import com.github.gaborfeher.grantmaster.ui.framework.EditableTableRowItem;
import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

/**
 * Factory for EditButtonTableCell.
 */
public class EditButtonTableCellFactory<S extends EditableTableRowItem>
    implements Callback<TableColumn<S, RowEditState>, TableCell<S, RowEditState>> {
  private String extraButtonText;
  private EventHandler<ActionEvent> onAction;
  
  @Override
  public TableCell<S, RowEditState> call(TableColumn<S, RowEditState> p) {
    List<Node> extraButtons = new ArrayList<>();
    if (extraButtonText != null) {
      Button button = new Button();
      button.setText(extraButtonText);
      button.setOnAction(onAction);
      extraButtons.add(button);
    }
    return new EditButtonTableCell<>(extraButtons);
  }

  public String getExtraButtonText() {
    return extraButtonText;
  }

  public void setExtraButtonText(String extraButtonText) {
    this.extraButtonText = extraButtonText;
  }

  public EventHandler<ActionEvent> getOnAction() {
    return onAction;
  }

  public void setOnAction(EventHandler<ActionEvent> extraButtonAction) {
    this.onAction = extraButtonAction;
  }

}
