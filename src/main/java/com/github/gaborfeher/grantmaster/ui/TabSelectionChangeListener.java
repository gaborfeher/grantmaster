package com.github.gaborfeher.grantmaster.ui;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Tab;

/**
 * Listens for the user switching tabs in a tab pane and sends a refresh
 * message to the selected tab.
 */
public class TabSelectionChangeListener implements ChangeListener<Tab> {
  static void activateTab(Tab tab) {
    Object controller = tab.getContent().getProperties().get("controller");
    ((ControllerBase) controller).onMyTabIsSelected(); 
  }
  
  @Override
  public void changed(ObservableValue<? extends Tab> ov, Tab oldTab, Tab newTab) {
    activateTab(newTab);
  }
}
