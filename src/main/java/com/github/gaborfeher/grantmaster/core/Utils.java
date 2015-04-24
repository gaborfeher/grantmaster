package com.github.gaborfeher.grantmaster.core;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.List;
import javax.persistence.TypedQuery;

public class Utils {
  // Note: the database has a different scale/precision setting for bigdecimals,
  // and possible different rounding rules.
  public static MathContext MC = MathContext.DECIMAL128;

  public static <T extends Object> T getSingleResultWithDefault(T defaultValue, TypedQuery<T> query) {
    List<T> list = query.getResultList();
    if (list.isEmpty()) {
      return defaultValue;
    }
    if (list.size() > 1) {
      System.err.println("too long list");
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
