package com.github.gaborfeher.grantmaster.ui.cells;

import com.github.gaborfeher.grantmaster.core.DatabaseSingleton;
import com.github.gaborfeher.grantmaster.logic.wrappers.EntityWrapper;
import java.util.List;
import java.util.Optional;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.layout.HBox;
import javax.persistence.EntityManager;

public class EditButtonTableCell<S extends EntityWrapper> extends TableCell<S, EntityWrapper.State> {
  final Button saveButton = new Button("Létrehoz");
  final Button discardButton = new Button("Töröl");
  final Button deleteButton = new Button("Töröl");
  
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
  protected void updateItem(EntityWrapper.State state, boolean empty) {
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
    alert.setTitle("Bejegyzés törlés");
    alert.setHeaderText("Biztos, hogy töröljem?");
    alert.setContentText("A törlés végleges, nem lehet visszavonni.");
    Optional<ButtonType> result = alert.showAndWait();
    if (result.get() != ButtonType.OK) {
      return;
    }
    EntityWrapper entityWrapper = getEntityWrapper();
    if (DatabaseSingleton.INSTANCE.transaction((EntityManager em) -> {
      entityWrapper.delete(em);
      return true;
    })) {
      entityWrapper.refresh();
    } else {
      alert = new Alert(AlertType.ERROR);
      alert.setTitle("Hiba");
      alert.setHeaderText("Nem sikerült a törlés.");
      alert.showAndWait();
    }
  }
  
}
