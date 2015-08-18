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
package com.github.gaborfeher.grantmaster.logic.entities;

import com.github.gaborfeher.grantmaster.framework.base.EntityBase;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

@Entity
@Table(
    uniqueConstraints=
        @UniqueConstraint(columnNames={"currencies", "rateDate"}))
public class ExchangeRateItem extends EntityBase implements Serializable {

  @Id
  @GeneratedValue
  private Long id;

  @NotNull(message="%ExchangeRateItem.ValidationErrorUnspecifiedCurrencies")
  @ManyToOne
  @JoinColumn(nullable = false, name = "currencies")
  private CurrencyPair currencies;

  @NotNull(message="%ExchangeRateItem.ValidationErrorRateDateEmpty")
  @Column(nullable = false)
  @Temporal(TemporalType.DATE)
  private LocalDate rateDate;

  @NotNull(message = "%ExchangeRateItem.ValidationErrorRateEmpty")
  @Column(nullable = false, scale = 10, precision = 25)
  private BigDecimal rate;

  @Override
  public Long getId() {
    return id;
  }

  public CurrencyPair getCurrencies() {
    return currencies;
  }

  public void setCurrencies(CurrencyPair currencies) {
    this.currencies = currencies;
  }

  public LocalDate getRateDate() {
    return rateDate;
  }

  public void setRateDate(LocalDate rateDate) {
    this.rateDate = rateDate;
  }

  public BigDecimal getRate() {
    return rate;
  }

  public void setRate(BigDecimal rate) {
    this.rate = rate;
  }
}
