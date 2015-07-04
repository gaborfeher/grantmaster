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
package com.github.gaborfeher.grantmaster.logic.wrappers;

import com.github.gaborfeher.grantmaster.framework.base.EntityWrapper;
import com.github.gaborfeher.grantmaster.logic.entities.Currency;
import java.util.List;
import javax.persistence.EntityManager;

public class CurrencyWrapper extends EntityWrapper {
  public CurrencyWrapper(Currency currency) {
    super(currency);
  }
    
  public static List<CurrencyWrapper> getCurrencyWrappers(EntityManager em) {
    return em.createQuery(
        "SELECT new com.github.gaborfeher.grantmaster.logic.wrappers.CurrencyWrapper(c) FROM Currency c",
        CurrencyWrapper.class).getResultList();
  }
  
  public static void createDefaultCurrencies(EntityManager em) {
    Currency c;
    c = new Currency(); c.setCode("HUF"); em.persist(c);
    c = new Currency(); c.setCode("USD"); em.persist(c);
    c = new Currency(); c.setCode("EUR"); em.persist(c);
    c = new Currency(); c.setCode("GBP"); em.persist(c);
  }
  
}
