/*
 * This file is a part of GrantMaster.
 * Copyright (C) 2016 Gabor Feher <feherga@gmail.com>
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

import com.github.gaborfeher.grantmaster.framework.base.TabManager;
import com.github.gaborfeher.grantmaster.framework.utils.Utils;
import com.github.gaborfeher.grantmaster.logic.entities.Project;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import org.slf4j.LoggerFactory;

/**
 * Destroys the content of the tab when it is deactivated and reconstructs
 * it when it gets activated. This is to save heap memory and prevent OOM
 * death. Unfortunately, as a side effect, this disables some nice features
 * of TablePageControllerBase, like remembering the last selected cell.
 */
class DestructiveProjectTabManager implements TabManager {
  private static final org.slf4j.Logger logger = LoggerFactory.getLogger(DestructiveProjectTabManager.class);

  private Tab tab;
  private Project project;

  public DestructiveProjectTabManager(Tab tab, Project project) {
    this.tab = tab;
    this.project = project;
  }

  @Override
  public void onActivate() {
    try {
      final ProjectTabController controller;
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ProjectTab.fxml"));
      loader.setResources(Utils.getResourceBundle());
      Parent projectPage;
      projectPage = loader.load();
      controller = loader.getController();
      controller.init(project);
      tab.setContent(projectPage);
    } catch (IOException ex) {
      logger.error("Cannot activate projact tab {}", project.getName(), ex);
    }
  }

  @Override
  public void onDeactivate() {
    tab.setContent(null);
  }

}
