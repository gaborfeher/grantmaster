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

import com.github.gaborfeher.grantmaster.logic.entities.Project;
import com.github.gaborfeher.grantmaster.logic.wrappers.ProjectExpenseWrapper;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;

public class ExpenseTableController implements Initializable {
  Project project;
  
  @FXML TableColumn<ProjectExpenseWrapper, Object> accountingCurrencyAmountColumn;
  @FXML TableColumn<ProjectExpenseWrapper, Object> grantCurrencyAmountColumn;
  
  @Override
  public void initialize(URL url, ResourceBundle rb) {
  }

  public void init(Project project) {
    this.project = project;
  }

  public void refresh() {
    if (project != null) {
      accountingCurrencyAmountColumn.setText(project.getAccountCurrency().toString());
      grantCurrencyAmountColumn.setText(project.getGrantCurrency().toString());    
    }
  }
 
}
