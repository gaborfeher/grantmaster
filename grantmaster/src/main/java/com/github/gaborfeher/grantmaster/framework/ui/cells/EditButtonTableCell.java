/*
 * This file is a part of GrantMaster.
 * Copyright (C) 2015  Gábor Fehér <feherga@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.gaborfeher.grantmaster.framework.ui.cells;

import com.github.gaborfeher.grantmaster.framework.utils.DatabaseSingleton;
import com.github.gaborfeher.grantmaster.framework.utils.Utils;
import com.github.gaborfeher.grantmaster.framework.base.EntityWrapper;
import com.github.gaborfeher.grantmaster.framework.base.RowEditState;
import com.github.gaborfeher.grantmaster.framework.base.EditableTableRowItem;
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
