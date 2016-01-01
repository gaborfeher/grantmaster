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
import com.github.gaborfeher.grantmaster.logic.wrappers.ProjectWrapper;
import com.github.gaborfeher.grantmaster.framework.base.RowEditState;
import com.github.gaborfeher.grantmaster.framework.ui.cells.EditButtonTableCell;
import com.github.gaborfeher.grantmaster.logic.entities.Project;
import java.io.IOException;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javax.persistence.EntityManager;

public class ProjectListTabController extends TablePageControllerBase<ProjectWrapper> {
  MainPageController parent;

  @Override
  public void getItemListForRefresh(EntityManager em, List<ProjectWrapper> items) {
    items.addAll(ProjectWrapper.getProjects(em));
  }

  public void handleOpenButtonAction(ActionEvent event) throws IOException {
    Node sourceButton = (Node) event.getSource();
    EditButtonTableCell sourceCell = (EditButtonTableCell) sourceButton.getProperties().get("tableCell");
    ProjectWrapper sourceProjectWrapper = (ProjectWrapper) sourceCell.getEntityWrapper();
    if (sourceProjectWrapper.getState() != RowEditState.SAVED) {
      return;
    }
    parent.addTab(createProjectTab(sourceProjectWrapper.getEntity()));
  }

  private Tab createProjectTab(Project project) throws IOException {
    Tab newTab = new Tab(project.getName());
    newTab.getProperties().put("manager", new DestructiveProjectTabManager(newTab, project));
    return newTab;
  }

  void init(MainPageController parent) {
    this.parent = parent;
  }

  @Override
  protected ProjectWrapper createNewEntity(EntityManager em) {
    return ProjectWrapper.createNew();
  }
}
