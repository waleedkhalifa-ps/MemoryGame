package com.localpulse.domain;

import static org.junit.Assert.*;

import org.junit.Test;

public class ReportPolicyTest {
  @Test
  public void anotherUserCannotChangeStatus() {
    assertFalse(
        ReportPolicy.allows(
            "other", "author", "reported", "in_progress", "Maintenance visited site"));
  }

  @Test
  public void cannotSkipToResolvedOrVerified() {
    assertFalse(
        ReportPolicy.allows("a", "a", "reported", "resolved", "Repair confirmed with photo"));
    assertFalse(
        ReportPolicy.allows("a", "a", "reported", "confirmed", "A hundred people liked it"));
  }

  @Test
  public void evidenceIsRequired() {
    assertFalse(ReportPolicy.allows("a", "a", "in_progress", "resolved", ""));
    assertFalse(ReportPolicy.allows("a", "a", "in_progress", "resolved", null));
  }

  @Test
  public void authorCanProgressResolveAndReopen() {
    assertTrue(
        ReportPolicy.allows("a", "a", "reported", "in_progress", "Maintenance visited site"));
    assertTrue(
        ReportPolicy.allows("a", "a", "in_progress", "resolved", "Repair confirmed with photo"));
    assertTrue(ReportPolicy.allows("a", "a", "resolved", "reopened", "Lamp stopped working again"));
  }
}
