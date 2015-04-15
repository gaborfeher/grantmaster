package com.github.gaborfeher.grantmaster.ui.cells;

import com.github.gaborfeher.grantmaster.logic.wrappers.EntityWrapper;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.layout.HBox;

public class EditButtonTableCell<S extends EntityWrapper> extends TableCell<S, EntityWrapper.State> {
  final Button saveButton = new Button("Save");
  final Button editButton = new Button("Edit");
  final Button discardButton = new Button("Discard");
  final Button deleteButton = new Button("Delete");
  
  final HBox editDeleteBox = new HBox(editButton, deleteButton);
  final HBox saveDiscardBox = new HBox(saveButton, discardButton);
  
  public EditButtonTableCell() {

    saveButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent t) {
        handleSaveButtonClick();
      }
    });
    editButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent t) {
        handleEditButtonClick();
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
  
  private EntityWrapper getEntityWrapper() {
    return (EntityWrapper) getTableRow().getItem();
  }
  
  @Override
  protected void updateItem(EntityWrapper.State state, boolean empty) {
    super.updateItem(state, empty);
    if (!empty && state != null && getEntityWrapper() != null) {
      switch (state) {
        case EDITING:
        case EDITING_NEW:
          setGraphic(saveDiscardBox);
          break;
        case SAVED:
          setGraphic(editDeleteBox);
          break;
      }
    } else {
      setGraphic(null);
    }
  }
  
  void handleSaveButtonClick() {
    EntityWrapper entityWrapper = getEntityWrapper();
    entityWrapper.persist();
    entityWrapper.setState(EntityWrapper.State.SAVED);
    updateItem(entityWrapper.getState(), false);
  }
  
  void handleEditButtonClick() {
    EntityWrapper entityWrapper = getEntityWrapper();
    entityWrapper.setState(EntityWrapper.State.EDITING);
    updateItem(entityWrapper.getState(), false);
  }
  
  void handleDiscardButtonClick() {
    EntityWrapper entityWrapper = getEntityWrapper();
    entityWrapper.discardEdits();
  }
  
  void handleDeleteButtonClick() {
    EntityWrapper entityWrapper = getEntityWrapper();
    entityWrapper.delete();
  }
  
}
