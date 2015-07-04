package com.github.gaborfeher.grantmaster.ui;

import javafx.application.HostServices;
import javafx.event.ActionEvent;
import javafx.scene.control.Hyperlink;

public class AboutTabController {
  HostServices hostServices;

  public void setHostServices(HostServices hostServices) {
    this.hostServices = hostServices;
  }

  public void onLinkClick(ActionEvent event) {
    Hyperlink hyperlink = (Hyperlink) event.getSource();
    hostServices.showDocument(hyperlink.getText());
  }
}
