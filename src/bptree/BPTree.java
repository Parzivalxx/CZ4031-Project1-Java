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
    private int numNodesAccessed;
    private int minNonLeafKeys;
    private int minNonLeafChildren;
    private int minLeafKeys;
    private int minLeafChildren;
    static Logger logger = Logger.getLogger(Main.class.getName());

    public BPTree(int capacity) {
        this.root = null;
        this.capacity = capacity;
        this.numNodes = 0;
        this.numLevels = 0;
        this.numNodesAccessed = 0;
        this.minNonLeafKeys = (int)Math.floor(capacity / 2);
        this.minNonLeafChildren = (int)Math.floor(capacity / 2) + 1;
        this.minLeafKeys = (int)Math.floor((capacity + 1) / 2);
        this.minLeafChildren = (int)Math.floor((capacity + 1) / 2);
    }

    public int getCapacity() {
        return capacity;
    }

    public int getNumNodes() {
        return numNodes;
    }

    public int getNumLevels() {
        return numLevels;
    }

    public int getNumNodesAccessed() {
        return numNodesAccessed;
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

    /**
     * traverses to leafNode and gets all recordBlocks accessed between minKey and maxKey
     * @param minKey, lowest key searching
     * @param maxKey, highest key searching
     * @return, list of recordBlocks accessed
     */
    public ArrayList<RecordBlock> searchNodes(int minKey, int maxKey) {
        ArrayList<RecordBlock> accessedRecords = new ArrayList<>();

        if (root != null) {
             LeafNode leafNode = findLeafNode(root, minKey);
             int i = 0;
             int currKey = leafNode.getElements().get(0);

             while (currKey <= maxKey) {
                 if (currKey >= minKey) {
                     for (RecordBlock rb : leafNode.getRecordBlocks().get(i)) {
                         accessedRecords.add(rb);
                     }
                 }

                 // continue in same node
                 if (i < leafNode.getElements().size() - 1) {
                     i += 1;
                 } else { // else, go to next node
                     if (leafNode.getNextNode() == null) return accessedRecords;
                     leafNode = (LeafNode) leafNode.getNextNode();
                     i = 0;
                     numNodesAccessed += 1;
                 }
                 currKey = leafNode.getElements().get(i);
             }
        }
        return accessedRecords;
    }

    /**
     * finds leafNode containing key and increments numNodesAccessed on the way there
     * @param node, starting node
     * @param key, key we are looking for
     * @return, leafNode which contains key
     */
    private LeafNode findLeafNode(Node node, int key) {
        numNodesAccessed = 0;
        while (node instanceof NonLeafNode) {
            numNodesAccessed += 1;
            NonLeafNode currNode = (NonLeafNode) node;
            Node nextNode = null;
            int i;

            for (i = 0; i < currNode.getElements().size(); i++) {
                if (key < currNode.getElements().get(i)) {
                    nextNode = currNode.getChildren().get(i);
                    break;
                }
            }
            if (nextNode == null) {
                nextNode = currNode.getChildren().get(i);
            }
            node = nextNode;
        }
        numNodesAccessed += 1;
        return (LeafNode) node;
    }

    public void deleteKey(int key) {
        LeafNode node = findLeafNode(root, key);
        int i;
        for (i = 0; i < node.getElements().size(); i++) {
            if (key == node.getElements().get(i)) {
                node.getElements().remove(i);
                node.getRecordBlocks().remove(i);
                break;
            }
        }

        // if still enough keys, only update if deleted index == 0, no recursive delete
        if (node.getElements().size() >= minLeafKeys) {
            if (i == 0) {
                int newKey = node.getElements().get(0);
                updateParent(key, newKey);
            }
        } else { // else, check if can borrow, if not join and recursively delete
            LeafNode prev = (LeafNode) node.getPrevNode();
            LeafNode next = (LeafNode) node.getNextNode();
            if (prev == null && next == null) return; // no neighbours
            if (prev != null) {
                // try to borrow from left neighbour
                if (prev.getElements().size() > minLeafKeys) {
                    int prevNumElements = prev.getElements().size();
                    int borrowedKey = prev.getElements().get(prevNumElements - 1);
                    ArrayList<RecordBlock> borrowedRecord = prev.getRecordBlocks().remove(prevNumElements - 1);
                    node.getElements().add(0, borrowedKey);
                    node.getRecordBlocks().add(0, borrowedRecord);
                    updateParent(key, borrowedKey);
                } else { // join and recursively delete

                }
            }
            else if (next != null) {
                // try to borrow from right neighbour
                if (next.getElements().size() > minLeafKeys) {
                    int borrowedKey = next.getElements().remove(0);
                    ArrayList<RecordBlock> borrowedRecord = next.getRecordBlocks().remove(0);
                    node.getElements().add(borrowedKey);
                    node.getRecordBlocks().add(borrowedRecord);
                    updateParent(key, borrowedKey);
                } else { // join and recursively delete

                }
            }
        }
    }

    /**
     * updates parent in top down fashion starting from root
     * @param oldKey, old key to remove
     * @param newKey, new key to insert
     */
    public void updateParent(int oldKey, int newKey) {
        Node node = root;

        int i;
        int childIdx = -1;

        while (node instanceof NonLeafNode) {
            NonLeafNode curr = (NonLeafNode) node;
            for (i = 0; i < node.getElements().size(); i++) {
                int element = node.getElements().get(i);
                if (oldKey == element) {
                    childIdx = i;
                    node.getElements().remove(i);
                    node.getElements().add(i, newKey);
                }
                else if (oldKey < element) childIdx = i;
            }

            // if equal or larger to key in node, use last idx
            if (childIdx == -1) childIdx = i;
            node = curr.getChildren().get(childIdx);
        }
        return;
    }

    private void recursiveDelete() {
        ;
    }

    /**
     * calculates the average of average ratings for all records returned within a range
     * @param accessedRecords, records accessed within a range of keys
     * @return, average of average ratings
     */
    public float getAvgOfAvgRatings(ArrayList<RecordBlock> accessedRecords) {
        if (accessedRecords.size() == 0) return 0;
        float total = 0;
        for (RecordBlock rb : accessedRecords) {
            total += rb.getRecord().getAvgRating();
        }
        return (total / accessedRecords.size());
    }
}
