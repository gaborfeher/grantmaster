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

import com.github.gaborfeher.grantmaster.framework.base.RowEditState;
import com.github.gaborfeher.grantmaster.framework.base.EditableTableRowItem;
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
