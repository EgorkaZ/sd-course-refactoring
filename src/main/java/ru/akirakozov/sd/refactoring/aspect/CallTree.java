package ru.akirakozov.sd.refactoring.aspect;

import java.util.ArrayList;
import java.util.List;

public class CallTree {
    private final String callString;
    private long startTime = 0;
    private long endTime = 0;

    private final CallTree parent;
    private final List<CallTree> children = new ArrayList<>();

    private CallTree(String callString, long startTime, CallTree parent) {
        this.callString = callString;
        this.startTime = startTime;
        this.parent = parent;
    }

    public static CallTreeView startCallTree(String method) {
        return new CallTree(method, System.nanoTime(), null).createView();
    }

    private CallTreeView createView() {
        return new CallTreeView(this);
    }

    public class CallTreeView {
        private CallTree tree;

        private CallTreeView(CallTree tree) {
            this.tree = tree;
        }

        public void enterCall(String method) {
            CallTree newCall = new CallTree(method, System.nanoTime(), tree);
            tree.children.add(newCall);
            tree = newCall;
        }

        public void exitCall() {
            tree.endTime = System.nanoTime();
            tree = tree.parent;
        }

        public boolean isRootCall() {
            return tree.parent == null;
        }

        public void dumpTree() {
            if (tree.endTime == 0) {
                tree.endTime = System.nanoTime();
            }
            dumpTree(tree, 0);
        }

        private void dumpTree(CallTree tree, int depth) {
            System.out.printf("[call-tree] %s%s %dns\n", "-".repeat(depth), tree.callString,
                    tree.endTime - tree.startTime);
            for (CallTree child : tree.children) {
                dumpTree(child, depth + 1);
            }
        }
    }
}
