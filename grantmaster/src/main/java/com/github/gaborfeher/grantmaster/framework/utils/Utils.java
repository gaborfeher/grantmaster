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
package com.github.gaborfeher.grantmaster.framework.utils;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;
import java.util.ResourceBundle;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javax.persistence.TypedQuery;
import org.slf4j.LoggerFactory;

public class Utils {
  private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Utils.class);

  // Note: the database has a different scale/precision setting for bigdecimals,
  // and possible different rounding rules.
  public static MathContext MC = MathContext.DECIMAL128;

  // Uncomment to enable a button on most of the GUI pages which can add a
  // random item to the list.
  public static RandomHelper testRandom = null;  // new RandomHelper(42);

  public static boolean testMode() {
    return testRandom != null;
  }

  public static String getString(String key) {
    return getResourceBundle().getString(key);
  }

  public static ResourceBundle getResourceBundle() {
    return ResourceBundle.getBundle("bundles.Strings", new Locale("hu"));
  }

  public static <T extends Object> T getSingleResultWithDefault(T defaultValue, TypedQuery<T> query) {
    List<T> list = query.getResultList();
    if (list.isEmpty()) {
      return defaultValue;
    }
    if (list.size() > 1) {
      logger.error("getSingleResultWithDefault(): too many results in list");
    }
    return list.get(0);
  }

  public static BigDecimal addMult(BigDecimal base, BigDecimal add, BigDecimal mult) {
    if (add == null) {
      return base;
    }
    if (base == null) {
      base = BigDecimal.ZERO;
    }
    return base.add(add.multiply(mult, MC), MC);
  }

  public static Optional<ButtonType> showSimpleErrorDialog(
      String title,
      String message,
      List<ButtonType> extraButtons) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle(Utils.getString(title));
    alert.setResizable(true);
    TextArea text = new TextArea(message);
    text.setWrapText(true);
    text.setEditable(false);
    alert.setGraphic(text);
    if (extraButtons != null) {
      alert.getButtonTypes().addAll(extraButtons);
    }
    return alert.showAndWait();
  }

  public static Optional<ButtonType> showListDialog(
      String title,
      String mainMessage,
      List<String> messages,
      List<ButtonType> extraButtons) {
    String fullMessage = Utils.getString(mainMessage) + ":\n";
    for (String message : messages) {
      if (message.length() > 0 && message.charAt(0) == '%') {
        message = Utils.getString(message.substring(1));
      }
      fullMessage += " * " + message + "\n";
    }
    return showSimpleErrorDialog(title, fullMessage, extraButtons);
  }

  public static void logMemoryUsage(String message) {
    final int bytesInMegabyte = 1024*1024;
    Runtime runtime = Runtime.getRuntime();
    logger.info(
        "{} - Memory Usage: total: {}, max: {}, free: {}, used: {}",
        message,
        runtime.totalMemory() / bytesInMegabyte,
        runtime.maxMemory() / bytesInMegabyte,
        runtime.freeMemory() / bytesInMegabyte,
        (runtime.totalMemory() - runtime.freeMemory()) / bytesInMegabyte);
  }

}
