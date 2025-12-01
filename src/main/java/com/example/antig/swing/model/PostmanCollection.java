package com.example.antig.swing.model;

import java.util.ArrayList;
import java.util.List;

public class PostmanCollection extends PostmanNode {
    private List<PostmanNode> children = new ArrayList<>();

    public PostmanCollection() {
        super();
    }

    public PostmanCollection(String name) {
        super(name);
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public List<PostmanNode> getChildren() {
        return children;
    }

    public void setChildren(List<PostmanNode> children) {
        this.children = children;
        removeAllChildren();
        if (children != null) {
            for (PostmanNode child : children) {
                add(child);
            }
        }
    }
    
    @Override
    public void add(javax.swing.tree.MutableTreeNode newChild) {
        super.add(newChild);
        if (newChild instanceof PostmanNode && !children.contains(newChild)) {
            children.add((PostmanNode) newChild);
        }
    }
    
    @Override
    public void remove(javax.swing.tree.MutableTreeNode aChild) {
        super.remove(aChild);
        children.remove(aChild);
    }
}
