package example;

import java.io.IOException;
import java.util.Scanner;
import sun.misc.Signal;
import sun.misc.SignalHandler;

/**
 * GraphLab is a command-line application that allows users to interact with a graph structure.
 * */

public class GraphLab {
  /**
   * The main method serves as the entry point for the application.
   * It initializes a graph from a file, sets up signal handling for interrupts,
   * and provides a menu for user interaction with various graph functionalities.
   *
   * @param args Command-line arguments, where the first argument is the path to the graph file.
   * @throws IOException If there is an error reading the graph file.
   */
  public static void main(String[] args) throws IOException {
    if (args.length < 1) {
      System.out.println("Help:\n java GraphLab pathTofile/filename");
      return;
    }
    Graph graph;
    try {
      graph = new Graph(args[0]);
    } catch (IOException e) {
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
          "1. Show Graph in Dot\n"
          + "2. Show Graph as PNG\n"
          + "3. Show Bridge Result\n"
          + "4. Generate New Words\n"
          + "5. Shortest Path\n"
          + "6. Show PageRank\n"
          + "7. Random Move\n"
          + "8. Show Node Info\n"
          + "0. Exit");
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
