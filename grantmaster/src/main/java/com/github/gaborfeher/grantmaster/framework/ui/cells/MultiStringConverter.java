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

import javafx.util.StringConverter;

/**
 * Provides toString and fromString conversion methods for a given type just
 * like Java FX's StringConverter. In addition to that, a different conversion
 * can be provided for user editing purposes.
 */
public abstract class MultiStringConverter<T extends Object>
    extends StringConverter<T> {
  public String toEditableString(T t) {
    return toString(t);
  }
  
  public String getParseError() {
    return null;
  }
}
