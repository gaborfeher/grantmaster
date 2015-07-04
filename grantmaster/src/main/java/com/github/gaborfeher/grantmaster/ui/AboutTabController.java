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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import javafx.application.HostServices;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextArea;

public class AboutTabController {
  @FXML
  TextArea license;
  
  HostServices hostServices;

  public void setHostServices(HostServices hostServices) {
    this.hostServices = hostServices;
  }

  public void onLinkClick(ActionEvent event) {
    Hyperlink hyperlink = (Hyperlink) event.getSource();
    hostServices.showDocument(hyperlink.getText());
  }
  
  @FXML
  void initialize() throws IOException, URISyntaxException {
    StringBuilder licenseText = new StringBuilder();
    try (
        InputStream is = getClass().getResourceAsStream("/data/license.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
      while (reader.ready()) {
        licenseText.append(reader.readLine());
        licenseText.append("\n");
      }
    }    
    license.setText(licenseText.toString());
  }
}
