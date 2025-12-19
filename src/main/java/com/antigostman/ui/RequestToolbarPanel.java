package com.antigostman.ui;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import com.antigostman.model.PostmanRequest;
import java.util.function.Consumer;

public class RequestToolbarPanel extends JPanel {
    private JTextField urlField;
    private JComboBox<String> methodComboBox;
    private JComboBox<String> bodyTypeComboBox;
    private JComboBox<String> httpVersionComboBox;
    private JSpinner timeoutSpinner;
    private JCheckBox dlContentCheckbox;
    private JButton sendButton;

    private PostmanRequest currentRequest;
    private boolean isLoading = false;

    private final Runnable onSend;
    private final Consumer<String> onBodyTypeChanged;
    private final Runnable onMethodChanged;

    public RequestToolbarPanel(Runnable onSend, Consumer<String> onBodyTypeChanged, Runnable onMethodChanged) {
        this.onSend = onSend;
        this.onBodyTypeChanged = onBodyTypeChanged;
        this.onMethodChanged = onMethodChanged;
        
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        initComponents();
    }

    private void initComponents() {
        String[] methods = { "GET", "POST", "PUT", "DELETE", "PATCH" };
        methodComboBox = new JComboBox<>(methods);
        methodComboBox.setPreferredSize(new Dimension(100, 30));
        methodComboBox.addActionListener(e -> {
            if (!isLoading && currentRequest != null) {
                String newMethod = (String) methodComboBox.getSelectedItem();
                if (newMethod != null && !newMethod.equals(currentRequest.getMethod())) {
                    currentRequest.setMethod(newMethod);
                    onMethodChanged.run();
                }
            }
        });

        String[] bodyTypes = { "TEXT", "JSON", "XML", "FORM ENCODED", "MULTIPART" };
        bodyTypeComboBox = new JComboBox<>(bodyTypes);
        bodyTypeComboBox.setPreferredSize(new Dimension(240, 30));
        bodyTypeComboBox.addActionListener(e -> {
            if (!isLoading && currentRequest != null) {
                String newType = (String) bodyTypeComboBox.getSelectedItem();
                if (newType != null && !newType.equals(currentRequest.getBodyType())) {
                    currentRequest.setBodyType(newType);
                    onBodyTypeChanged.accept(newType);
                }
            }
        });

        urlField = new JTextField();
        urlField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { saveUrl(); }
            public void removeUpdate(DocumentEvent e) { saveUrl(); }
            public void changedUpdate(DocumentEvent e) { saveUrl(); }
            private void saveUrl() {
                if (!isLoading && currentRequest != null) {
                    currentRequest.setUrl(urlField.getText());
                }
            }
        });
        
        urlField.getInputMap().put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER, java.awt.event.InputEvent.CTRL_DOWN_MASK), "sendRequest");
        urlField.getActionMap().put("sendRequest", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) { onSend.run(); }
        });

        sendButton = new JButton("Send");
        sendButton.addActionListener(e -> onSend.run());
        sendButton.setPreferredSize(new Dimension(80, 30));

        timeoutSpinner = new JSpinner(new SpinnerNumberModel(1000, 1, Integer.MAX_VALUE, 100));
        timeoutSpinner.setPreferredSize(new Dimension(80, 30));
        timeoutSpinner.addChangeListener(e -> {
            if (!isLoading && currentRequest != null) {
                currentRequest.setTimeout(((Number) timeoutSpinner.getValue()).longValue());
            }
        });

        String[] httpVersions = { "HTTP/1.1", "HTTP/2" };
        httpVersionComboBox = new JComboBox<>(httpVersions);
        httpVersionComboBox.setPreferredSize(new Dimension(90, 30));
        httpVersionComboBox.addActionListener(e -> {
            if (!isLoading && currentRequest != null) {
                currentRequest.setHttpVersion((String) httpVersionComboBox.getSelectedItem());
            }
        });

        dlContentCheckbox = new JCheckBox("DL Content");
        dlContentCheckbox.addActionListener(e -> {
            if (!isLoading && currentRequest != null) {
                currentRequest.setDownloadContent(dlContentCheckbox.isSelected());
            }
        });

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        JPanel line1 = new JPanel(new BorderLayout(5, 0));
        line1.setOpaque(false);
        line1.add(urlField, BorderLayout.CENTER);
        line1.add(sendButton, BorderLayout.EAST);
        line1.setMaximumSize(new Dimension(Integer.MAX_VALUE, line1.getPreferredSize().height));

        JPanel line2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        line2.setOpaque(false);
        line2.add(methodComboBox);
        line2.add(Box.createHorizontalStrut(5));
        line2.add(bodyTypeComboBox);
        line2.add(Box.createHorizontalStrut(5));
        line2.add(httpVersionComboBox);
        line2.add(Box.createHorizontalStrut(20));
        line2.add(new JLabel("Timeout: "));
        line2.add(timeoutSpinner);
        line2.add(Box.createHorizontalStrut(20));
        line2.add(dlContentCheckbox);
        line2.setMaximumSize(new Dimension(Integer.MAX_VALUE, line2.getPreferredSize().height));

        contentPanel.add(line1);
        contentPanel.add(Box.createVerticalStrut(5));
        contentPanel.add(line2);

        add(contentPanel, BorderLayout.CENTER);
    }

    public void loadRequest(PostmanRequest req) {
        this.currentRequest = req;
        this.isLoading = true;
        try {
            urlField.setText(req.getUrl());
            methodComboBox.setSelectedItem(req.getMethod());
            bodyTypeComboBox.setSelectedItem(req.getBodyType() != null ? req.getBodyType() : "TEXT");
            httpVersionComboBox.setSelectedItem(req.getHttpVersion() != null ? req.getHttpVersion() : "HTTP/1.1");
            timeoutSpinner.setValue(req.getTimeout());
            dlContentCheckbox.setSelected(req.isDownloadContent());
        } finally {
            this.isLoading = false;
        }
    }

    public void setSendButtonEnabled(boolean enabled) {
        sendButton.setEnabled(enabled);
    }

    public String getUrl() {
        return urlField.getText();
    }
}
