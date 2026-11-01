package com.localpulse;

import static org.junit.Assert.*;

import com.localpulse.data.ComposerViewModel;
import com.localpulse.data.LocalRepository;
import com.localpulse.models.News;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class LocalRepositoryTest {
  @Test
  public void mutationsSurviveRepositoryRecreation() {
    var context = RuntimeEnvironment.getApplication();
    LocalRepository repository = new LocalRepository(context);
    News mine =
        new News(
            "mine",
            "demo-you",
            "Demo",
            "New park",
            "An illustrated fictional news story",
            "community",
            "nazareth");
    repository.put(mine);
    repository.toggle("like:mine");
    repository.toggle("save:mine");
    repository.comment("mine", null, "A useful community update");
    LocalRepository restored = new LocalRepository(context);
    assertEquals(5, restored.all().size());
    assertEquals(1, restored.likeCount(restored.find("mine")));
    assertTrue(restored.flag("save:mine"));
    assertEquals(1, restored.comments("mine").size());
    News edit = LocalRepository.decode(LocalRepository.encode(restored.find("mine")));
    edit.title = "A revised title";
    restored.put(edit);
    assertEquals(5, restored.all().size());
    assertEquals("A revised title", new LocalRepository(context).find("mine").title);
  }

  @Test
  public void likesAreIdempotentAndOwnershipIsRespected() {
    LocalRepository r = new LocalRepository(RuntimeEnvironment.getApplication());
    News seed = r.find("park");
    int original = r.likeCount(seed);
    r.toggle("like:park");
    r.toggle("like:park");
    assertEquals(original, r.likeCount(seed));
    r.delete("park");
    assertNotNull(r.find("park"));
    News other = LocalRepository.decode(LocalRepository.encode(seed));
    other.title = "Unauthorized change";
    assertThrows(SecurityException.class, () -> r.put(other));
    assertNotEquals(other.title, r.find("park").title);
  }

  @Test
  public void draftIsRestoredWithoutImplicitReview() {
    LocalRepository r = new LocalRepository(RuntimeEnvironment.getApplication());
    ComposerViewModel first = new ComposerViewModel();
    first.load(r);
    String id = first.draft.id;
    first.draft.title = "Unpublished draft";
    first.draft.text = "A draft not yet reviewed";
    first.reviewed = true;
    first.persist(r);
    ComposerViewModel second = new ComposerViewModel();
    second.load(new LocalRepository(RuntimeEnvironment.getApplication()));
    assertEquals(id, second.draft.id);
    assertEquals("Unpublished draft", second.draft.title);
    assertFalse(second.reviewed);
    assertNull(r.find(id));
  }

  @Test
  public void ownCommentsCanBeEditedAndDeletedWithoutDuplication() {
    LocalRepository r = new LocalRepository(RuntimeEnvironment.getApplication());
    r.comment("park", null, "Original comment");
    String id = r.comments("park").get(0).optString("id");
    r.comment("park", id, "Edited comment");
    assertEquals(1, r.comments("park").size());
    assertEquals("Edited comment", r.comments("park").get(0).optString("text"));
    r.comment("park", id, null);
    assertTrue(r.comments("park").isEmpty());
  }
}
