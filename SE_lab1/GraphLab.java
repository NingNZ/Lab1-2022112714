import java.io.*;
import java.util.*;
import sun.misc.Signal;
import sun.misc.SignalHandler;

class Node {
  String name;
  int seq;
  Node pnextHashNode; // 链接散列表
  ArrayList<Pair<Node, Integer>> outNodeList = new ArrayList<>();
  ArrayList<Node> inNodeList = new ArrayList<>();
  double rankValue;
  int inDegree;
  int outDegree;

  Node(String name, int seq) {
    this.name = name;
    this.seq = seq;
  }
}


class Pair<F, S> {
  public F first;
  public S second;

  public Pair(F f, S s) {
    first = f;
    second = s;
  }
}


class Graph {
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

  public boolean createGraphFromTxt(String filePath) {
    try (BufferedReader infile = new BufferedReader(new FileReader(filePath))) {
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
    } catch (IOException e) {
      System.err.println("Unable to open file " + filePath);
      return false;
    }
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
}


class Function {
  static final int Max_iter = 1000;
  static volatile boolean stopTraversal = false;

  public static void geneNewWords(Graph graph) throws IOException {
    System.out.println("Please input a line of words(less than 1023 chars)");
    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    String userInput = reader.readLine();
    String word1 = "";
    String word2;
    if(userInput==null) {
      System.out.println("you don't input in geneNewWords");
      return ;
    }
    StringTokenizer stream = new StringTokenizer(userInput);
    ArrayList<String> bridge;
    while (stream.hasMoreTokens()) {
      word2 = stream.nextToken();
      word2 = Graph.wordsFliter(word2);
      if (word2.isEmpty())
        continue;
      if (!word1.isEmpty()) {
        bridge = graph.searchBridgeWord(word1, word2);
        System.out.print(word1 + " ");
        if (!bridge.isEmpty()) {
          System.out.print(bridge.get(new Random().nextInt(bridge.size())) + " ");
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
    if (pfrom == null && pto != null)
      System.err.println("No \"" + word1 + "\" in the graph!");
    else if (pto == null && pfrom != null)
      System.err.println("No \"" + word2 + "\" in the graph!");
    else if (pto == null && pfrom == null)
      System.err.println("No \"" + word1 + "\" and \"" + word2 + "\" in the graph!");
    else {
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
    String[] words = line.trim().split("\\s+");
    String startNode = words.length > 0 ? words[0] : "";
    String endNode = words.length > 1 ? words[1] : "";

    startNode = Graph.wordsFliter(startNode);
    Node start = graph.findNode(startNode);
    if (start == null) {
      System.err.println("No \"" + startNode + "\" in the graph!");
      return;
    }

    Map<Node, Integer> distances = new HashMap<>();
    Map<Node, ArrayList<Node>> predecessors = new HashMap<>();
    PriorityQueue<Pair<Node, Integer>> pq =
        new PriorityQueue<>(Comparator.comparingInt(a -> a.second));

    for (Node n : graph.nodeSet) {
      distances.put(n, Integer.MAX_VALUE);
    }
    distances.put(start, 0);
    pq.add(new Pair<>(start, 0));

    while (!pq.isEmpty()) {
      Pair<Node, Integer> top = pq.poll();
      Node current = top.first;
      int currentDist = top.second;
      if (currentDist > distances.get(current))
        continue;

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
        System.err.println("No \"" + endNode + "\" in the graph!");
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
    Node pfirst = graph.nodeSet.get(new Random().nextInt(graph.nodeNum));
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
        int randomIndex = new Random().nextInt(pfirst.outNodeList.size());
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


public class GraphLab {
  public static void main(String[] args) throws IOException {
    Graph graph = new Graph();
    if (args.length < 1) {
      System.out.println("Help:\n java GraphLab pathTofile/filename");
      return;
    }
    if (!graph.createGraphFromTxt(args[0])) {
      System.out.println("Exit");
      return;
    }
    Function.cacuPageRank(graph);
    Scanner scanner = new Scanner(System.in);

    Signal.handle(new Signal("INT"), new SignalHandler() {
      @Override
      public void handle(Signal sig) {
        Function.stopTraversal = true;
        System.out.println("\n已中断当前游走,返回ing");
      }
    });
    while (true) {
      System.out.println(
          "1. Show Graph in Dot\n2. Show Graph as PNG\n3. Show Bridge Result\n4. Generate New Words\n5. Shortest Path\n6. Show PageRank\n7. Random Move\n8. Show Node Info\n0. Exit");
      System.out.print("Your choice is:");
      int choice = scanner.nextInt();
      scanner.nextLine();
      if (choice == 0) {
        break;
      } else if (choice == 1) {
        System.out.print("Please input the dot file path:");
        String dotfile = scanner.nextLine();
        if (graph.showGraphInDot(dotfile)) {
          System.out.println("Success");
        } else {
          System.out.println("Error");
        }
      } else if (choice == 2) {
        System.out.print("Please input the dot file path:");
        String dotfile = scanner.nextLine();
        System.out.print("Please input the png file path:");
        String pngfile = scanner.nextLine();
        graph.showGraph(dotfile, pngfile);
        System.out.println("Success");
      } else if (choice == 3) {
        Function.showBridgeResult(graph, scanner);
      } else if (choice == 4) {
        Function.geneNewWords(graph);
      } else if (choice == 5) {
        Function.shortPath(graph);
      } else if (choice == 6) {
        System.out.print("Please input the node you want to search(-all mean all):");
        String node = scanner.nextLine();
        if (node.equals("-all")) {
          for (Node pnode : graph.nodeSet) {
            System.out.println("the PK of node \"" + pnode.name + "\" is " + pnode.rankValue);
          }
        } else {
          Node pnode = graph.findNode(node);
          if (pnode == null) {
            System.out.println("node \"" + node + "\" don't exist");
          } else {
            System.out.println("the PK of node \"" + node + "\" is " + pnode.rankValue);
          }
        }
      } else if (choice == 7) {
        System.out.print("Please input the output file path:");
        String outfile = scanner.nextLine();
        Function.randomMove(graph, outfile);
      } else if (choice == 8) {
        System.out.println(graph.nodeNum + "\t" + graph.edgeNum);
        for (Node n : graph.nodeSet) {
          System.out.println(n.name + " inDegree:" + n.inDegree + " outDegree:" + n.outDegree);
        }
      } else {
        System.err.println("Wrong choice");
      }
    }
    scanner.close();
  }
}
