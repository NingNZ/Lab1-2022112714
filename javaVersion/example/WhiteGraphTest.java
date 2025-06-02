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
 * WhiteGraphTest is a JUnit test class that tests the functionality of the Graph class,
 * specifically the searchBridgeWord method.
 */
public class WhiteGraphTest {
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
  public void testSearchBridgeWord1() {
    System.out.println("White testcase 1:");
    ArrayList<String> actualRes = graph.searchBridgeWord("toa", "strange");
    ArrayList<String> expectRes = new ArrayList<>();
    System.out.println("actualRes: " + actualRes);
    System.out.println("expectRes: " + expectRes);
    assertEquals("test wrong in testSearchBridgeWord1", expectRes, actualRes);
  }

  @Test
  public void testSearchBridgeWord2() {
    System.out.println("White testcase 2:");
    ArrayList<String> actualRes = graph.searchBridgeWord("new", "andb");
    ArrayList<String> expectRes = new ArrayList<>();
    System.out.println("actualRes: " + actualRes);
    System.out.println("expectRes: " + expectRes);
    assertTrue("test wrong in testSearchBridgeWord2",
        new HashSet<>(expectRes).equals(new HashSet<>(actualRes)));
    return;
  }
  @Test
  public void testSearchBridgeWord3() {
    System.out.println("White testcase 3:");
    ArrayList<String> actualRes = graph.searchBridgeWord("civilizations", "and");
    ArrayList<String> expectRes = new ArrayList<>();
    System.out.println("actualRes: " + actualRes);
    System.out.println("expectRes: " + expectRes);
    assertTrue("test wrong in testSearchBridgeWord3",
        new HashSet<>(expectRes).equals(new HashSet<>(actualRes)));
    return;
  }
  @Test
  public void testSearchBridgeWord4() {
    System.out.println("White testcase 4:");
    ArrayList<String> actualRes = graph.searchBridgeWord("new", "and");
    ArrayList<String> expectRes = new ArrayList<>();
    expectRes.add("worlds");
    expectRes.add("life");
    System.out.println("actualRes: " + actualRes);
    System.out.println("expectRes: " + expectRes);
    assertTrue("test wrong in testSearchBridgeWord4",
        new HashSet<>(expectRes).equals(new HashSet<>(actualRes)));
    return;
  }
}
