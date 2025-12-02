package com.example.antig.swing.ui;

import com.example.antig.swing.model.PostmanCollection;
import com.example.antig.swing.model.PostmanFolder;
import com.example.antig.swing.model.PostmanRequest;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

/**
 * Custom tree cell renderer that displays different icons for
 * Collection, Folder, and Request nodes.
 */
public class PostmanTreeCellRenderer extends DefaultTreeCellRenderer {
    
    private final Icon collectionIcon;
    private final Icon folderIcon;
    private final Icon requestIcon;
    
    public PostmanTreeCellRenderer() {
        // Create simple colored icons for different node types
        collectionIcon = createColoredIcon(new Color(52, 152, 219), 16, 16); // Blue
        folderIcon = createColoredIcon(new Color(241, 196, 15), 16, 16);     // Yellow/Gold
        requestIcon = createColoredIcon(new Color(46, 204, 113), 16, 16);    // Green
    }
    
    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                   boolean selected, boolean expanded,
                                                   boolean leaf, int row, boolean hasFocus) {
        
        super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        
        // Set icon based on node type
        if (value instanceof PostmanCollection) {
            setIcon(collectionIcon);
        } else if (value instanceof PostmanFolder) {
            setIcon(folderIcon);
        } else if (value instanceof PostmanRequest) {
            setIcon(requestIcon);
        }
        
        // Add padding for better spacing
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        
        // Set font size for better readability
        Font currentFont = getFont();
        setFont(currentFont.deriveFont(13.0f));
        
        return this;
    }
    
    /**
     * Create a simple colored square icon.
     */
    private Icon createColoredIcon(Color color, int width, int height) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw rounded rectangle
                g2d.setColor(color);
                g2d.fillRoundRect(x, y, width, height, 4, 4);
                
                // Draw border
                g2d.setColor(color.darker());
                g2d.drawRoundRect(x, y, width, height, 4, 4);
                
                g2d.dispose();
            }
            
            @Override
            public int getIconWidth() {
                return width;
            }
            
            @Override
            public int getIconHeight() {
                return height;
            }
        };
    }
}
