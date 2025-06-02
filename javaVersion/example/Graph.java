package example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Stack;
import java.util.StringTokenizer;

public class Graph {
  static final int Max_Num = 0x3ff;
  Node[] nodeList = new Node[Max_Num + 1];
  boolean[] flag = new boolean[Max_Num + 1];
  int nodeNum = 0;
  int edgeNum = 0;
  ArrayList<Node> nodeSet = new ArrayList<>();

  public Graph() {
    Arrays.fill(nodeList, null);
    Arrays.fill(flag, false);
    nodeSet.clear();
  }

  public Graph(String filePath) throws IOException {
    Arrays.fill(nodeList, null);
    Arrays.fill(flag, false);
    nodeSet.clear();
    this.createGraphFromTxt(filePath);
  }

  private int hash(String name) {
    int index = 0;
    for (char c : name.toCharArray()) {
      index = (index << 2) + c;
      int i = (index & ~Max_Num);
      if (i != 0) {
        index = ((i >> 8) ^ index) & Max_Num;
      }
    }
    return index;
  }

  public Node findNode(String name) {
    int index = hash(name);
    if (!flag[index]) {
      return null;
    }
    Node pn = nodeList[index];
    while (pn != null) {
      if (pn.name.equals(name))
        return pn;
      pn = pn.pnextHashNode;
    }
    return null;
  }

  public Node insertNode(String name) {
    int index = hash(name);
    if (!flag[index]) {
      Node n = new Node(name, nodeNum++);
      nodeList[index] = n;
      flag[index] = true;
      nodeSet.add(n);
      return n;
    }
    Node pn = nodeList[index];
    while (pn.pnextHashNode != null) {
      if (pn.name.equals(name)) {
        System.err.println("The node have existed");
        return pn;
      }
      pn = pn.pnextHashNode;
    }
    if (pn.name.equals(name)) {
      System.err.println("The node have existed");
      return pn;
    }
    Node pnew = new Node(name, nodeNum++);
    pn.pnextHashNode = pnew;
    nodeSet.add(pnew);
    return pnew;
  }

  public boolean insertEdge(String firName, String secName) {
    Node pFir = findNode(firName);
    if (pFir == null)
      pFir = insertNode(firName);
    Node pSec = findNode(secName);
    if (pSec == null)
      pSec = insertNode(secName);

    for (Pair<Node, Integer> pair : pFir.outNodeList) {
      if (pair.first.name.equals(secName)) {
        pair.second++;
        return true;
      }
    }
    pFir.outNodeList.add(new Pair<>(pSec, 1));
    pFir.outDegree++;
    pSec.inNodeList.add(pFir);
    pSec.inDegree++;
    edgeNum++;
    return true;
  }

  public boolean createGraphFromTxt(String filePath) throws IOException {
    BufferedReader infile = new BufferedReader(new FileReader(filePath));
    String word1 = "";
    String word2;
    String line;
    while ((line = infile.readLine()) != null) {
      StringTokenizer st = new StringTokenizer(line);
      while (st.hasMoreTokens()) {
        word2 = st.nextToken();
        word2 = wordsFliter(word2);
        if (!word2.isEmpty()) {
          if (!word1.isEmpty()) {
            if (!insertEdge(word1, word2)) {
              System.err.println("Edge insert error");
            }
          }
          word1 = word2;
        }
      }
    }
    return true;
  }

  public boolean showGraphInDot(String filepath) {
    try (PrintWriter outfile = new PrintWriter(new FileWriter(filepath))) {
      outfile.println("digraph G {");
      for (Node pt : nodeSet) {
        for (Pair<Node, Integer> edge : pt.outNodeList) {
          outfile.printf("\t\"%s\" -> \"%s\" [label=%d];\n", pt.name, edge.first.name, edge.second);
        }
      }
      outfile.println("}");
      return true;
    } catch (IOException e) {
      System.err.println(filepath + " don't open");
      return false;
    }
  }

  public void showGraph(String src, String dst) {
    String command = ".\\bin\\dot.exe -Tpng " + src + " -o " + dst;
    try {
      Runtime.getRuntime().exec(command);
    } catch (IOException e) {
      System.err.println("Failed to run dot.exe");
    }
  }

  public ArrayList<String> searchBridgeWord(String from, String to) {
    from = wordsFliter(from);
    to = wordsFliter(to);
    Node pfrom = findNode(from);
    Node pto = findNode(to);
    ArrayList<String> bridge = new ArrayList<>();
    if (pfrom == null || pto == null)
      return bridge;
    for (Pair<Node, Integer> out : pfrom.outNodeList) {
      for (Node inN : pto.inNodeList) {
        if (out.first == inN) {
          bridge.add(inN.name);
        }
      }
    }
    return bridge;
  }

  public static String wordsFliter(String name) {
    StringBuilder sb = new StringBuilder();
    for (char c : name.toCharArray()) {
      if ('a' <= c && c <= 'z')
        sb.append(c);
      else if ('A' <= c && c <= 'Z')
        sb.append((char) (c - ('A' - 'a')));
    }
    return sb.toString();
  }

  public ArrayList<String> calcShortestPath(String startNode, String endNode) {
    startNode = Graph.wordsFliter(startNode);
    Node start = this.findNode(startNode);
    endNode = Graph.wordsFliter(endNode);
    Node end = this.findNode(endNode);
    if (start == null && end != null) {
      System.err.println("No \"" + startNode + "\" in the graph!");
      return null;
    } else if (start != null && end == null) {
      System.err.println("No \"" + endNode + "\" in the graph!");
      return null;
    } else if (start == null && end == null) {
      System.err.println("No \"" + startNode + "\" and \"" + endNode + "\" in the graph!");
      return null;
    }
    Map<Node, Integer> distances = new HashMap<>();
    PriorityQueue<Pair<Node, Integer>> pq =
        new PriorityQueue<>(Comparator.comparingInt(a -> a.second));

    for (Node n : this.nodeSet) {
      distances.put(n, Integer.MAX_VALUE);
    }
    distances.put(start, 0);
    pq.add(new Pair<>(start, 0));
    Map<Node, ArrayList<Node>> predecessors = new HashMap<>();;
    while (!pq.isEmpty()) {
      Pair<Node, Integer> top = pq.poll();
      Node current = top.first;
      int currentDist = top.second;
      if (currentDist > distances.get(current)) {
        continue;
      }
      
      for (Pair<Node, Integer> edge : current.outNodeList) {
        Node neighbor = edge.first;
        int weight = edge.second;
        int newDist = distances.get(current) + weight;
        if (newDist < distances.get(neighbor)) {
          distances.put(neighbor, newDist);
          ArrayList<Node> predList = new ArrayList<>();
          predList.add(current);
          predecessors.put(neighbor, predList);
          pq.add(new Pair<>(neighbor, newDist));
        } else if (newDist == distances.get(neighbor)) {
          predecessors.get(neighbor).add(current);
        }
      }
    }
    return getAllPaths(start, end, predecessors);
  }

  public ArrayList<String> getAllPaths(Node start, Node target,
      Map<Node, ArrayList<Node>> predecessors) {
    LinkedList<String> currentPath = new LinkedList<>();
    ArrayList<String> allPaths = new ArrayList<>();
    currentPath.add(target.name);
    Stack<Node> stack = new Stack<>();
    stack.push(target);

    while (!stack.isEmpty()) {
      Node current = stack.peek();
      if (current.name.equals(start.name)) {
        ArrayList<String> path = new ArrayList<>(currentPath);
        String thisPath = "";
        Collections.reverse(path);
        thisPath = thisPath.concat(path.get(0));
        for (int i = 1; i < path.size(); ++i) {
          thisPath = thisPath.concat(" -> " + path.get(i));
        }
        allPaths.add(thisPath);
        stack.pop();
        currentPath.removeLast();
        continue;
      }
      ArrayList<Node> preds = predecessors.get(current);
      if (preds != null && !preds.isEmpty()) {
        Node next = preds.remove(preds.size() - 1);
        stack.push(next);
        currentPath.add(next.name);
      } else {
        stack.pop();
        currentPath.removeLast();
      }
    }
    return allPaths;
  }
}
