package PriorityQueue;


import java.awt.*;
import java.util.Hashtable;
import java.util.LinkedList;

import static org.dreambot.api.utilities.Logger.log;

public class Node {
    public Integer min = -1;
    private Integer maxDegrees = 12;
    public Hashtable<Integer, Node> root;

    public final Node tree;
    public Integer priority;
    public Node parent;
    public LinkedList<Integer> children;



    public Node(Integer priority, Node... tree) {
        this.tree = (tree.length>0)? tree[0] : null;
        this.priority = priority;

        log("Tree? " + this.tree);
        if (this.tree == null) { //we must be tree
            instantiateRoot();
        }

        children = new LinkedList<>();
    }

    public Node get(Integer priority) {
        return root.get(priority);
    }

    public int degrees() {
        return children.size();
    }

    private void instantiateRoot() {
        root = new Hashtable<>();
    }

    public void checkForNewMin() {
        if (tree != null) return;

        for (Integer priority : children) {
            if (min == -1 || priority < min) {
                min = priority;
            }
        }
    }

    public void decreasePriority(Integer amountBy) {
        Integer newPriority = priority - amountBy;

        if (newPriority > parent.priority) {
            priority = priority - amountBy;
        }else {
            //cutOut(this, newPriority); //todo cutOut
        }
    }

    public void addChild(Node child) {
        if (tree == null) {
            root.put(child.priority, child);
            children.add(child.priority);
            child.setParent(this);
            checkForNewMin();
        }else {
            tree.root.put(child.priority, child);
            children.add(child.priority);
            child.setParent(this);
        }
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public void onPaint(Graphics g, int parentX, int parentY, int depth) {
        if (tree == null || tree.root == null || tree.root.isEmpty()) return;

        int i=0;
        for (Integer priority : children) {
            int rootSize = degrees();
            int gap = 180/depth - (depth*50);
            int xPos = (parentX+12) - (((rootSize*24) + (((rootSize-1)*gap)))/2) + i*(gap+24); //centerX - groupSize/2 + (this/gapSize * position)
            int yPos = parentY + 30;

            g.setColor(Color.darkGray);
            g.fillRect(xPos, yPos, 24, 15);
            g.setColor(Color.white);
            g.drawString("" + priority, xPos, yPos+12);

            tree.root.get(priority).onPaint(g, xPos, yPos, depth+1);
            i++;
        }
    }

    public Node merge(Node node) {
        if (priority < node.priority) { //merge to me.
            tree.children.remove(node.priority);
            addChild(node);
            tree.checkForNewMin();
            return this;
        }else { //merge to them.
            tree.children.remove(priority);
            node.addChild(this);
            tree.checkForNewMin();
            return node;
        }
    }



    public void tryPlace(Node node) {
        for (int i=0; i<nodeArray.length; i++) {
            if (node.degrees() == i) {
                if (nodeArray[i] == null) {
                    log("empty slot");
                    nodeArray[i] = node;
                    break;
                }else {
                    log("merging");
                    Node winner = node.merge(nodeArray[i]);
                    nodeArray[i] = null;
                    tryPlace(winner);
                    break;
                }
            }
        }
    }

    Node[] nodeArray;
    public void cleanup() {
        nodeArray = new Node[maxDegrees+1];

        for (Integer priority : children) {
            Node node = root.get(priority);
            tryPlace(node);
        }

        log(degrees());
        for (Node node : nodeArray) {
            if (node != null) {
                log("Not null!");
            }
        }

    }
}
