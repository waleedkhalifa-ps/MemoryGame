package com.localpulse.models;

import java.util.ArrayList;
import java.util.List;

/** Firebase-compatible model. IDs, ownership, and timestamps must be enforced on the server. */
public class News {
  public String id = "", authorUid = "demo-you", authorName = "", title = "", text = "";
  public String locationId = "nazareth", categoryId = "community";
  public long createdAt;
  public int likes;
  public boolean illustration = true;
  public List<String> media = new ArrayList<>();

  public News() {}

  public News(
      String id,
      String author,
      String name,
      String title,
      String text,
      String category,
      String town) {
    this.id = id;
    authorUid = author;
    authorName = name;
    this.title = title;
    this.text = text;
    categoryId = category;
    locationId = town;
    createdAt = System.currentTimeMillis();
  }
}
