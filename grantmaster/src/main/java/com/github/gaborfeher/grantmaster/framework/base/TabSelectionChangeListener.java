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
    TablePageControllerBase controller = getController(tab);
    if (controller != null) {
      controller.onMyTabIsSelected();
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
    activateTab(newTab);
  }
}
