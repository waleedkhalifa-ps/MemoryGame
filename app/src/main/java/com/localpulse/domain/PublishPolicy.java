package com.localpulse.domain;

public final class PublishPolicy {
  private PublishPolicy() {}

  public static boolean valid(String title, String body, String locality) {
    return title != null
        && title.trim().length() >= 3
        && title.trim().length() <= 160
        && body != null
        && body.trim().length() >= 10
        && body.length() <= 20000
        && locality != null
        && !locality.trim().isEmpty();
  }

  public static boolean canPublish(String title, String body, String locality, boolean reviewed) {
    return reviewed && valid(title, body, locality);
  }
}
