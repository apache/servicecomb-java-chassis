/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.servicecomb.common.rest.codec.query;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryCodecs {
  private static final Logger LOGGER = LoggerFactory.getLogger(QueryCodecs.class);

  static QueryCodecs createForTest() {
    return new QueryCodecs(Arrays.asList(
        new QueryCodecMulti(),
        new QueryCodecCsv(),
        new QueryCodecSsv(),
        new QueryCodecPipes()
    ));
  }

  private final Map<String, QueryCodec> codecs = new HashMap<>();

  private final QueryCodec defaultCodec;

  public QueryCodecs(List<QueryCodec> orderedCodecs) {
    orderedCodecs.forEach(this::register);
    defaultCodec = codecs.get(QueryCodecMulti.CODEC_NAME);
  }

  private void register(QueryCodec codec) {
    QueryCodec exists = codecs.put(codec.getCodecName(), codec);
    if (exists != null) {
      LOGGER.info("override QueryCodec, exists={}, new={}.",
          exists.getClass().getName(), codec.getClass().getName());
    }
  }

  public QueryCodec find(String name) {
    if (name == null) {
      return defaultCodec;
    }

    QueryCodec codec = codecs.get(name);
    if (codec == null) {
      throw new IllegalStateException("not support QueryCodec, name=" + name);
    }
    return codec;
  }
}
