package FibanocciHeap;

import static org.dreambot.api.utilities.Logger.log;

import java.util.*;
import java.awt.*;
import java.util.List;

public class Node {
    public Integer priority;
    public Node parent;
    public LinkedList<Integer> rootList;
    private Hashtable<Integer, Node> root;
    private Integer min = -1; //-1 is null?
    public Integer maxDegrees = 12;

    private void instantiateRoot() {
        root = new Hashtable<Integer, Node>();
    }
    private void instantiateRootList() {
        rootList = new LinkedList<>();
    }
    public Hashtable<Integer, Node> getRoot() {
        return root;
    }
    public LinkedList<Integer> getRootList() {
        return rootList;
    }
    public Integer getPriority() {
        return priority;
    }
    public Integer getMin() {
        return min;
    }

    public Main main;
    public Node(Integer priority, Main main) {
        this.main = main; //idk
        this.priority = priority;

        instantiateRoot();
        instantiateRootList();
    }

    public Hashtable<Integer, Node> rootClone;
    public void cleanup() {
        Node[] nodeArray = new Node[maxDegrees+1];

        boolean pass = false; //when done merging, will pass.
        do {
            pass = true;
            rootClone = (Hashtable<Integer, Node>) root.clone();
            nodeArray = new Node[maxDegrees+1];
            for (int l=0; l <= maxDegrees;l++) {
                nodeArray[l] = null;
            }


            for (Node node : rootClone.values()) {
                if (node != null) { //would be null if upcoming node was merged.
                    for (int k=0; k <= maxDegrees;k++) { //0 1 2 3
                        if (node.getRoot().size() == k) {
                            if (nodeArray[k] == null) {
                                nodeArray[k] = node;
                            }else {
                                Node storedNode = nodeArray[k];
                                if (storedNode != null) {
                                    storedNode.merge(node);
                                    nodeArray[k] = null;
                                    pass = false;
                                }else {
                                    nodeArray[k] = node;
                                }
                            }
                        }
                    }
                }
            }
        }
        while (!pass);


        Hashtable<Integer, Node> temp = new Hashtable<>();
        for (int k=0; k <= maxDegrees;k++) {
            Node node = nodeArray[k];
            if (node != null) {
                temp.put(node.priority, node);
            }
        }

        root.clear();
        rootList.clear();

        for (Node node : temp.values()) {
            addChild(node);
        }

        checkForNewMin();
    }

    public void merge(Node node) {
        if (priority < node.priority) { //merge to me.
            main.tree.getRoot().remove(node.priority);
            main.tree.getRootList().remove(node.priority);
            addChild(node);
            checkForNewMin();
        }else { //merge to them.
            main.tree.getRoot().remove(priority);
            main.tree.getRootList().remove(priority);
            node.addChild(this);
            node.checkForNewMin();
        }
        main.tree.checkForNewMin();
    }

    public boolean marked;
    public void cutOut(Node node, Integer... decreasedPriority ) {
        node.parent.root.remove(node.priority); //remove from parent table
        node.parent.rootList.remove(node.priority); //add to parent list
        node.parent.checkForNewMin();
        if (!node.parent.marked) {
            node.parent.marked = true;
        }else {
            node.parent.cutOut(node.parent);
        }

        Integer priorityCheck = decreasedPriority.length > 0 ? decreasedPriority[0] : node.priority;
        node.priority = priorityCheck;
        node.marked = false; //todo good? added node. to it
        main.tree.addChild(node);
    }

    public void addChild(Node child) {
        root.put(child.getPriority(), child);
        rootList.add(child.getPriority());
        checkForNewMin();
        child.setParent(this);
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public void checkForNewMin() {
        for (Integer priority : rootList) {
            if (min == -1 || priority < min) { //update main table min.
                min = priority;
            }
        }
    }








    public void deleteSelf() {
        List<Integer> priorityList = new ArrayList<>();
        for (Node node : root.values()) {
            priorityList.add(node.priority);
        }
        for (Integer priority : priorityList) {
            cutOut(root.get(priority));
        }

        main.tree.getRoot().remove(priority); //todo tree
        main.tree.getRootList().remove(priority); //todo tree
        main.tree.checkForNewMin();

        main.tree.cleanup(); //todo tree
    }

    public void decreasePriority(Integer amountBy) {
        Integer newPriority = priority - amountBy;

        if (newPriority > parent.priority) {
            priority = priority - amountBy;
        }else {
            cutOut(this, newPriority);
        }
    }







    public Hashtable<Integer, Node> clone;
    public void onPaint(Graphics g, int parentX, int parentY, int depth) {
        if (root == null || root.isEmpty()) return;
        clone = (Hashtable<Integer, Node>) root.clone();

        int i=0;
        for (Node node : root.values()) {
            int rootSize = getRoot().size();
            int gap = (180 - (50*depth))/depth;
            int xPos = (12 + parentX - ((rootSize*24) + ((rootSize-1)*gap))/2) + i*(24+gap);
            int yPos = parentY + 30;

            g.setColor(Color.darkGray);
            g.fillRect(xPos, yPos, 24, 15);
            g.setColor(Color.white);
            if (node.getPriority().equals(getMin())) g.setColor(Color.cyan);
            g.drawString("" + node.getPriority(), xPos, yPos+12);

            node.onPaint(g, xPos, yPos, depth+1);
            i++;
        }

    }
}
