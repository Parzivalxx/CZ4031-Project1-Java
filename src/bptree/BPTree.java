package bptree;

import main.Main;
import memorypool.RecordBlock;

import java.sql.Array;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.Queue;
import java.util.ArrayDeque;

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
        for (Node child : newNonLeafNode.getChildren())
            child.setParent(newNonLeafNode);
        if (node.getNextNode() != null)
            node.getNextNode().setPrevNode(newNonLeafNode);
        newNonLeafNode.setPrevNode(node);
        newNonLeafNode.setNextNode(node.getNextNode());
        node.setNextNode(newNonLeafNode);

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

    public void findAndDeleteKey(int key) {
        LeafNode node = findLeafNode(root, key);
        deleteInLeaf(node, key);
        return;
    }

    private void deleteInLeaf(Node node, int key) {
        int i;
        LeafNode currNode = (LeafNode) node;
        int prevFirst = currNode.getElements().get(0);
        for (i = 0; i < currNode.getElements().size(); i++) {
            if (key == currNode.getElements().get(i)) {
                currNode.getElements().remove(i);
                currNode.getRecordBlocks().remove(i);
                break;
            }
        }
        // if still enough keys, only update if deleted index == 0, no recursive delete
        if (currNode.getElements().size() >= minLeafKeys) {
            if (i == 0) {
                int newKey = currNode.getElements().get(0);
                updateParent(key, newKey);
            }
        } else { // else, check if can borrow, if not join and recursively delete
            LeafNode prev = (LeafNode) currNode.getPrevNode();
            LeafNode next = (LeafNode) currNode.getNextNode();
            if (prev == null && next == null) return; // no neighbours
            boolean borrowed = false;
            if (prev != null) {
                // try to borrow from left neighbour
                if (prev.getElements().size() > minLeafKeys) {
                    int prevNumElements = prev.getElements().size();
                    int borrowedKey = prev.getElements().remove(prevNumElements - 1);
                    ArrayList<RecordBlock> borrowedRecord = prev.getRecordBlocks().remove(prevNumElements - 1);
                    currNode.getElements().add(0, borrowedKey);
                    currNode.getRecordBlocks().add(0, borrowedRecord);
                    updateParent(prevFirst, borrowedKey);
                    borrowed = true;
                }
            }
            else if (next != null) {
                // try to borrow from right neighbour
                if (next.getElements().size() > minLeafKeys) {
                    int borrowedKey = next.getElements().remove(0);
                    ArrayList<RecordBlock> borrowedRecord = next.getRecordBlocks().remove(0);
                    currNode.getElements().add(borrowedKey);
                    currNode.getRecordBlocks().add(borrowedRecord);
                    updateParent(borrowedKey, next.getElements().get(0));
                    borrowed = true;
                }
            }
            if (!borrowed) { // join and recursively delete
                if (prev != null) {
                    ArrayList<Integer> currElements = currNode.getElements();
                    ArrayList<ArrayList<RecordBlock>> currRecords = currNode.getRecordBlocks();
                    for (int j = 0; j < currElements.size(); j++) {
                        prev.getElements().add(currElements.get(j));
                        prev.getRecordBlocks().add(currRecords.get(j));
                    }
                    prev.setNextNode(currNode.getNextNode());
                    if (currNode.getNextNode() != null)
                        currNode.getNextNode().setPrevNode(prev);
                    // due to time constraints, we will just assign the node to the prev node of parent if parent is going to be deleted
                    if (!(currNode.getParent().equals(root)) && (currNode.getParent().getElements().size() == 1)) {
                        if (currNode.getParent().getChildren().contains(prev)) {
                            NonLeafNode newParent = (NonLeafNode) currNode.getParent().getPrevNode();
                            newParent.getElements().add(prev.getElements().get(0));
                            newParent.getChildren().add(prev);
                            prev.setParent(newParent);
                            if (newParent.getElements().size() > capacity) {
                                KeyNodePair newPair = createNonLeafNode(newParent);
                            }
                        }
                    }
                    deleteFromParent(currNode, prevFirst);
                }
                else if (next != null) { // join and recursively delete
                    LeafNode nextNode = (LeafNode) currNode.getNextNode();
                    ArrayList<Integer> nextElements = nextNode.getElements();
                    ArrayList<ArrayList<RecordBlock>> nextRecords = nextNode.getRecordBlocks();
                    for (int j = 0; j < nextElements.size(); j++) {
                        currNode.getElements().add(nextElements.get(j));
                        currNode.getRecordBlocks().add(nextRecords.get(j));
                    }
                    currNode.setNextNode(nextNode.getNextNode());
                    if (nextNode.getNextNode() != null)
                        nextNode.getNextNode().setPrevNode(currNode);
                    if (!(currNode.getParent().equals(root)) && (currNode.getParent().getElements().size() == 1)) {
                        NonLeafNode newParent = (NonLeafNode) currNode.getParent().getNextNode();
                        int lowest = getSmallestKeyFromChildren(newParent);
                        newParent.getElements().add(0, lowest);
                        newParent.getChildren().add(0, next);
                        if (newParent.getElements().size() > capacity) {
                            KeyNodePair newPair = createNonLeafNode(newParent);
                        }
                    }
                    key = nextNode.getElements().get(0);
                    deleteFromParent(nextNode, key);
                }
            }
        }
    }


    private void mergeOrBorrowNonLeaf(NonLeafNode node) {
        System.out.println("HERE4");
        NonLeafNode currNode = (NonLeafNode) node;
        NonLeafNode prev = (NonLeafNode) currNode.getPrevNode();
        NonLeafNode next = (NonLeafNode) currNode.getNextNode();
        if (prev == null && next == null) return; // no neighbours
        boolean merged = false;
        // try merge first
        if (prev != null && (prev.getElements().size() + node.getChildren().size()) <= capacity) {
            System.out.println("HERE7");
            ArrayList<Integer> currElements = currNode.getElements();
            ArrayList<Node> currChildren = currNode.getChildren();
            for (int j = 0; j < currElements.size(); j++) {
                prev.getElements().add(currElements.get(j));
                prev.getChildren().add(currChildren.get(j));
            }
            prev.getChildren().add(currChildren.get(currChildren.size() - 1));
            prev.setNextNode(currNode.getNextNode());
            if (currNode.getNextNode() != null)
                currNode.getNextNode().setPrevNode(prev);
            int key = getSmallestKeyFromChildren(currNode);
            deleteFromParent(currNode, key);
            merged = true;
        }
        else if (next != null && (next.getElements().size() + node.getChildren().size()) <= capacity) {
            System.out.println("HERE8");
            NonLeafNode nextNode = (NonLeafNode) currNode.getNextNode();
            ArrayList<Integer> nextElements = nextNode.getElements();
            ArrayList<Node> nextChildren = nextNode.getChildren();
            for (int j = 0; j < nextElements.size(); j++) {
                currNode.getElements().add(nextElements.get(j));
                currNode.getChildren().add(nextChildren.get(j));
            }
            currNode.getChildren().add(nextChildren.get(nextChildren.size() - 1));
            currNode.setNextNode(nextNode.getNextNode());
            if (nextNode.getNextNode() != null)
                nextNode.getNextNode().setPrevNode(currNode);
            int key = getSmallestKeyFromChildren(currNode);
            deleteFromParent(currNode, key);
            currNode.setParent(nextNode.getParent());
            updateParent(getSmallestKeyFromChildren(nextNode), key);
            merged = true;
        }
        if (!merged) {
            int difference = minNonLeafKeys - currNode.getElements().size();
            if (prev != null) {
                System.out.println("HERE5");
                int oldKey = currNode.getElements().get(0);
                // just assuming prev has sufficient to lend currNode and maintain enough keys
                int prevNumElements = prev.getElements().size();
                currNode.getElements().add(0, getSmallestKeyFromChildren(currNode));
                for (int i = 0; i < difference; i++) {
                    currNode.getElements().add(0, prev.getElements().remove(prevNumElements - 1 - i));
                    currNode.getChildren().add(0, prev.getChildren().remove(prevNumElements - i));
                }
                currNode.getElements().remove(0);
                int newKey = currNode.getElements().get(0);
                updateParent(oldKey, newKey);
            }
            else if (next != null) {
                System.out.println("HERE6");
                // try to borrow from right neighbour
                int oldKey = next.getElements().get(0);
                // just assuming prev has sufficient to lend currNode and maintain enough keys
                int nextNumElements = next.getElements().size();
                currNode.getElements().add(getSmallestKeyFromChildren(next));
                currNode.getChildren().add(next.getChildren().get(0));
                for (int i = 0; i < difference - 1; i++) {
                    currNode.getElements().add(next.getElements().remove(i));
                    currNode.getChildren().add(next.getChildren().remove(i));
                }
                next.getElements().remove(0);
                int newKey = next.getElements().get(0);
                updateParent(oldKey, newKey);
            }
        }
    }

    private void deleteFromParent(Node node, int key) {
        NonLeafNode parentNode = (NonLeafNode) node.getParent();
        System.out.println(key);
        if (key < parentNode.getElements().get(0)) {
            System.out.println("HERE");
            if (parentNode.getElements().size() == 1) {
                mergeOrBorrowNonLeaf(parentNode);
                return;
            }
            int oldKey = parentNode.getElements().get(0);
            parentNode.getElements().remove(0);
            parentNode.getChildren().remove(0);
            updateParent(oldKey, parentNode.getElements().get(0));
        } else {
            for (int i = 0; i < parentNode.getElements().size(); i++) {
                if (key == parentNode.getElements().get(i)) {
                    System.out.println("HERE3");
                    parentNode.getElements().remove(i);
                    parentNode.getChildren().remove(i + 1);
                    break;
                }
            }
        }
        if (parentNode.equals(root)) {
            if (parentNode.getElements().size() == 0) {
                if (parentNode.getChildren().size() > 0) {
                    root = parentNode.getChildren().get(0);
                }
            }
            return;
        }
        if (parentNode.getElements().size() < minNonLeafKeys) {
            key = getSmallestKeyFromChildren(parentNode);
            mergeOrBorrowNonLeaf(parentNode);
        }
        return;
    }

    private int getSmallestKeyFromChildren(Node currNode) {
        Node tempNode = currNode;
        while (tempNode instanceof NonLeafNode) {
            NonLeafNode tempNode2 = (NonLeafNode) tempNode;
            Node nextNode = tempNode2.getChildren().get(0);
            tempNode = nextNode;
        }
        int key = tempNode.getElements().get(0);
        return key;
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

    public void printTree() {
        ArrayList<Node> nodes = new ArrayList<>();
        int numNodes = 1;
        int count = 0;
        Node cur;

        nodes.add(root);

        if (root == null) {
            System.out.println("-----Tree is empty-----");
            return;
        }

        System.out.println("-----Printing Tree-----");

        while (nodes.isEmpty() == false) {
            cur = nodes.get(0);

            // System.out.printf("size of node: %d\n", cur.numElements());
            for (int e : cur.getElements()) {
                System.out.printf("%d ", e);

            }

            if (cur instanceof NonLeafNode) {
                for (Node n : ((NonLeafNode)cur).getChildren()) {
                    nodes.add(n);
                }
            }

            count += 1;

            if (count < numNodes) {
                System.out.printf("| ");
            } else {
                System.out.printf("\n");
                numNodes = nodes.size() - 1;
                count = 0;
            }

            nodes.remove(0);
        }
    }
}
