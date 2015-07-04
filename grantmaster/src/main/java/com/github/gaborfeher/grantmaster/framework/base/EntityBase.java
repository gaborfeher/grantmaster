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
package com.github.gaborfeher.grantmaster.framework.base;

public abstract class EntityBase {
  public abstract Long getId();

  @Override
  public boolean equals(Object other) {
    if (other == null) {
      return false;
    }
    if (!other.getClass().equals(this.getClass())) {
      return false;
    }
    EntityBase otherEntity = (EntityBase) other;
    if (this.getId() == null || otherEntity.getId() == null) {
      return false;
    }
    return this.getId().equals(otherEntity.getId());
  }

  @Override
  public int hashCode() {
    return getId() == null ? 0 : getId().hashCode();
  }
}
