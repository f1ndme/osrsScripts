package PriorityQueue;

import org.dreambot.api.Client;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;

import java.awt.*;
import java.util.*;
import java.util.List;

@ScriptManifest(category = Category.UTILITY, name = "Fibanocci Queue", description = "It prioritizes stuff.", author = "find me", version = 1.0)
public class Main extends AbstractScript {
    public static List<Integer> generateRandomNumbers(int size) {
        List<Integer> randomNumbers = new ArrayList<>();

        // Populate the list with numbers from 1 to 100
        for (int i = 5; i <= 104; i++) {
            randomNumbers.add(i);
        }

        // Shuffle the list to get a random order
        Collections.shuffle(randomNumbers);

        // Trim the list to the specified size if needed
        if (size < randomNumbers.size()) {
            randomNumbers = randomNumbers.subList(0, size);
        }

        return randomNumbers;
    }



    Node tree;
    List<Integer> randomNumbers;
    List<Integer> randomNumbers2;
    Node n1;
    Node n2;

    public void onStart() {
        tree = new Node(0);

        //Node t = new Node(1, tree);
        //tree.addChild(t);

        //log(tree.get(t.priority) == null); //root check
        log(tree.degrees()); //tree degrees

        randomNumbers = generateRandomNumbers(100);
        randomNumbers2 = generateRandomNumbers(10000);

        n1 = new Node(1, tree);
        n2 = new Node(2, tree);
        tree.addChild(n1);
        tree.addChild(n2);
        Node n4 = new Node(3, tree);
        Node n5 = new Node(4, tree);
        Node n6 = new Node(5, tree);
        Node n7 = new Node(6, tree);
        tree.addChild(n4);
        tree.addChild(n5);
        tree.addChild(n6);
        tree.addChild(n7);
    }


    long nextReset;
    long newNodeReset;
    boolean firstLoop;
    int next;
    public int onLoop() {
        if (next >= 99) return 100;

        if (!firstLoop) {
            firstLoop = true;
            nextReset = System.currentTimeMillis() + 1000;
            return 100;
        }

        if (newNodeReset < System.currentTimeMillis()) {
            newNodeReset = System.currentTimeMillis() + 300;
            Node randomNode = getRandomValue(tree.root);

            if (randomNode != null) {
                Integer prio = randomNumbers2.get(next);
                if (randomNode.priority < prio) {
                    Node n = new Node(prio, tree);
                    randomNode.addChild(n);
                }
            }
            next++;
        }


        if (nextReset < System.currentTimeMillis()) {
            nextReset = System.currentTimeMillis() + 3000;

            tree.cleanup();
        }

        return 100;
    }

    public static <K, V> V getRandomValue(Hashtable<K, V> hashtable) {
        if (hashtable == null || hashtable.isEmpty()) {
            throw new IllegalStateException("Hashtable is empty");
        }

        // Get a random index from the keys set
        Object[] keysArray = hashtable.keySet().toArray();
        Random random = new Random();
        K randomKey = (K) keysArray[random.nextInt(keysArray.length)];

        // Retrieve the corresponding value
        return hashtable.get(randomKey);
    }



    int originX = Client.getViewportWidth()/2;
    int originY = Client.getViewportHeight()/4;
    public void onPaint(Graphics g) {
        if (tree == null || tree.root == null || tree.root.isEmpty()) return;

        int i=0;
        for (Integer priority : tree.children) {
            int rootSize = tree.degrees();
            int gap = 400;
            int xPos = originX - (((rootSize*24) + (((rootSize-1)*gap)))/2) + i*(gap+24); //centerX - groupSize/2 + (this/gapSize * position)
            int yPos = originY;

            g.setColor(Color.darkGray);
            g.fillRect(xPos, yPos, 24, 15);
            g.setColor(Color.white);
            if (priority.equals(tree.min)) g.setColor(Color.cyan);
            g.drawString("" + priority, xPos, yPos+12);

            tree.root.get(priority).onPaint(g, xPos, yPos, 1);
            i++;
        }
    }

}
