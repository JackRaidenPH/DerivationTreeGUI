package dev.jackraidenph;

import org.jpl7.Query;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

public class Main {

    public static int REPLACEMENT_COUNTER = 0;
    public static final Pattern VARIABLE_PATTERN = Pattern.compile("(_\\d+)");
    private static final int LEVEL_OFFSET = 3;

    public static void main(String[] args) throws IOException {
        Query initQuery = new Query("consult('source.pl')");
        initQuery.hasSolution();

        Query solutionQuery
                = new Query("""
                leash(-all),
                visible([+full]),
                protocol('trace_op.txt'),
                trace,
                %s,
                notrace,
                noprotocol.""".formatted(args[0]));

        solutionQuery.hasSolution();

        int level = -1;
        Node root = new Node(null, level, "");
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(new FileInputStream("trace_op.txt"), StandardCharsets.UTF_8))) {

            String line;
            while ((line = in.readLine()) != null) {
                int firstOpening = line.indexOf('(');
                int firstClosing = line.indexOf(')');

                try {
                    String type = line.substring(0, firstOpening).trim();
                    String message = line.substring(firstClosing + 1).trim();

                    if (VARIABLE_PATTERN.matcher(message).results().findAny().isPresent()) {
                        int cycles = REPLACEMENT_COUNTER / 26;
                        int letter = REPLACEMENT_COUNTER % 26;
                        String postfix = (cycles > 0 ? String.valueOf(cycles) : "");
                        String name = (char) ((int) 'A' + letter) + postfix;
                        message = message.replaceAll(VARIABLE_PATTERN.pattern(), name);
                        REPLACEMENT_COUNTER++;
                    }

                    level = Integer.parseInt(line.substring(firstOpening + 1, firstClosing)) - LEVEL_OFFSET;

                    root = root.trace(level, type, message);
                } catch (Exception ex) {
                    System.err.printf("Line node skipped due to a risen exception!\n\t%s\n", ex.getLocalizedMessage());
                }
            }

        }

        root.optimize();

        try (PrintWriter pw = new PrintWriter("out.txt", StandardCharsets.UTF_8)) {
            pw.println(root);
        }

        new Query("halt.").hasSolution();
    }

    static class Node {
        List<Node> children = new ArrayList<>();
        Node parent;
        int level;
        String type;
        String contents;

        Node(Node parent, int level, String type, String contents) {
            this.parent = parent;
            this.level = level;
            this.type = type;
            this.contents = contents;
        }

        void optimize() {
            optimize(this);
        }

        void optimize(Node branchRoot) {
            /*branchRoot.children = branchRoot.children.stream().filter(n -> !Node.isFailBranch(n))
                    .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);*/
            for (Node child : branchRoot.children) {
                if (getBranchLength(child) != -1) {
                    reduceBranch(child);
                    /*if (isFailBranch(child)) {
                        child.children.clear();
                    }*/
                } else {
                    optimize(child);
                }
            }
        }

        static boolean isFailBranch(Node branchRoot) {
            if (branchRoot.children.size() == 1) {
                return isFailBranch(branchRoot.children.get(0));
            } else if (branchRoot.children.isEmpty()) {
                return branchRoot.contents.toLowerCase(Locale.ROOT).contains("fail");
            }
            return false;
        }

        static int getBranchLength(Node branchRoot) {
            return getBranchLength(branchRoot, 0);
        }

        static int getBranchLength(Node branchRoot, int length) {
            if (branchRoot.children.size() == 1) {
                return getBranchLength(branchRoot.children.get(0), length + 1);
            } else if (branchRoot.children.isEmpty()) {
                return length;
            }
            return -1;
        }

        static void reduceBranch(Node branchRoot) {
            reduceBranch(branchRoot, branchRoot);
        }

        static void reduceBranch(Node branchRoot, Node current) {
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
}