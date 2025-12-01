package com.example.antig.swing.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = PostmanCollection.class, name = "collection"),
        @JsonSubTypes.Type(value = PostmanFolder.class, name = "folder"),
        @JsonSubTypes.Type(value = PostmanRequest.class, name = "request")
})
public abstract class PostmanNode extends DefaultMutableTreeNode {
    private String id;
    private String name;
    private Map<String, String> environment = new HashMap<>();
    private Map<String, String> headers = new HashMap<>();
    private String prescript = "";
    private String postscript = "";

    public PostmanNode() {
        this.id = UUID.randomUUID().toString();
    }

    public PostmanNode(String name) {
        this();
        this.name = name;
        setUserObject(name);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        setUserObject(name);
    }

    public Map<String, String> getEnvironment() {
        return environment;
    }

    public void setEnvironment(Map<String, String> environment) {
        this.environment = environment;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getPrescript() {
        return prescript;
    }

    public void setPrescript(String prescript) {
        this.prescript = prescript;
    }

    public String getPostscript() {
        return postscript;
    }

    public void setPostscript(String postscript) {
        this.postscript = postscript;
    }

    @Override
    public String toString() {
        return name;
    }

    // Prevent serialization of tree structure to avoid cycles
    @com.fasterxml.jackson.annotation.JsonIgnore
    @Override
    public javax.swing.tree.TreeNode getParent() {
        return super.getParent();
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    @Override
    public javax.swing.tree.TreeNode getRoot() {
        return super.getRoot();
    }
    
    @com.fasterxml.jackson.annotation.JsonIgnore
    @Override
    public javax.swing.tree.TreeNode getChildAt(int childIndex) {
        return super.getChildAt(childIndex);
    }
    
    @com.fasterxml.jackson.annotation.JsonIgnore
    @Override
    public int getChildCount() {
        return super.getChildCount();
    }
    
    @com.fasterxml.jackson.annotation.JsonIgnore
    @Override
    public int getIndex(javax.swing.tree.TreeNode node) {
        return super.getIndex(node);
    }
    
    @com.fasterxml.jackson.annotation.JsonIgnore
    @Override
    public boolean getAllowsChildren() {
        return super.getAllowsChildren();
    }
    
    @com.fasterxml.jackson.annotation.JsonIgnore
    @Override
    public boolean isLeaf() {
        return super.isLeaf();
    }
    
    @com.fasterxml.jackson.annotation.JsonIgnore
    @Override
    public java.util.Enumeration<javax.swing.tree.TreeNode> children() {
        return super.children();
    }
    
    @com.fasterxml.jackson.annotation.JsonIgnore
    @Override
    public Object getUserObject() {
        return super.getUserObject();
    }
    
    @com.fasterxml.jackson.annotation.JsonIgnore
    @Override
    public javax.swing.tree.TreeNode[] getPath() {
        return super.getPath();
    }
}
