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
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

@Entity
@Table(
    uniqueConstraints=
        @UniqueConstraint(columnNames={"fromCurrency", "toCurrency"}))
public class CurrencyPair extends EntityBase implements Serializable {
  @Id
  @GeneratedValue
  private Long id;

  @NotNull(message = "%CurrencyPair.ValidationErrorFromCurrency")
  @ManyToOne
  @JoinColumn(nullable = false, name = "fromCurrency")
  private Currency fromCurrency;

  @NotNull(message = "%CurrencyPair.ValidationErrorToCurrency")
  @ManyToOne
  @JoinColumn(nullable = false, name = "toCurrency")
  private Currency toCurrency;

  @Override
  public Long getId() {
    return id;
  }

  public Currency getFromCurrency() {
    return fromCurrency;
  }

  public void setFromCurrency(Currency fromCurrency) {
    this.fromCurrency = fromCurrency;
  }

  public Currency getToCurrency() {
    return toCurrency;
  }

  public void setToCurrency(Currency toCurrency) {
    this.toCurrency = toCurrency;
  }

  @Override
  public String toString() {
    return fromCurrency.toString() + " " + toCurrency.toString();
  }

}
