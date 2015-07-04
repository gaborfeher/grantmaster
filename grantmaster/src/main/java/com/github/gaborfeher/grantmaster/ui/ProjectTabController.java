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
import com.github.gaborfeher.grantmaster.framework.base.TabSelectionChangeListener;
import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.framework.base.EditableTableRowItem;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javax.persistence.EntityManager;

public class ProjectTabController extends TablePageControllerBase {
  @FXML TabPane projectTabs;
  @FXML ProjectExpenseTabController projectExpenseTabController;
  @FXML ProjectSourcesTabController projectSourcesTabController;
  @FXML ProjectBudgetCategoriesTabController projectBudgetCategoriesTabController;
  @FXML ProjectNotesTabController projectNotesTabController;
  @FXML ProjectReportsTabController projectReportsTabController;

  void init(Project project) {
    projectTabs.getSelectionModel().selectedItemProperty().addListener(new TabSelectionChangeListener());
    projectExpenseTabController.init(project);
    projectSourcesTabController.init(project);
    projectBudgetCategoriesTabController.init(project);
    projectNotesTabController.init(project);
    projectReportsTabController.init(project);
  }
  
  @Override
  public void onMyTabIsSelected() {
    Tab tab = projectTabs.getSelectionModel().getSelectedItem();
    TabSelectionChangeListener.activateTab(tab);
  }

  @Override
  protected void getItemListForRefresh(EntityManager em, List items) {
    throw new UnsupportedOperationException("Not supported.");
  }

  @Override
  protected EditableTableRowItem createNewEntity(EntityManager em) {
    throw new UnsupportedOperationException("Not supported.");
  }
  
  @Override
  public void onRefresh() {
    throw new UnsupportedOperationException("Not supported.");
  }
}
