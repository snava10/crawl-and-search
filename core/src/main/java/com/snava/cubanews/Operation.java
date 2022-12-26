package com.snava.cubanews;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.immutables.value.Value.Default;
import org.immutables.value.Value.Immutable;
import javax.annotation.Nullable;

@Immutable
public interface Operation {

  @Default
  default UUID id() {
    return UUID.randomUUID();
  }

  OperationType type();

  OperationState state();

  @Default
  default long startedAt() {
    return LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
  }

  @Default
  default long finishedAt() {
    return -1;
  }

  @Default
  default long lastUpdated() {
    return LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
  }

  @Default
  default int docsProcessed() {
    return 0;
  }

  @Nullable
  String description();
}
