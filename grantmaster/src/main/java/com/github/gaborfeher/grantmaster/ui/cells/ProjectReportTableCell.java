/**
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
package com.github.gaborfeher.grantmaster.ui.cells;

import com.github.gaborfeher.grantmaster.framework.utils.DatabaseSingleton;
import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectExpense;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectReport;
import com.github.gaborfeher.grantmaster.logic.entities.ProjectSource;
import com.github.gaborfeher.grantmaster.logic.wrappers.ProjectReportWrapper;
import com.github.gaborfeher.grantmaster.framework.ui.cells.BetterChoiceBoxTableCell;
import com.github.gaborfeher.grantmaster.framework.ui.cells.MultiStringConverter;
import com.github.gaborfeher.grantmaster.framework.base.EditableTableRowItem;
import javax.persistence.EntityManager;

class ProjectReportTableCell<S extends EditableTableRowItem>
    extends BetterChoiceBoxTableCell<S, ProjectReport> {
  private static class ProjectReportStringConverter extends MultiStringConverter<ProjectReport> {
    @Override
    public String toString(ProjectReport object) {
      return object.toString();
    }
    @Override
    public ProjectReport fromString(String string) {
      throw new UnsupportedOperationException("Not supported.");
    }
  }
  
  public ProjectReportTableCell(String property) {
    super(new ProjectReportStringConverter(), property, ProjectReport.class);
  }

  @Override
  protected void refreshChoiceItems() {
    DatabaseSingleton.INSTANCE.query((EntityManager em) -> {
      Project project;
      EditableTableRowItem entity = getEntityWrapper();
      if (entity.getEntity() instanceof ProjectSource) {
        project = ((ProjectSource) entity.getEntity()).getProject();
      } else if (entity.getEntity() instanceof ProjectExpense) {
        project = ((ProjectExpense) entity.getEntity()).getProject();
      } else {
        throw new RuntimeException("Not supported yet.");
      }
      getItems().setAll(ProjectReportWrapper.getProjectReportsWithoutWrapping(em, project));
      return true;
    });
  }
}
