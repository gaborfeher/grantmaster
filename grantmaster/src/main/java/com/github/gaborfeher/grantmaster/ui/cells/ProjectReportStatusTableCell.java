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
import com.github.gaborfeher.grantmaster.framework.ui.cells.BetterChoiceBoxTableCell;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectReport;

class ProjectReportStatusTableCell<S extends EditableTableRowItem>
    extends BetterChoiceBoxTableCell<S, ProjectReport.Status> {

  public ProjectReportStatusTableCell(String property) {
    super(new ProjectReportStatusStringConverter(), property, ProjectReport.Status.class);
    getItems().add(ProjectReport.Status.OPEN);
    getItems().add(ProjectReport.Status.CLOSED);
  }

  @Override
  protected void refreshChoiceItems() {
  }

}
