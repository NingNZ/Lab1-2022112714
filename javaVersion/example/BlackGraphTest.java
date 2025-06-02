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
 * BlackGraphTest is a JUnit test class for
 * testing the functionality of the Graph class.
 * */
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
  public void testcalcShortestPath1() {
    System.out.println("testcase 1:");
    ArrayList<String> actualRes = graph.calcShortestPath("civilizations", "to");
    ArrayList<String> expectRes = new ArrayList<>();
    System.out.println("actualRes: " + actualRes);
    System.out.println("expectRes: " +expectRes);
    assertEquals("testcalcShortestPath1", expectRes, actualRes);
  }

  @Test
  public void testcalcShortestPath2() {
    System.out.println("testcase 2:");
    ArrayList<String> actualRes = graph.calcShortestPath("to", "explore");
    ArrayList<String> expectRes = new ArrayList<>();
    expectRes.add("to -> explore");
    System.out.println("actualRes: " + actualRes);
    System.out.println("expectRes: " +expectRes);
    assertTrue("test wrong in testcalcShortestPath2",
        new HashSet<>(expectRes).equals(new HashSet<>(actualRes)));
    return;
  }

  @Test
  public void testcalcShortestPath3() {
    System.out.println("testcase 3:");
    ArrayList<String> actualRes = graph.calcShortestPath("new", "and");
    ArrayList<String> expectRes = new ArrayList<>();
    expectRes.add("new -> worlds -> and");
    expectRes.add("new -> life -> and");
    System.out.println("actualRes: " + actualRes);
    System.out.println("expectRes: " +expectRes);
    assertTrue("test wrong in testcalcShortestPath3",
        new HashSet<>(expectRes).equals(new HashSet<>(actualRes)));
    return;
  }

  @Test
  public void testcalcShortestPath4() {
    System.out.println("testcase 4:");
    ArrayList<String> actualRes = graph.calcShortestPath(null, null);
    ArrayList<String> expectRes = null;
    assertEquals("test wrong in testcalcShortestPath4",
        expectRes, actualRes);
    return;
  }
  
  @Test
  public void testcalcShortestPath5() {
    System.out.println("testcase 5:");
    ArrayList<String> actualRes = graph.calcShortestPath("new", null);
    ArrayList<String> expectRes = null;
    System.out.println("actualRes: " + actualRes);
    System.out.println("expectRes: " +expectRes);
    assertEquals("test wrong in testcalcShortestPath5",
        expectRes, actualRes); 
    return;
  }
  @Test
  public void testcalcShortestPath6() {
    System.out.println("testcase 6:");
    ArrayList<String> actualRes = graph.calcShortestPath("newx", "andx");
    ArrayList<String> expectRes = null;
    System.out.println("actualRes: " + actualRes);
    System.out.println("expectRes: " +expectRes);
    assertEquals("test wrong in testcalcShortestPath6",
        expectRes, actualRes); 
    return;
  }
  @Test
  public void testcalcShortestPath7() {
    System.out.println("testcase 7:");
    ArrayList<String> actualRes = graph.calcShortestPath("new", "andx");
    ArrayList<String> expectRes = null;
    System.out.println("actualRes: " + actualRes);
    System.out.println("expectRes: " +expectRes);
    assertEquals("test wrong in testcalcShortestPath7",
        expectRes, actualRes); 
    return;
  }
}
