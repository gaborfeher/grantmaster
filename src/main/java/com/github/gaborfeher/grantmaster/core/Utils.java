package com.github.gaborfeher.grantmaster.core;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.persistence.TypedQuery;
import org.slf4j.LoggerFactory;

public class Utils {
  private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Utils.class);
  
  // Note: the database has a different scale/precision setting for bigdecimals,
  // and possible different rounding rules.
  public static MathContext MC = MathContext.DECIMAL128;
  
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
}
