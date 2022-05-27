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

package org.apache.servicecomb.demo.springmvc.server;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestSchema(schemaId = "DateTimeSchema")
@RequestMapping(path = "/dateTime", produces = MediaType.APPLICATION_JSON)
public class DateTimeSchema {
  @GetMapping(path = "/getDate")
  public Date getDate(@RequestParam("date") Date date) {
    return date;
  }

  @GetMapping(path = "/getDatePath/{date}")
  public Date getDatePath(@PathParam("date") Date date) {
    return date;
  }

  @PostMapping(path = "/postDate")
  public Date postDate(@RequestBody Date date) {
    return date;
  }

  @GetMapping(path = "/getLocalDate")
  public LocalDate getLocalDate(@RequestParam("date") LocalDate date) {
    return date;
  }

  @GetMapping(path = "/getLocalDate/{date}")
  public LocalDate getLocalDatePath(@PathParam("date") LocalDate date) {
    return date;
  }

  @PostMapping(path = "/postLocalDate")
  public LocalDate postLocalDate(@RequestBody LocalDate date) {
    return date;
  }

  @GetMapping(path = "/getLocalDateTime")
  public LocalDateTime getLocalDateTime(@RequestParam("date") LocalDateTime date) {
    return date;
  }

  @GetMapping(path = "/getLocalDateTime/{date}")
  public LocalDateTime getLocalDateTimePath(@PathParam("date") LocalDateTime date) {
    return date;
  }

  @PostMapping(path = "/postLocalDateTime")
  public LocalDateTime postLocalDateTime(@RequestBody LocalDateTime date) {
    return date;
  }
}
