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

package org.apache.servicecomb.tests;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.annotation.Nonnull;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;

import com.seanyinx.github.unit.scaffolding.Poller;

public class EmbeddedAppender extends ConsoleAppender {

  private Queue<String> events;

  private final Poller poller = new Poller(5000, 200);

  public EmbeddedAppender() {
    super(new PatternLayout("%d{ABSOLUTE} [%X{traceId}/%X{spanId}/%X{parentId}] %-5p [%t] %C{2} (%F:%L) - %m%n"));
  }

  @Override
  protected OutputStreamWriter createWriter(OutputStream os) {
    events = new ConcurrentLinkedQueue<>();
    return super.createWriter(new InMemoryOutputStream(events, os));
  }

  public Collection<String> pollLogs(String regex) {
    final Set<String> messages = new LinkedHashSet<>();

    poller.assertEventually(() -> {
      for (String event : events) {
        if (event.trim().matches(regex)) {
          messages.add(event);
        }
      }
      return !messages.isEmpty();
    });

    return messages;
  }

  public void clear() {
    events.clear();
  }

  private static class InMemoryOutputStream extends OutputStream {

    private final Queue<String> events;

    private final OutputStream outputStream;

    InMemoryOutputStream(Queue<String> events, OutputStream outputStream) {
      this.events = events;
      this.outputStream = outputStream;
    }

    @Override
    public void flush() throws IOException {
      outputStream.flush();
    }

    @Override
    public void write(int b) throws IOException {
      outputStream.write(b);
      events.add(String.valueOf(b));
    }

    @Override
    public void write(@Nonnull byte[] b) throws IOException {
      outputStream.write(b);
      events.add(new String(b));
    }

    @Override
    public void write(@Nonnull byte[] b, int off, int len) throws IOException {
      outputStream.write(b, off, len);
      events.add(new String(b, off, len));
    }
  }
}
