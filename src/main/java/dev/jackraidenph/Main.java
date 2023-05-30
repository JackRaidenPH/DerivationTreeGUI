package dev.jackraidenph;

import org.jpl7.Query;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

public class Main {

    static Map<String, String> varMap = new HashMap<>();

    public static void main(String[] args) throws IOException {
        Query initQuery = new Query("consult('source.pl')");
        initQuery.hasSolution();
        //REPLACE #find/2 LINE WITH YOUR QUERY!
        Query solutionQuery
                = new Query("""
                leash(-all),
                visible([-all,+exit,+unify,+fail]),
                protocol('trace_op.txt'),
                trace,
                begin,
                notrace,
                noprotocol.""");

        solutionQuery.hasSolution();

        int level = -1;
        Node root = new Node(null, level, "");
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(new FileInputStream("trace_op.txt"), StandardCharsets.UTF_8))) {

            String line;
            while ((line = in.readLine()) != null) {
                int firstOpening = line.indexOf('(');
                int firstClosing = line.indexOf(')');

                String type = line.substring(0, firstOpening).trim();
                String message = line.substring(firstClosing + 1).trim();
                String nodeContents = type + message;

                level = Integer.parseInt(line.substring(firstOpening + 1, firstClosing)) - 3;

                root = root.trace(level, nodeContents);
            }

        }

        for (Node child : root.children) {
            System.out.println(Node.getBranchLength(child));
        }

        try (PrintWriter pw = new PrintWriter("out.txt", StandardCharsets.UTF_8)) {
            pw.println(root);
        }

        //root.optimize();

        new Query("halt.").hasSolution();
    }

    static class Node {
        List<Node> children = new ArrayList<>();
        Node parent;
        int level;
        String contents;

        Node(Node parent, int level, String contents) {
            this.parent = parent;
            this.level = level;
            this.contents = contents;
        }

        void optimize() {
            optimize(this);
        }

        void optimize(Node branchRoot) {
            for (Node child : branchRoot.children) {
                if (getBranchLength(child) != -1) {
                    reduceBranch(child);
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
                }
                reduceBranch(branchRoot, current.children.get(0));
            }
        }

        public Node trace(int level, String contents) {
            if (level <= this.level) {
                return this.parent;
            } else {
                Node n = new Node(this, level, contents);
                children.add(n);
                /*children = children.stream()
                        .filter(node -> !isFailBranch(node))
                        .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);*/
                return n;
            }
        }

        @Override
        public String toString() {
            StringBuilder buffer = new StringBuilder(50);
            print(buffer, "", "");
            return buffer.toString();
        }

        Pattern varPattern = Pattern.compile("_\\d*");

        StringBuilder current = new StringBuilder();

        private void print(StringBuilder buffer, String prefix, String childrenPrefix) {
            buffer.append(prefix);
            contents = contents.replace("Exit:", "Resolved:");
            varPattern.matcher(contents).results().forEach(res ->
            {
                int cycles = varMap.size() / 26;
                int letter = varMap.size() % 26;
                varMap.putIfAbsent(res.group(),
                        String.valueOf((char) ((int) 'A' + letter)) + (cycles > 0 ? cycles : ""));
                current.append(res.group());
            });
            if (!current.isEmpty()) {
                String newContents = contents.replace(current.toString(), varMap.get(current.toString()));
                buffer.append(newContents);
            } else {
                buffer.append(contents);
            }
            current.setLength(0);
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