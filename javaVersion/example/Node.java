package example;

import java.util.ArrayList;

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