package example;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import org.junit.Before;
import org.junit.Test;

/**
 * BlackGraphTest is a JUnit test class that tests the functionality of the Graph class,
 * specifically the searchBridgeWord method.
 */
public class BlackGraphTest {
  Graph graph = null;

  /**
   * setUp initializes the Graph object before each test.
   * It attempts to create a Graph from a specified file path.
   */
  @Before
  public void setUp() {
    try {
      graph = new Graph("./test/test.txt");
    } catch (IOException e) {
      e.printStackTrace();
      fail("File not found");
    }
  }

  @Test
  public void testSearchBridgeWordRight1res() {
    ArrayList<String> actualRes = graph.searchBridgeWord("to", "strange");
    ArrayList<String> expectRes = new ArrayList<>();
    expectRes.add("explore");
    assertEquals("test wrong in testSearchBridgeWord_right_1res", expectRes, actualRes);
  }

  @Test
  public void testSearchBridgeWordRight2res() {
    ArrayList<String> actualRes = graph.searchBridgeWord("new", "and");
    ArrayList<String> expectRes = new ArrayList<>();
    expectRes.add("civilizations");
    expectRes.add("life");
    assertTrue("test wrong in testSearchBridgeWord_right_2res",
        new HashSet<>(expectRes).equals(new HashSet<>(actualRes)));
    return;
  }

  @Test
  public void testSearchBridgeWordRight0res() {
    ArrayList<String> actualRes = graph.searchBridgeWord("new", "out");
    ArrayList<String> expectRes = new ArrayList<>();
    assertTrue("test wrong in testSearchBridgeWord_right_0res",
        new HashSet<>(expectRes).equals(new HashSet<>(actualRes)));
    return;
  }

  @Test
  public void testSearchBridgeWordWrongNonExist() {
    ArrayList<String> actualRes = graph.searchBridgeWord("newx", "andw");
    ArrayList<String> expectRes = new ArrayList<>();
    assertTrue("test wrong in testSearchBridgeWord_wrong_NonExist",
        new HashSet<>(expectRes).equals(new HashSet<>(actualRes)));
    return;
  }
}
