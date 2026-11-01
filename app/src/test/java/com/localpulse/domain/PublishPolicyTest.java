package com.localpulse.domain;

import static org.junit.Assert.*;

import org.junit.Test;

public class PublishPolicyTest {
  @Test
  public void humanReviewIsRequired() {
    assertFalse(PublishPolicy.canPublish("A new park", "A park opened today.", "nazareth", false));
    assertTrue(PublishPolicy.canPublish("A new park", "A park opened today.", "nazareth", true));
  }

  @Test
  public void incompleteFactsCannotPublish() {
    assertFalse(PublishPolicy.valid("  ", "long enough details", "nazareth"));
    assertFalse(PublishPolicy.valid("Park", "short", "nazareth"));
    assertFalse(PublishPolicy.valid("Park", "long enough details", ""));
    assertFalse(PublishPolicy.valid(null, null, null));
  }

  @Test
  public void contentLimitsAreEnforced() {
    assertFalse(PublishPolicy.valid("x".repeat(161), "long enough details", "nazareth"));
    assertFalse(PublishPolicy.valid("Park", "x".repeat(20001), "nazareth"));
  }
}
