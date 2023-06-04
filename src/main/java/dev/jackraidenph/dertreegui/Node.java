package dev.jackraidenph.dertreegui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class Node {
    List<Node> children = new ArrayList<>();
    Node parent;
    int level;
    String type;
    String contents;

    public Node(Node parent, int level, String type, String contents) {
        this.parent = parent;
        this.level = level;
        this.type = type;
        this.contents = contents;
    }

    public void optimize() {
        optimize(this);
    }

    private void optimize(Node branchRoot) {
        branchRoot.children =
                branchRoot.children.stream().filter(n -> !Node.isFailBranch(n))
                        .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        for (Node child : branchRoot.children) {
            reduceBranch(child);
            optimize(child);
        }
    }

    private static boolean isFailBranch(Node branchRoot) {
        if (branchRoot.children.size() == 1) {
            return isFailBranch(branchRoot.children.get(0));
        } else if (branchRoot.children.isEmpty()) {
            return branchRoot.contents.toLowerCase(Locale.ROOT).contains("fail");
        }
        return false;
    }

    public static void reduceBranch(Node branchRoot) {
        reduceBranch(branchRoot, branchRoot);
    }

    private static void reduceBranch(Node branchRoot, Node current) {
        if (!current.children.isEmpty()) {
            if (branchRoot != current) {
                current.contents = "...";
                branchRoot.children.set(0, current);
            }
            reduceBranch(branchRoot, current.children.get(0));
        }
    }

    public Node trace(int level, String type, String contents) {
        if (level <= this.level) {
            return this.parent;
        } else {
            Node n = new Node(this, level, type, contents);
            children.add(n);
            return n;
        }
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder(50);
        print(buffer, "", "");
        return buffer.toString();
    }

    private void print(StringBuilder buffer, String prefix, String childrenPrefix) {
        buffer.append(prefix);
        contents = contents.replaceAll("\\^", " ");
        contents = contents.replaceAll("\\[\\d;\\d+m", "");
        contents = contents.replaceAll("\\[0m", "");
        contents = contents.replaceAll(" \u001B", "");
        contents = contents.trim();

        buffer.append(contents);

        buffer.append('\n');
        for (Iterator<Node> it = children.iterator(); it.hasNext(); ) {
            Node next = it.next();
            if (it.hasNext()) {
                next.print(buffer, childrenPrefix + "├── ", childrenPrefix + "│   ");
            } else {
                next.print(buffer, childrenPrefix + "└── ", childrenPrefix + "    ");
            }
        }
    }
}
