package com.github.gaborfeher.grantmaster.ui.cells;

import javafx.util.StringConverter;

/**
 * Provides toString and fromString conversion methods for a given type just
 * like Java FX's StringConverter. In addition to that, a different conversion
 * can be provided for user editing purposes.
 * @author gabor
 */
public abstract class MultiStringConverter<T extends Object>
    extends StringConverter<T> {
  public String toEditableString(T t) {
    return toString(t);
  }
}
