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

import com.github.gaborfeher.grantmaster.framework.base.EditableTableRowItem;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class TextFieldTableCell<S extends EditableTableRowItem, T>
    extends TableCell<S, T> {
  final String property;
  final TextField editTextField;
  final MultiStringConverter<T> stringConverter;
  final Class<T> valueType;

  // true if the last editing session was cancelled by user (currently esc key)
  // This disables later spurious commit messages.
  boolean userCancelled;

  public TextFieldTableCell(
      String property,
      MultiStringConverter<T> stringConverter0,
      Class<T> valueType) {
    this.property = property;
    this.stringConverter = stringConverter0;
    this.valueType = valueType;
    editTextField = new TextField();
    editTextField.setOnKeyPressed(new EventHandler<KeyEvent>() {
        @Override
        public void handle(KeyEvent event) {
          if (event.getCode().equals(KeyCode.ENTER)) {
            parseAndCommit(editTextField.getText());
          } else if (event.getCode().equals(KeyCode.ESCAPE)) {
            userCancelled = true;
            cancelEdit();
          }
        }
    });
    editTextField.focusedProperty().addListener(
        new ChangeListener<Boolean>() {
          @Override
          public void changed(ObservableValue<? extends Boolean> ov, Boolean t1, Boolean t2) {
            if (!t2 && t1) {
              parseAndCommit(editTextField.getText());
            }
          }
        }
    );
  }

  private EditableTableRowItem getEntityWrapper() {
    return (EditableTableRowItem) getTableRow().getItem();
  }

  private void parseAndCommit(String val) {
    if (userCancelled) {
      return;  // User cancel was before this commit message, ignore this.
    }
    if (val == null) {
      return;
    }
    T parsed = stringConverter.fromString(val);
    if (parsed != null) {
      commitEdit(parsed);
    } else {
      userCancelled = true;
      cancelEdit();
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setHeaderText(stringConverter.getParseError());
      alert.showAndWait();
    }
  }

  @Override
  public void commitEdit(T val) {
    if (userCancelled) {
      return;  // User cancel was before this commit message, ignore this.
    }
    if (getEntityWrapper().commitEdit(property, val, valueType)) {
      super.commitEdit(val);
      updateItem(val, false);
    } else {
      userCancelled = true;
      cancelEdit();
    }
  }

  @Override
  public void startEdit() {
    userCancelled = false;
    if (getEntityWrapper() != null && getEntityWrapper().canEdit()) {
      super.startEdit();
      updateItem(getItem(), false);
      editTextField.requestFocus();
    }
  }

  @Override
  public void cancelEdit() {
    super.cancelEdit();
    updateItem(getItem(), false);
  }

  @Override
  public void updateItem(final T item, final boolean empty) {
    super.updateItem(item, empty);
    if (empty) {
      setText(null);
      setGraphic(null);
    } else {
      if (isEditing()) {
        editTextField.setText(stringConverter.toEditableString(item));
        setGraphic(editTextField);
        setText(null);
      } else {
        setGraphic(null);
        setText(stringConverter.toString(item));
      }
    }
  }
}
