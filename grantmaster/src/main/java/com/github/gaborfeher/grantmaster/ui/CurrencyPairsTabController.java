package com.github.gaborfeher.grantmaster.ui;

/*
 * This file is a part of GrantMaster.
 * Copyright (C) 2015 Gabor Feher <feherga@gmail.com>
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

import com.github.gaborfeher.grantmaster.framework.base.RowEditState;
import com.github.gaborfeher.grantmaster.framework.base.TablePageControllerBase;
import com.github.gaborfeher.grantmaster.framework.ui.cells.EditButtonTableCell;
import com.github.gaborfeher.grantmaster.framework.utils.Utils;
import com.github.gaborfeher.grantmaster.logic.entities.CurrencyPair;
import com.github.gaborfeher.grantmaster.logic.wrappers.CurrencyPairWrapper;
import java.io.IOException;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javax.persistence.EntityManager;

/**
 * FXML Controller class
 *
 * @author gabor
 */
public class CurrencyPairsTabController extends TablePageControllerBase<CurrencyPairWrapper> {
  MainPageController parent;

  public void handleOpenButtonAction(ActionEvent event) throws IOException {
    Node sourceButton = (Node) event.getSource();
    EditButtonTableCell sourceCell = (EditButtonTableCell) sourceButton.getProperties().get("tableCell");
    CurrencyPairWrapper currenciesWrapper = (CurrencyPairWrapper) sourceCell.getEntityWrapper();
    if (currenciesWrapper.getState() != RowEditState.SAVED) {
      return;
    }
    parent.addTab(createExchageRateTab(currenciesWrapper.getEntity()));
  }

  private Tab createExchageRateTab(CurrencyPair currencyPair) throws IOException {
    Tab newTab = new Tab(currencyPair.toString());
    final ExchangeRateItemsTabController controller;
    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ExchangeRateItems.fxml"));
    loader.setResources(Utils.getResourceBundle());
    Parent projectPage = loader.load();
    controller = loader.getController();
    controller.init(currencyPair);
    newTab.setContent(projectPage);
    return newTab;
  }

  @Override
  protected CurrencyPairWrapper createNewEntity(EntityManager em) {
    return new CurrencyPairWrapper(new CurrencyPair());
  }

  @Override
  protected void getItemListForRefresh(EntityManager em, List<CurrencyPairWrapper> items) {
    items.addAll(CurrencyPairWrapper.getAllExhangeRatePairs(em));
  }

  public void init(MainPageController parent) {
    this.parent = parent;
  }

}
