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

package org.apache.servicecomb.common.rest;

import org.apache.commons.lang3.SystemUtils;

public final class RestConst {
  private RestConst() {
  }

  public static final String REST_CLIENT_REQUEST_PATH = "rest-client-request-path";

  public static final String SWAGGER_REST_OPERATION = "swaggerRestOperation";

  public static final String REST = "rest";

  public static final String SCHEME = "cse";

  public static final String SCHEME_NEW = "servicecomb";

  public static final String URI_PREFIX = SCHEME + "://";

  public static final String URI_PREFIX_NEW = SCHEME_NEW + "://";

  // in HttpServletRequest attribute
  public static final String PATH_PARAMETERS = "servicecomb-paths";

  // in HttpServletRequest attribute
  public static final String BODY_PARAMETER = "servicecomb-body";

  //in invocation response
  public static final String INVOCATION_HANDLER_RESPONSE = "servicecomb-invocation-hanlder-response";

  //in invocation response
  public static final String INVOCATION_HANDLER_PROCESSOR = "servicecomb-invocation-hanlder-processor";

  //in invocation response
  public static final String INVOCATION_HANDLER_REQUESTCLIENT = "servicecomb-invocation-hanlder-requestclient";

  public static final String REST_PRODUCER_INVOCATION = "servicecomb-rest-producer-invocation";

  public static final String REST_INVOCATION_CONTEXT = "servicecomb-rest-invocation-context";

  public static final String REST_REQUEST = "servicecomb-rest-request";

  public static final String CONSUMER_HEADER = "servicecomb-rest-consumer-header";

  public static final String READ_STREAM_PART = "servicecomb-readStreamPart";

  public static final String UPLOAD_DIR = "servicecomb.uploads.directory";

  public static final String UPLOAD_DEFAULT_DIR = SystemUtils.JAVA_IO_TMPDIR;

  // limit of one upload file, only available for servlet rest transport
  public static final String UPLOAD_MAX_FILE_SIZE = "servicecomb.uploads.maxFileSize";

  // limit of upload request body
  public static final String UPLOAD_MAX_SIZE = "servicecomb.uploads.maxSize";

  // the size threshold after which files will be written to disk
  // only available for servlet rest transport
  public static final String UPLOAD_FILE_SIZE_THRESHOLD = "servicecomb.uploads.fileSizeThreshold";

  public static final String PROVIDER_SCAN_REST_CONTROLLER = "servicecomb.provider.rest.scanRestController";

  public static final String PRINT_CODEC_ERROR_MESSGAGE = "servicecomb.codec.printErrorMessage";

  public static final String HEADER_CONTEXT_MAPPER = "servicecomb.context.headerContextMapper";

  public static final String QUERY_CONTEXT_MAPPER = "servicecomb.context.queryContextMapper";
}
