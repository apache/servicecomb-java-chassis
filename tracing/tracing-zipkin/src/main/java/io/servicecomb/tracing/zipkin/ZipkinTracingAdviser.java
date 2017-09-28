package io.servicecomb.tracing.zipkin;

import java.util.function.Supplier;

import brave.Span;
import brave.Tracer;
import brave.Tracer.SpanInScope;

public class ZipkinTracingAdviser {

  private final Tracer tracer;

  public ZipkinTracingAdviser(Tracer tracer) {
    this.tracer = tracer;
  }

  public <T> T invoke(String spanName, Supplier<T> supplier) {
    Span span = createSpan(spanName);
    try (SpanInScope spanInScope = tracer.withSpanInScope(span)) {
      return supplier.get();
    } finally {
      span.finish();
    }
  }

  private Span createSpan(String spanName) {
    Span currentSpan = tracer.currentSpan();
    if (currentSpan != null) {
      return tracer.newChild(currentSpan.context()).name(spanName).start();
    }

    return tracer.newTrace().name(spanName).start();
  }
}
