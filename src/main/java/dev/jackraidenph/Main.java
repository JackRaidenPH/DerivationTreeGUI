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
                find(['>', '>', '>', ' ', '<', '<', '<'], ['<', '<', '<', ' ', '>', '>', '>']),
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

        try (PrintWriter pw = new PrintWriter("out.txt", StandardCharsets.UTF_8)) {
            pw.println(root.toString());
        }

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

        Node trace(int level, String contents) {
            if (level < this.level)
                return this.parent;
            else {
                Node n = new Node(this, level, contents);
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

        Pattern varPattern = Pattern.compile("_\\d*");

        StringBuilder current = new StringBuilder();
        private void print(StringBuilder buffer, String prefix, String childrenPrefix) {
            buffer.append(prefix);
            varPattern.matcher(contents).results().forEach(res ->
            {
                varMap.putIfAbsent(res.group(), "X" + varMap.size());
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