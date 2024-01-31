package FibanocciHeap;

import org.dreambot.api.Client;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;

import java.awt.*;
import java.util.Hashtable;

@ScriptManifest(category = Category.UTILITY, name = "Fibanocci Heap", description = "It heaps stuff.", author = "find me", version = 1.0)
public class Main extends AbstractScript {
    public Node tree;
    @Override
    public void onStart() {
        testStuff();
    }

    public void testStuff() {
        tree = new Node(0, this);
    }


    int originX = Client.getViewportWidth()/2;
    int originY = Client.getViewportHeight()/4;
    public Hashtable<Integer, Node> clone;
    @Override
    public void onPaint(Graphics g) {
        if (tree.getRoot() == null || tree.getRoot().isEmpty()) return;
        clone = (Hashtable<Integer, Node>) tree.getRoot().clone();


        int i=0;
        for (Node node : clone.values()) {
            int rootSize = tree.getRoot().size();
            int gap = 360;
            int xPos = originX - (((rootSize*24) + (((rootSize-1)*gap)))/2) + i*(gap+24); //centerX - groupSize/2 + (this/gapSize * position)
            int yPos = originY;

            g.setColor(Color.darkGray);
            g.fillRect(xPos, yPos, 24, 15);
            g.setColor(Color.white);
            if (node.priority.equals(tree.getMin())) g.setColor(Color.cyan);
            g.drawString("" + node.priority, xPos, yPos+12);

            node.onPaint(g, xPos, yPos, 1);
            i++;
        }
    }




    public Integer test = 1;
    long nextReset;
    long nextDelete;
    boolean firstLoop;
    @Override
    public int onLoop() {
        Node strayNode = new Node(test, this);
        tree.addChild(strayNode);

        if (!firstLoop) {
            firstLoop = true;
            nextReset = System.currentTimeMillis() + 2000;
        }
        if (nextDelete < System.currentTimeMillis()) {
            nextDelete = System.currentTimeMillis() + 8022;
            if (Calculations.random(1, 10) == 5) {
                if (test != 1) {
                    if (strayNode.priority != strayNode.parent.getMin()) {
                        strayNode.deleteSelf();
                        tree.cleanup();
                    }
                }
            }
        }
        if (nextReset < System.currentTimeMillis()) {
            nextReset = System.currentTimeMillis() + 4051;
            tree.cleanup();
        }

        test++;
        return 50;
    }
}














/*        //Node n1 = new Node(1, this);
        Node n2 = new Node(2, this);
        Node n3 = new Node(3, this);
        //Node n4 = new Node(4, this);
        //Node n5 = new Node(5, this);
        Node n6 = new Node(6, this);
        Node n7 = new Node(7, this);
        Node n8 = new Node(8, this);
        Node n9 = new Node(9, this);
        Node n10 = new Node(10, this);
        Node n11 = new Node(11, this);
        Node n12 = new Node(12, this);
        Node n13 = new Node(13, this);
        Node n14 = new Node(14, this);
        Node n15 = new Node(15, this);


        n6.addChild(n10);
        n6.addChild(n12);
        n6.addChild(n13);
        n6.addChild(n14);
        n6.addChild(n15);

        n2.addChild(n9);
        n2.addChild(n8);
        n2.addChild(n6);
        //n5.addChild(n4);

        //insertPriority(n1);
        insertPriority(n2);
        insertPriority(n3);
        //insertPriority(n4);
        //insertPriority(n5);
        //insertPriority(n6);
        //insertPriority(n7);
        //insertPriority(n8);
        //insertPriority(n9);
        //insertPriority(n10);
        insertPriority(n11);
        //insertPriority(n12);

        //n6.decreasePriority(5);
        //n10.decreasePriority(5);
        //n12.decreasePriority(8);


        Node n20 = new Node(20, this);
        Node n21 = new Node(21, this);
        Node n22 = new Node(22, this);
        Node n23 = new Node(23, this);
        Node n24 = new Node(24, this);
        Node n25 = new Node(25, this);
        insertPriority(n20);
        insertPriority(n21);
        insertPriority(n22);
        insertPriority(n23);
        //insertPriority(n24);
        //insertPriority(n25);



        n6.deleteSelf();



        log("Min: " + getMin());*/