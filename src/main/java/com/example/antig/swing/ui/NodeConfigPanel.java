package com.example.antig.swing.ui;

import com.example.antig.swing.model.PostmanNode;
import com.example.antig.swing.model.PostmanRequest;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Tabbed panel for configuring nodes (Collection, Folder, Request).
 * 
 * Common tabs for all nodes:
 * - Headers: Text area for headers in properties format
 * - Prescript: Script to run before request execution
 * - Postscript: Script to run after request execution
 * 
 * Request-only tab:
 * - Execution: Params (top) and Response (bottom)
 */
public class NodeConfigPanel extends JPanel {
    
    private JTabbedPane tabbedPane;
    
    // Common tabs
    private JTextArea headersArea;
    private JTextArea prescriptArea;
    private JTextArea postscriptArea;
    
    // Request-only tabs
    private JTextArea paramsArea;
    private JTextArea responseArea;
    private JPanel executionPanel;
    
    private PostmanNode currentNode;
    
    public NodeConfigPanel() {
        setLayout(new BorderLayout());
        
        tabbedPane = new JTabbedPane();
        
        // Tab 1: Headers
        headersArea = createTextArea();
        tabbedPane.addTab("Headers", new JScrollPane(headersArea));
        
        // Tab 2: Prescript
        prescriptArea = createTextArea();
        tabbedPane.addTab("Prescript", new JScrollPane(prescriptArea));
        
        // Tab 3: Postscript
        postscriptArea = createTextArea();
        tabbedPane.addTab("Postscript", new JScrollPane(postscriptArea));
        
        // Tab 4: Execution (for requests only)
        createExecutionPanel();
        
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    private JTextArea createTextArea() {
        JTextArea area = new JTextArea();
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        area.setTabSize(2);
        return area;
    }
    
    private void createExecutionPanel() {
        executionPanel = new JPanel(new BorderLayout());
        
        // Split into two parts: params (top) and response (bottom)
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.3); // 30% for params, 70% for response
        
        // Top: Params
        paramsArea = createTextArea();
        JPanel paramsPanel = new JPanel(new BorderLayout());
        paramsPanel.add(new JLabel("Request Parameters (key=value format):"), BorderLayout.NORTH);
        paramsPanel.add(new JScrollPane(paramsArea), BorderLayout.CENTER);
        splitPane.setTopComponent(paramsPanel);
        
        // Bottom: Response
        responseArea = createTextArea();
        responseArea.setEditable(false);
        JPanel responsePanel = new JPanel(new BorderLayout());
        responsePanel.add(new JLabel("Response:"), BorderLayout.NORTH);
        responsePanel.add(new JScrollPane(responseArea), BorderLayout.CENTER);
        splitPane.setBottomComponent(responsePanel);
        
        executionPanel.add(splitPane, BorderLayout.CENTER);
    }
    
    /**
     * Load node data into the UI.
     */
    public void loadNode(PostmanNode node) {
        this.currentNode = node;
        
        if (node == null) {
            clearAll();
            return;
        }
        
        // Load headers (convert map to properties format)
        headersArea.setText(mapToProperties(node.getHeaders()));
        
        // Load scripts
        prescriptArea.setText(node.getPrescript() != null ? node.getPrescript() : "");
        postscriptArea.setText(node.getPostscript() != null ? node.getPostscript() : "");
        
        // Show/hide execution tab based on node type
        if (node instanceof PostmanRequest) {
            // Add execution tab if not present
            if (tabbedPane.indexOfComponent(executionPanel) == -1) {
                tabbedPane.addTab("Execution", executionPanel);
            }
            
            PostmanRequest request = (PostmanRequest) node;
            paramsArea.setText(request.getParams() != null ? request.getParams() : "");
            responseArea.setText(""); // Clear previous response
        } else {
            // Remove execution tab if present
            int index = tabbedPane.indexOfComponent(executionPanel);
            if (index != -1) {
                tabbedPane.removeTabAt(index);
            }
        }
    }
    
    /**
     * Save UI data back to the node.
     */
    public void saveNode() {
        if (currentNode == null) return;
        
        // Save headers (convert properties format to map)
        currentNode.setHeaders(propertiesToMap(headersArea.getText()));
        
        // Save scripts
        currentNode.setPrescript(prescriptArea.getText());
        currentNode.setPostscript(postscriptArea.getText());
        
        // Save request-specific data
        if (currentNode instanceof PostmanRequest) {
            PostmanRequest request = (PostmanRequest) currentNode;
            request.setParams(paramsArea.getText());
        }
    }
    
    /**
     * Get the response area for displaying HTTP responses.
     */
    public JTextArea getResponseArea() {
        return responseArea;
    }
    
    /**
     * Clear all fields.
     */
    private void clearAll() {
        headersArea.setText("");
        prescriptArea.setText("");
        postscriptArea.setText("");
        paramsArea.setText("");
        responseArea.setText("");
    }
    
    /**
     * Convert map to properties format (key=value\n).
     */
    private String mapToProperties(Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
        }
        return sb.toString();
    }
    
    /**
     * Convert properties format to map.
     */
    private Map<String, String> propertiesToMap(String text) {
        Map<String, String> map = new HashMap<>();
        
        if (text == null || text.trim().isEmpty()) {
            return map;
        }
        
        String[] lines = text.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue; // Skip empty lines and comments
            }
            
            int equalsIndex = line.indexOf('=');
            if (equalsIndex > 0) {
                String key = line.substring(0, equalsIndex).trim();
                String value = line.substring(equalsIndex + 1).trim();
                map.put(key, value);
            }
        }
        
        return map;
    }
}
