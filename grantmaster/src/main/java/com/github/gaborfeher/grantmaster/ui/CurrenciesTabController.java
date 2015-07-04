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
import com.github.gaborfeher.grantmaster.logic.entities.Currency;
import com.github.gaborfeher.grantmaster.logic.wrappers.CurrencyWrapper;
import com.github.gaborfeher.grantmaster.framework.base.EntityWrapper;
import java.util.List;
import javax.persistence.EntityManager;

public class CurrenciesTabController extends TablePageControllerBase {
  @Override
  protected EntityWrapper createNewEntity(EntityManager em) {
    return new CurrencyWrapper(new Currency());
  }

  @Override
  protected void getItemListForRefresh(EntityManager em, List items) {
    items.addAll(CurrencyWrapper.getCurrencyWrappers(em));
  }
}
