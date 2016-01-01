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
package com.github.gaborfeher.grantmaster.framework.base;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Tab;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listens for the user switching tabs in a tab pane and sends a refresh
 * message to the selected tab. This is installed for both the global tab
 * pane and the project-level tab panes.
 */
public class TabSelectionChangeListener implements ChangeListener<Tab> {
  private static final Logger logger = LoggerFactory.getLogger(TabSelectionChangeListener.class);
  private static Tab activeTab = null;

  private static TablePageControllerBase getController(Tab tab) {
    Object controller = tab.getContent().getProperties().get("controller");
    if (controller != null && controller instanceof TablePageControllerBase) {
      return ((TablePageControllerBase) controller);
    }
    return null;
  }

  public static void activateTab(Tab tab) {
    logger.info("activate tab {}", tab.getText());
    activeTab = tab;
    if (tab.getProperties().containsKey("manager")) {
      ((TabManager) tab.getProperties().get("manager")).onActivate();
    }
    TablePageControllerBase controller = getController(tab);
    if (controller != null) {
      controller.onMyTabIsSelected();
    }
  }

  public static void deactivateTab(Tab tab) {
    logger.info("deactivate tab {}", tab.getText());
    activeTab = tab;
    if (tab.getProperties().containsKey("manager")) {
      ((TabManager) tab.getProperties().get("manager")).onDeactivate();
    }
  }

  public static Tab getActiveTab() {
    return activeTab;
  }

  public static TablePageControllerBase getActiveTabController() {
    if (activeTab != null) {
      return getController(activeTab);
    }
    return null;
  }

  @Override
  public void changed(ObservableValue<? extends Tab> ov, Tab oldTab, Tab newTab) {
    if (oldTab != null) {
      deactivateTab(oldTab);
    }
    activateTab(newTab);
  }
}
