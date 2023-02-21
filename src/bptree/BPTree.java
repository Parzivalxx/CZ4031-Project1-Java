package bptree;

import main.Main;
import memorypool.RecordBlock;

import java.sql.Array;
import java.util.logging.Logger;
import java.util.ArrayList;
public class BPTree {
    private Node root;
    private int capacity;
    private int numNodes;
    private int numLevels;
    private int numAccessedNodes;
    static Logger logger = Logger.getLogger(Main.class.getName());

    public BPTree(int capacity) {
        this.root = null;
        this.capacity = capacity;
        this.numNodes = 0;
        this.numLevels = 0;
        this.numAccessedNodes = 0;
    }

    /**
     * print root contents
     */
    public String getRootContent() {
        if (root == null) {
            System.err.println("Root is null...");
        }
        String s = "Root elements: ";
        for (int i = 0; i < root.getElements().size(); i++) {
            s += root.getElements().get(i) + " ";
        }
        return s;
    }

    /**
     * inserts recordBlock into bptree
     * @param key, key to insert
     * @param recordBlock, the record and block which it is in
     */
    public void insertKey(int key, RecordBlock recordBlock) {
        // first time insert
        if (root == null) {
            root = new LeafNode();
            LeafNode node = (LeafNode) root;
            node.getElements().add(0, key);
            node.getRecordBlocks().add(0, new ArrayList());
            numNodes += 1;
            numLevels += 1;
            return;
        }
        // second time and onwards insert
        KeyNodePair newPair = recursiveInsert(root, key, recordBlock);

        // by the time it reaches this point and is not null, means root is overloaded
        // first child lowest key not in elements
        if (newPair != null) {
            NonLeafNode newRoot = new NonLeafNode();
            newRoot.getChildren().add(0, root);
            newRoot.getElements().add(0, newPair.getKey());
            newRoot.getChildren().add(1, newPair.getNode());

            root.setParent(newRoot);
            newPair.getNode().setParent(newRoot);

            root = newRoot;
            numNodes += 1;
            numLevels += 1;
        }
        return;
    }

    /**
     * gets the new node when inserting new key into tree
     * @param node, the current node
     * @param key, the key we want to insert
     * @param recordBlock, holding the record and block we want to store
     * @return KeyNodePair if new node was created, else null
     */
    public KeyNodePair recursiveInsert(Node node, int key, RecordBlock recordBlock) {
        KeyNodePair newPair = null;
        KeyNodePair childNewPair = null;

        // if node is leaf node, root starts off as leaf node
        if (node instanceof LeafNode) {
            LeafNode currNode = (LeafNode) node;
            int idx;

            // adding to recordBlocks / recordBlocks + elements (if key not present)
            for (idx = 0; idx < currNode.getElements().size(); idx++) {
                if (key <= currNode.getElements().get(idx)) break;
            }

            // adding key to elements and new arraylist if key not found
            if (!currNode.getElements().contains(key)) {
                currNode.getElements().add(idx, key);
                currNode.getRecordBlocks().add(idx, new ArrayList<>());
            }

            // adding RecordBlock to recordBlocks
            currNode.getRecordBlocks().get(idx).add(recordBlock);

            // if need to split, split and return newPair
            if (currNode.getElements().size() > capacity) {
                newPair = createLeafNode(currNode);
                numNodes += 1;
            }
        }
        else {
            NonLeafNode currNode = (NonLeafNode) node;
            Node childNode = null;
            int idx;

            // if key less than elements value, we go to the child on the left
            for (idx = 0; idx < currNode.getElements().size(); idx++) {
                if (key < currNode.getElements().get(idx)) {
                    childNode = currNode.getChildren().get(idx);
                    break;
                }
            }

            // use last bucket if all >= key
            if (childNode == null) {
                childNode = currNode.getChildren().get(idx);
            }

            childNewPair = recursiveInsert(childNode, key, recordBlock);

            // if child node is overloaded
            if (childNewPair != null) {
                currNode.getElements().add(idx, childNewPair.getKey());
                currNode.getChildren().add(idx + 1, childNewPair.getNode());
                if (currNode.getElements().size() > capacity) {
                    newPair = createNonLeafNode(currNode);
                    numNodes += 1;
                }
            }
        }
        return newPair;
    }

    /**
     * when leaf node overloads, use this function to split and return the KeyNodePair with lowest key from new node
     * @param node, node to split
     * @return KeyNodePair with lowest key
     */
    private KeyNodePair createLeafNode(LeafNode node) {
        LeafNode newLeafNode = new LeafNode();

        int numEntries = node.getElements().size();
        int midpoint = (int)Math.floor((numEntries + 1) / 2);

        ArrayList<Integer> firstElements = new ArrayList<>();
        ArrayList<ArrayList<RecordBlock>> firstRecordBlocks = new ArrayList<>();
        ArrayList<Integer> secondElements = new ArrayList<>();
        ArrayList<ArrayList<RecordBlock>> secondRecordBlocks = new ArrayList<>();

        for (int i = 0; i < midpoint; i++) {
            firstElements.add(node.getElements().get(i));
            firstRecordBlocks.add(node.getRecordBlocks().get(i));
        }
        for (int i = midpoint; i < numEntries; i++) {
            secondElements.add(node.getElements().get(i));
            secondRecordBlocks.add(node.getRecordBlocks().get(i));
        }

        node.setElements(firstElements);
        node.setRecordBlocks((firstRecordBlocks));
        newLeafNode.setElements(secondElements);
        newLeafNode.setRecordBlocks(secondRecordBlocks);

        // do linked list stuff
        LeafNode nextNode = (LeafNode) node.getNextNode();
        if (nextNode != null) {
            nextNode.setPrevNode(newLeafNode);
        }
        newLeafNode.setNextNode(nextNode);
        newLeafNode.setPrevNode(node);
        node.setNextNode(newLeafNode);

        // set parent
        newLeafNode.setParent(node.getParent());

        return (new KeyNodePair(newLeafNode.getElements().get(0), newLeafNode));
    }

    /**
     * creates a nonleafnode and returns keynodepair with lowest key from new node
     * @param node, node that we are splitting
     * @return keynodepair with lowest key from new node
     */
    private KeyNodePair createNonLeafNode(NonLeafNode node) {
        NonLeafNode newNonLeafNode = new NonLeafNode();

        int numEntries = node.getElements().size();
        int entriesMidpoint = (int)Math.floor(numEntries / 2);
        int numChildren = node.getChildren().size();
        int childrenMidpoint = (int)Math.floor((numChildren + 1) / 2);

        // create the new KeyNodePair first, as new node will not have the midpoint element
        KeyNodePair newPair = new KeyNodePair(node.getElements().get(entriesMidpoint), newNonLeafNode);

        ArrayList<Integer> firstElements = new ArrayList<>();
        ArrayList<Node> firstChildren = new ArrayList<>();
        ArrayList<Integer> secondElements = new ArrayList<>();
        ArrayList<Node> secondChildren = new ArrayList<>();

        for (int i = 0; i < entriesMidpoint; i++) {
            firstElements.add(node.getElements().get(i));
        }
        for (int i = 0; i < childrenMidpoint; i++) {
            firstChildren.add(node.getChildren().get(i));
        }
        for (int i = entriesMidpoint + 1; i < numEntries; i++) {
            secondElements.add(node.getElements().get(i));
        }
        for (int i = childrenMidpoint; i < numChildren; i++) {
            secondChildren.add(node.getChildren().get(i));
        }

        node.setElements(firstElements);
        node.setChildren(firstChildren);
        newNonLeafNode.setElements(secondElements);
        newNonLeafNode.setChildren(secondChildren);

        newNonLeafNode.setParent(node.getParent());

        return newPair;
    }

    public void printExperiment2() {
        logger.info("Capacity n: " + capacity);
        logger.info("Number of nodes: " + numNodes);
        logger.info("Number of levels: " + numLevels);
        logger.info(getRootContent());
        return;
    }
}
