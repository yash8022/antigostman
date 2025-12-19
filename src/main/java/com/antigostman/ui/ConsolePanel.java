package com.antigostman.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class ConsolePanel extends JPanel {
    private JTextArea consoleTextArea;
    private JTabbedPane tabbedPane;

    public ConsolePanel() {
        setLayout(new BorderLayout());
        initComponents();
        initConsole();
    }

    private void initComponents() {
        tabbedPane = new JTabbedPane();
        consoleTextArea = new JTextArea();
        consoleTextArea.setEditable(false);
        consoleTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        consoleTextArea.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    JPopupMenu menu = new JPopupMenu();
                    JMenuItem clearItem = new JMenuItem("Clear Console");
                    clearItem.addActionListener(ev -> clear());
                    menu.add(clearItem);
                    menu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        JPanel panel = new JPanel(new BorderLayout());
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        JButton clearButton = new JButton("Clear");
        clearButton.setMargin(new Insets(2, 5, 2, 5));
        clearButton.setFocusable(false);
        clearButton.addActionListener(e -> clear());
        toolbar.add(clearButton);

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(new JScrollPane(consoleTextArea), BorderLayout.CENTER);
        
        tabbedPane.addTab("Console", panel);
        add(tabbedPane, BorderLayout.CENTER);
    }

    private void initConsole() {
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;

        System.setOut(new PrintStream(new ConsoleOutputStream(consoleTextArea, originalOut)));
        System.setErr(new PrintStream(new ConsoleOutputStream(consoleTextArea, originalErr)));
    }

    public void clear() {
        consoleTextArea.setText("");
    }

    public JTextArea getTextArea() {
        return consoleTextArea;
    }
    
    public JTabbedPane getTabbedPane() {
        return tabbedPane;
    }

    private static class ConsoleOutputStream extends OutputStream {
        private final JTextArea textArea;
        private final OutputStream target;

        public ConsoleOutputStream(JTextArea textArea, OutputStream target) {
            this.textArea = textArea;
            this.target = target;
        }

        @Override
        public void write(int b) throws IOException {
            target.write(b);
            final String s = String.valueOf((char) b);
            SwingUtilities.invokeLater(() -> {
                textArea.append(s);
                textArea.setCaretPosition(textArea.getDocument().getLength());
            });
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            target.write(b, off, len);
            final String s = new String(b, off, len);
            SwingUtilities.invokeLater(() -> {
                textArea.append(s);
                textArea.setCaretPosition(textArea.getDocument().getLength());
            });
        }

        @Override
        public void flush() throws IOException {
            target.flush();
        }
    }
}
