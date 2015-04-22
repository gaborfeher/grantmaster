package com.github.gaborfeher.grantmaster.ui;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Tab;

public class TabSelectionChangeListener implements ChangeListener<Tab> {
  static void refreshTab(Tab tab) {
    Object controller = tab.getContent().getProperties().get("controller");
    ((ControllerBase) controller).refresh(); 
  }
  
  @Override
  public void changed(ObservableValue<? extends Tab> ov, Tab oldTab, Tab newTab) {
    refreshTab(newTab);
  }
}
