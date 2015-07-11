/*
 * This file is a part of GrantMaster.
 * Copyright (C) 2015 Gabor Feher <feherga@gmail.com>
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
package com.github.gaborfeher.grantmaster.ui.cells;

import com.github.gaborfeher.grantmaster.framework.base.EditableTableRowItem;
import com.github.gaborfeher.grantmaster.framework.ui.cells.PropertyTableCellFactoryBase;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectReport;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

public class ProjectReportStatusTableCellFactory <S extends EditableTableRowItem>
    extends PropertyTableCellFactoryBase
    implements Callback<TableColumn<S, ProjectReport.Status>, TableCell<S, ProjectReport.Status>> {

  @Override
  public TableCell<S, ProjectReport.Status> call(TableColumn<S, ProjectReport.Status> p) {
    return new ProjectReportStatusTableCell(property);
  }
  
}
