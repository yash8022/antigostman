package com.example.antig.swing.model.xml;

import java.util.ArrayList;
import java.util.List;

/**
 * XML-safe collection node.
 * Contains only children references, no parent reference.
 */
public class XmlCollection extends XmlNode {
    private List<XmlNode> children = new ArrayList<>();

    public XmlCollection() {
        super();
    }

    public XmlCollection(String id, String name) {
        super(id, name);
    }

    public List<XmlNode> getChildren() {
        return children;
    }

    public void setChildren(List<XmlNode> children) {
        this.children = children;
    }
}
