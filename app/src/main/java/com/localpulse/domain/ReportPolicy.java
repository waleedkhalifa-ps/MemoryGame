package com.localpulse.domain;

import java.util.Map;
import java.util.Set;

public final class ReportPolicy {
  private static final Map<String, Set<String>> TRANSITIONS =
      Map.of(
          "reported",
          Set.of("in_progress"),
          "in_progress",
          Set.of("resolved"),
          "resolved",
          Set.of("reopened"),
          "reopened",
          Set.of("in_progress"));

  private ReportPolicy() {}

  public static boolean allows(
      String actor, String author, String from, String to, String evidence) {
    return actor != null
        && actor.equals(author)
        && evidence != null
        && evidence.trim().length() >= 10
        && TRANSITIONS.getOrDefault(from, Set.of()).contains(to);
  }
}
