package example;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.concurrent.ThreadLocalRandom;


class Function {
  static final int Max_iter = 1000;
  static volatile boolean stopTraversal = false;
  static final Random random = new Random();

  public static void geneNewWords(Graph graph) throws IOException {
    System.out.println("Please input a line of words(less than 1023 chars)");
    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    String userInput = reader.readLine();
    String word1 = "";
    String word2;
    if (userInput == null) {
      System.out.println("you don't input in geneNewWords");
      return;
    }
    StringTokenizer stream = new StringTokenizer(userInput);
    ArrayList<String> bridge;
    while (stream.hasMoreTokens()) {
      word2 = stream.nextToken();
      word2 = Graph.wordsFliter(word2);
      if (word2.isEmpty()) {
        continue;
      }
      if (!word1.isEmpty()) {
        bridge = graph.searchBridgeWord(word1, word2);
        System.out.print(word1 + " ");
        if (!bridge.isEmpty()) {
          System.out.print(bridge.get(random.nextInt(bridge.size())) + " ");
        }
      }
      word1 = word2;
    }
    System.out.println(word1 + " ");
  }

  public static void showBridgeResult(Graph graph, Scanner scanner) throws IOException {
    System.out.println("Please input the word1 word2");
    String word1 = scanner.next();
    String word2 = scanner.next();
    word1 = Graph.wordsFliter(word1);
    word2 = Graph.wordsFliter(word2);
    Node pfrom = graph.findNode(word1);
    Node pto = graph.findNode(word2);
    if (pfrom == null && pto != null) {
      System.out.println("No \"" + word1 + "\" in the graph!");
    } else if (pto == null && pfrom != null) {
      System.out.println("No \"" + word2 + "\" in the graph!");
    } else if (pto == null && pfrom == null) {
      System.out.println("No \"" + word1 + "\" and \"" + word2 + "\" in the graph!");
    } else {
      ArrayList<String> bridge = graph.searchBridgeWord(word1, word2);
      if (bridge.isEmpty()) {
        System.out.println("No bridge words from \"" + word1 + "\" to \"" + word2 + "\"");
      } else {
        System.out.print("The bridge words from \"" + word1 + "\" to \"" + word2 + "\" are: \""
            + bridge.get(0) + "\"");
        for (int i = 1; i < bridge.size(); i++) {
          System.out.print(",\"" + bridge.get(i) + "\"");
        }
        System.out.println();
      }
    }
  }

  public static void shortPath(Graph graph) throws IOException {
    System.out.println("Please input the start node (and optionally the end node):");
    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    String line = reader.readLine();
    if (line == null || line.trim().isEmpty()) {
      System.out.println("You didn't input anything, please try again.");
      return;
    }
    String[] words = line.trim().split("\\s+");
    String startNode = words.length > 0 ? words[0] : "";
    

    startNode = Graph.wordsFliter(startNode);
    Node start = graph.findNode(startNode);
    if (start == null) {
      System.out.println("No \"" + startNode + "\" in the graph!");
      return;
    }

    Map<Node, Integer> distances = new HashMap<>();
    
    PriorityQueue<Pair<Node, Integer>> pq =
        new PriorityQueue<>(Comparator.comparingInt(a -> a.second));

    for (Node n : graph.nodeSet) {
      distances.put(n, Integer.MAX_VALUE);
    }
    distances.put(start, 0);
    pq.add(new Pair<>(start, 0));
    Map<Node, ArrayList<Node>> predecessors = new HashMap<>();
    String endNode = words.length > 1 ? words[1] : "";
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

    if (endNode.isEmpty()) {
      System.out.println("Shortest paths from \"" + startNode + "\":");
      for (Node target : graph.nodeSet) {
        if (target == start)
          continue;
        if (distances.get(target) == Integer.MAX_VALUE) {
          System.out.println("\nNo path to \"" + target.name + "\".");
        } else {
          System.out.println(
              "\nTo \"" + target.name + "\": Distance = " + distances.get(target) + ", Paths:");
          Map<Node, ArrayList<Node>> predecessorsCopy = new HashMap<>();
          for (Map.Entry<Node, ArrayList<Node>> entry : predecessors.entrySet()) {
            // 对每个 ArrayList<Node> 也 new 一个新的副本
            predecessorsCopy.put(entry.getKey(), new ArrayList<>(entry.getValue()));
          }
          printAllPaths(graph, start, target, predecessorsCopy);
        }
      }
    } else {
      endNode = Graph.wordsFliter(endNode);
      Node end = graph.findNode(endNode);
      if (end == null) {
        System.out.println("No \"" + endNode + "\" in the graph!");
        return;
      }
      if (distances.get(end) == Integer.MAX_VALUE) {
        System.out.println("No path from \"" + startNode + "\" to \"" + endNode + "\".");
      } else {
        System.out.println("Shortest paths from \"" + startNode + "\" to \"" + endNode
            + "\": Distance = " + distances.get(end) + ", Paths:");
        printAllPaths(graph, start, end, predecessors);
      }
    }
  }

  public static void printAllPaths(Graph graph, Node start, Node target,
      Map<Node, ArrayList<Node>> predecessors) {
    LinkedList<String> currentPath = new LinkedList<>();
    currentPath.add(target.name);
    Stack<Node> stack = new Stack<>();
    stack.push(target);

    while (!stack.isEmpty()) {
      Node current = stack.peek();
      if (current.name.equals(start.name)) {
        List<String> path = new ArrayList<>(currentPath);
        Collections.reverse(path);
        System.out.print(path.get(0));
        for (int i = 1; i < path.size(); ++i) {
          System.out.print(" -> " + path.get(i));
        }
        System.out.println();
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
  }

  public static void cacuPageRank(Graph graph) {
    double d = 0.85;
    for (Node pnode : graph.nodeSet) {
      pnode.rankValue = 1.0 / graph.nodeNum;
    }
    double max_dif = 1;
    int iter = 0;
    while (max_dif >= 1e-7 && iter <= Max_iter) {
      double deadNodePRSum = 0;
      max_dif = 0;
      double[] oldPR = new double[graph.nodeSet.size()];
      for (int i = 0; i < graph.nodeSet.size(); i++) {
        Node pnow = graph.nodeSet.get(i);
        oldPR[i] = pnow.rankValue;
        if (pnow.outDegree == 0) {
          deadNodePRSum += pnow.rankValue;
        }
      }
      double fix = deadNodePRSum / graph.nodeNum;
      for (int i = 0; i < graph.nodeSet.size(); i++) {
        double newPr = 0;
        Node pnow = graph.nodeSet.get(i);
        for (Node inNode : pnow.inNodeList) {
          newPr += oldPR[inNode.seq] / inNode.outDegree;
        }
        newPr += fix;
        pnow.rankValue = d * newPr + (1 - d) * 1.0 / graph.nodeNum;
      }
      for (int i = 0; i < graph.nodeSet.size(); i++) {
        Node pnow = graph.nodeSet.get(i);
        max_dif = Math.max(max_dif, Math.abs(oldPR[i] - pnow.rankValue));
      }
      iter++;
    }
  }

  public static void randomMove(Graph graph, String filepath) throws IOException {
    stopTraversal = false;
    Node pfirst = graph.nodeSet.get(random.nextInt(graph.nodeNum));
    BufferedWriter output = new BufferedWriter(new FileWriter(filepath));
    int nowlen = 0;
    int maxlen = 2048;
    StringBuilder buffer = new StringBuilder();
    boolean is_end = false;
    Set<String> visited = new HashSet<>();
    while (true) {
      if (nowlen + pfirst.name.length() + 1 <= maxlen) {
        buffer.append(pfirst.name).append(" ");
        System.out.print(pfirst.name + " ");
        nowlen += (pfirst.name.length() + 1);
      } else {
        output.write(buffer.toString());
        output.flush();
        buffer.setLength(0);
        nowlen = 0;
        continue;
      }
      if (pfirst.outDegree == 0) {
        System.out.println("\nmeet a node outDegree = 0");
        is_end = true;
      } else {
        int randomIndex = random.nextInt(pfirst.outNodeList.size());
        Pair<Node, Integer> edge = pfirst.outNodeList.get(randomIndex);
        String edgeKey = pfirst.name + "->" + edge.first.name;
        if (!visited.contains(edgeKey)) {
          visited.add(edgeKey);
          pfirst = edge.first;
        } else {
          System.out.println("\nmeet the same edge again");
          is_end = true;
        }
      }
      try {
        Thread.sleep(200);
      } catch (InterruptedException e) {
        // ignore
      }
      if (is_end || stopTraversal) {
        System.out.println("Preparing for Exit");
        if (nowlen > 0) {
          output.write(buffer.toString());
        }
        output.close();
        return;
      }
    }

  }
}
