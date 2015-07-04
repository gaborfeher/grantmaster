/*
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
package com.github.gaborfeher.grantmaster.framework.ui.cells;

import com.github.gaborfeher.grantmaster.framework.utils.Utils;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParsePosition;

class BigDecimalStringConverter extends MultiStringConverter<BigDecimal> {
  final DecimalFormat displayFormatter;
  final DecimalFormat editFormatter;
  
  public BigDecimalStringConverter() {
    displayFormatter = new DecimalFormat("#,##0.00");
    editFormatter = new DecimalFormat("#0.00");
    editFormatter.setParseBigDecimal(true);
  }

  @Override
  public String toString(BigDecimal t) {
    if (t == null) {
      return "";
    }
    return displayFormatter.format(t);
  }

  @Override
  public BigDecimal fromString(String string) {
    if (string == null) {
      return null;
    }
    ParsePosition pos = new ParsePosition(0);
    BigDecimal value = (BigDecimal) editFormatter.parse(string, pos);
    if (pos.getIndex() == string.length()) {
      return value;
    } else {
      return null;
    }
  }
  
  @Override
  public String toEditableString(BigDecimal t) {
    if (t == null) {
      return null;
    }
    return editFormatter.format(t);
  }

  @Override
  public String getParseError() {
    return Utils.getString("Edit.UnknownNumberFormat");
  }
}
