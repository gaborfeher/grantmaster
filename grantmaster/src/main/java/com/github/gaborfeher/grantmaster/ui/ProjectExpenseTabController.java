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
package com.github.gaborfeher.grantmaster.ui;

import com.github.gaborfeher.grantmaster.framework.base.TablePageControllerBase;
import javafx.fxml.FXML;
import java.util.List;
import javax.persistence.EntityManager;

import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.logic.wrappers.ProjectExpenseWrapper;

public class ProjectExpenseTabController extends TablePageControllerBase<ProjectExpenseWrapper> {
  @FXML ExpenseTableController tableController;
  
  Project project;

  public ProjectExpenseTabController() {
  }
  
  void init(Project project) {
    this.project = project;
    tableController.init(project);
  }
  
  @Override
  public void getItemListForRefresh(EntityManager em, List<ProjectExpenseWrapper> items) {
    items.addAll(ProjectExpenseWrapper.getProjectExpenseList(em, project));
    tableController.refresh();
  }

  @Override
  public ProjectExpenseWrapper createNewEntity(EntityManager em) {
    return ProjectExpenseWrapper.createNew(em, project);
  }

}
