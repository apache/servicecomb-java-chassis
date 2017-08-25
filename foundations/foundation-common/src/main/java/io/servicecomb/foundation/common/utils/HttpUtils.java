/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.foundation.common.utils;

import java.net.HttpCookie;
import java.util.List;

public final class HttpUtils {
  private HttpUtils() {
  }

  public static String getCookieParamValue(String cookieName, List<String> cookieLines) {
    for (String cookieLine : cookieLines) {
      String value = getCookieParamValue(cookieName, cookieLine);
      if (value != null) {
        return value;
      }
    }

    return null;
  }

  public static String getCookieParamValue(String cookieName, String cookieLine) {
    List<HttpCookie> httpCookieList = HttpCookie.parse(cookieLine);
    for (HttpCookie httpCookie : httpCookieList) {
      if (cookieName.equals(httpCookie.getName())) {
        return httpCookie.getValue();
      }
    }
    return null;
  }
}
