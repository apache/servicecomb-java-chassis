/*
 * Copyright 2014 Red Hat, Inc.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 *
 */

/*
 * Forked from https://github.com/vert-x3/vertx-web/blob/927ed057ddc028eb09a168db621de3d72fd85ed4/vertx-web/src/main/java/io/vertx/ext/web/impl/Utils.java
 * Because we uses getSortedAcceptableMimeTypes method which is removed by vertx.
 */

package io.vertx.ext.web.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

public class MimeTypesUtils {
  private static final Pattern COMMA_SPLITTER = Pattern.compile(" *, *");

  private static final Pattern SEMICOLON_SPLITTER = Pattern.compile(" *; *");

  private static final Pattern EQUAL_SPLITTER = Pattern.compile(" *= *");

  private static final Comparator<String> ACCEPT_X_COMPARATOR = new Comparator<String>() {
    float getQuality(String s) {
      if (s == null) {
        return 0;
      }

      String[] params = SEMICOLON_SPLITTER.split(s);
      for (int i = 1; i < params.length; i++) {
        String[] q = EQUAL_SPLITTER.split(params[1]);
        if ("q".equals(q[0])) {
          return Float.parseFloat(q[1]);
        }
      }
      return 1;
    }

    @Override
    public int compare(String o1, String o2) {
      float f1 = getQuality(o1);
      float f2 = getQuality(o2);
      return Float.compare(f2, f1);
    }
  };

  public static List<String> getSortedAcceptableMimeTypes(String acceptHeader) {
    // accept anything when accept is not present       
    if (acceptHeader == null) {
      return Collections.emptyList();
    }

    // parse      
    String[] items = COMMA_SPLITTER.split(acceptHeader);
    // sort on quality        
    Arrays.sort(items, ACCEPT_X_COMPARATOR);

    List<String> list = new ArrayList<>(items.length);

    for (String item : items) {
      // find any ; e.g.: "application/json;q=0.8"        
      int space = item.indexOf(';');

      if (space != -1) {
        list.add(item.substring(0, space));
      } else {
        list.add(item);
      }
    }

    return list;
  }
}
