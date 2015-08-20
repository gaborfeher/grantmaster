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
package com.github.gaborfeher.grantmaster.logic.wrappers;

import com.github.gaborfeher.grantmaster.framework.base.EntityWrapper;
import com.github.gaborfeher.grantmaster.framework.utils.Utils;
import com.github.gaborfeher.grantmaster.logic.entities.CurrencyPair;
import com.github.gaborfeher.grantmaster.logic.entities.ExchangeRateItem;
import com.github.gaborfeher.grantmaster.logic.entities.Project;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import javax.persistence.EntityManager;

/**
 *
 * @author gabor
 */
public class ExchangeRateItemWrapper extends EntityWrapper<ExchangeRateItem> {

  public ExchangeRateItemWrapper(ExchangeRateItem entity) {
    super(entity);
  }

  public static List<ExchangeRateItemWrapper> getAllForCurrencyPair(
      EntityManager em,
      CurrencyPair currencyPair) {
    return em.createQuery("SELECT new com.github.gaborfeher.grantmaster.logic.wrappers.ExchangeRateItemWrapper(e) " +
            "FROM ExchangeRateItem e " +
            "WHERE e.currencies = :currencies " +
            "ORDER BY e.rateDate DESC",
        ExchangeRateItemWrapper.class).
            setParameter("currencies", currencyPair).
           getResultList();
  }

  public static BigDecimal getExchangeRate(
      EntityManager em,
      Project project,
      LocalDate date) {
    ExchangeRateItem exchangeRateItem = Utils.getSingleResultWithDefault(
        null,
        em.createQuery(
            "SELECT e " +
            "FROM ExchangeRateItem e " +
            "WHERE e.currencies.fromCurrency = :fromCurrency " +
            "AND e.currencies.toCurrency = :toCurrency " +
            "AND e.rateDate = :date",
            ExchangeRateItem.class).
            setParameter("fromCurrency", project.getAccountCurrency()).
            setParameter("toCurrency", project.getGrantCurrency()).
            setParameter("date", date));
    if (exchangeRateItem == null) {
      return null;
    } else {
      return exchangeRateItem.getRate();
    }
  }
}
