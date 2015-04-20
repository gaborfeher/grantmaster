package com.github.gaborfeher.grantmaster.ui.cells;

public class DefaultMultiStringConverter extends MultiStringConverter<String> {

  @Override
  public String toString(String t) {
    return t;
  }

  @Override
  public String fromString(String string) {
    return string;
  }
  
}
