package com.example.versioncontrol;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.Consumer;

public class NodeTree {
    public static final int XSPACING = 120, YSPACING = 83, r = 25, bw = 5;//horizontal spacing between parents and children
    private int maxLayer = 0;
    private VersionControlGUI host;
    public Node root = null;
    public NodeTree(VersionControlGUI host){
        this.host = host;
    }
    //public int[] widths = new int[0];

    /*public void updateWidth(){
        int[] temp = new int[maxLayer + 1];
        int count = 0, currentLayer = 0;//formerly height

        ArrayList<Node> queue = new ArrayList<>();
        visitAll(root, queue::add);
        queue.sort(Comparator.comparingInt((Node e) -> e.layer));//formerly height

        for (Node item : queue) {
            if (item.layer != currentLayer) {
                temp[currentLayer++] = count;
                count = 0;
            }//else
            count++;
        }
        temp[temp.length - 1] = count;
        widths = temp;
    }*/

    public void visitAll(Node input, Consumer<Node> consumer){
        consumer.accept(input);
        for(int i = 0; i < input.children.length; i++) visitAll(input.children[i], consumer);
    }

    public void printTree(){
        visitAll(root, System.out::println);
    }

    public void blindPush(String PARENT, String ID, String author, String date, String commitMessage){
        //System.out.println("Searching for " + PARENT + ", parent of " + ID);
        visitAll(root, e -> {
            if(e.ID.equals(PARENT)) e.addChild(new Node(e, ID, author, date, commitMessage));
        });
    }

    public Node getNodeFromID(String input, Node myNode){
        if(myNode.ID.equals(input)) return myNode;
        for(int i = 0; i < myNode.children.length; i++) {
            Node temp = getNodeFromID(input, myNode.children[i]);
            if(temp != null) return temp;
        }
        return null;
    }

    public void genCoords(){
        visitAll(this.root, node -> {
            node.animX = NodeTree.XSPACING * node.layer;

            if (node.parent == null) {
                node.animY = 0;//root
                return;
            }

            int index = -1;
            int sumWidths = 0;
            final int totalLen = node.parent.children.length;
            for (int i = 0; i < totalLen; i++)
                if (node.parent.children[i].equals(node)) {
                    index = i;
                    break;
                } else sumWidths += node.parent.children[i].width;
            if (index == -1) throw new RuntimeException("Node is not listed among parent's children!");

            final int totalParentSpace = node.parent.width * NodeTree.YSPACING;
            final double percentageTaken = node.width * 1.0 / node.parent.width;//% of space node will take among its siblings
            final double before = sumWidths * 1.0 / node.parent.width;
            final double offset = percentageTaken * totalParentSpace / 2.0;

            node.animY = (int)(node.parent.animY - offset + totalParentSpace / 2.0 - before * totalParentSpace);
        });
    }

    public void addRoot(Node newRoot){
        newRoot.tree = this;
        this.root = newRoot;
    }

    public static class Node {
        public int layer = 0, width = 1;
        public int animX, animY;
        public String ID, parentID, author, date, commitMessage;
        public Node parent;
        public Node[] children = new Node[0];
        private NodeTree tree;

        public Node(Node parent, String ID, String author, String date, String commitMessage){
            this.parent = parent;
            this.ID = ID;
            this.author = author;
            this.date = date;
            this.commitMessage = commitMessage;
            if(parent != null) {
                this.layer = parent.layer + 1;
                this.tree = parent.tree;
                this.parentID = parent.ID;
                if(this.layer > this.tree.maxLayer) this.tree.maxLayer = this.layer;
            } else {
                this.parentID = "null";
            }
        }

        public void addChild(Node input){
            Node[] temp = new Node[children.length + 1];
            for(int i = 0; i < children.length; i++) temp[i] = children[i];
            temp[children.length] = input;
            children = temp;
            fixWidth(this);
        }

        public static void fixWidth(Node input){
            input.width = 0;
            for(int i = 0; i < input.children.length; i++) input.width += input.children[i].width;
            if(input.width < 1) input.width = 1;
            if(input.parent != null) fixWidth(input.parent);
        }

        public String toString(){
            String temp = "";
            for(int i = 0; i < this.layer; i++) temp += '\t';
            temp += this.ID + " " +  this.parentID + " " +  this.date + " " +  this.author + " " +  this.commitMessage;
            return temp;
        }
    }
}
