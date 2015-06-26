package com.github.gaborfeher.grantmaster.ui.cells;

import com.github.gaborfeher.grantmaster.core.DatabaseSingleton;
import com.github.gaborfeher.grantmaster.core.Utils;
import com.github.gaborfeher.grantmaster.logic.wrappers.EntityWrapper;
import com.github.gaborfeher.grantmaster.ui.framework.RowEditState;
import com.github.gaborfeher.grantmaster.ui.framework.EditableTableRowItem;
import java.util.List;
import java.util.Optional;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.layout.HBox;
import javax.persistence.EntityManager;

/**
 * This table cell should be used in the first column of all the editable
 * tables used in this program. (Tables with EntityWrapper rows, controlled
 * by a subclass of ControllerBase.) This table provides Edit/Create/Delete
 * buttons depending on the state of the row, and optionally a user-defined
 * additional button.
 */
public class EditButtonTableCell<S extends EditableTableRowItem>
    extends TableCell<S, RowEditState> {
  final Button saveButton =
      new Button(Utils.getString("EditCell.CreateEntity"));
  final Button discardButton =
      new Button(Utils.getString("EditCell.DiscardEntity"));
  final Button deleteButton =
      new Button(Utils.getString("EditCell.DeleteEntity"));
  
  final HBox editDeleteBox = new HBox();
  final HBox saveDiscardBox = new HBox(saveButton, discardButton);
  
  public EditButtonTableCell(List<Node> extraButtons) {
    editDeleteBox.getChildren().add(deleteButton);
    editDeleteBox.getChildren().addAll(extraButtons);
    for (Node extraButton : extraButtons) {
      extraButton.getProperties().put("tableCell", this);
    }

    saveButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent t) {
        handleSaveButtonClick();
      }
    });
    discardButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent t) {
        handleDiscardButtonClick();
      }
    });
    deleteButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent t) {
        handleDeleteButtonClick();
      }
    });
  }
  
  public EntityWrapper getEntityWrapper() {
    return (EntityWrapper) getTableRow().getItem();
  }
  
  @Override
  protected void updateItem(RowEditState state, boolean empty) {
    super.updateItem(state, empty);
    EntityWrapper e = getEntityWrapper();
    if (empty || e == null || state == null) {
      setGraphic(null);
      setText(null);
      return;
    }
    switch (state) {
      case EDITING_NEW:
        setGraphic(saveDiscardBox);
        break;
      case SAVED:
        setGraphic(editDeleteBox);
        break;
    }
  }
  
  void handleSaveButtonClick() {
    final EntityWrapper entityWrapper = getEntityWrapper();
    if (entityWrapper.saveNewInstance()) {
      updateItem(entityWrapper.getState(), false);
    }
  }
  
  void handleDiscardButtonClick() {
    EntityWrapper entityWrapper = getEntityWrapper();
    entityWrapper.discardEdits();
  }
  
  void handleDeleteButtonClick() {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle(Utils.getString("EditCell.DeleteConfirmTitle"));
    alert.setContentText(Utils.getString("EditCell.DeleteConfirmQuestion"));
    Optional<ButtonType> result = alert.showAndWait();
    if (result.get() != ButtonType.OK) {
      return;
    }
    EntityWrapper entityWrapper = getEntityWrapper();
    if (DatabaseSingleton.INSTANCE.transaction((EntityManager em) -> {
      entityWrapper.delete(em);
      return true;
    })) {
      entityWrapper.requestTableRefresh();
    } else {
      entityWrapper.getParent().showBackendFailureDialog("delete");
    }
  }
  
}
