package com.example.antig.swing.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Manages the list of recently opened project files and open workspace files.
 */
public class RecentProjectsManager {
    private static final String PREFS_FILE = System.getProperty("user.home") + "/.postman-clone-recent.properties";
    private static final String RECENT_KEY_PREFIX = "recent.";
    private static final String OPEN_PROJECT_KEY_PREFIX = "open.project.";
    private static final String THEME_KEY = "theme";
    private static final String LAST_SAVE_DIR_KEY = "last.save.dir";
    private static final String UI_FRAME_WIDTH = "ui.frame.width";
    private static final String UI_FRAME_HEIGHT = "ui.frame.height";
    private static final String UI_SPLIT_MAIN = "ui.split.main.location";
    private static final String UI_SPLIT_CONSOLE = "ui.split.console.location";
    private static final int MAX_RECENT = 10;
    
    private final List<String> recentProjects = new ArrayList<>();
    
    public RecentProjectsManager() {
        loadRecentProjects();
    }

    /**
     * Get the last directory used for saving files.
     */
    public String getLastSaveDirectory() {
        Properties props = loadProperties();
        return props.getProperty(LAST_SAVE_DIR_KEY);
    }

    /**
     * Set the last directory used for saving files.
     */
    public void setLastSaveDirectory(String path) {
        Properties props = loadProperties();
        props.setProperty(LAST_SAVE_DIR_KEY, path);
        saveProperties(props);
    }
    
    /**
     * Add a project file to the recent list.
     */
    public void addRecentProject(File file) {
        String path = file.getAbsolutePath();
        
        // Remove if already exists (to move to top)
        recentProjects.remove(path);
        
        // Add to beginning
        recentProjects.add(0, path);
        
        // Keep only MAX_RECENT items
        while (recentProjects.size() > MAX_RECENT) {
            recentProjects.remove(recentProjects.size() - 1);
        }
        
        saveRecentProjects();
    }
    
    /**
     * Get the list of recent project file paths.
     */
    public List<String> getRecentProjects() {
        // Filter out non-existent files
        List<String> existing = new ArrayList<>();
        for (String path : recentProjects) {
            if (new File(path).exists()) {
                existing.add(path);
            }
        }
        return existing;
    }
    
    /**
     * Clear all recent projects.
     */
    public void clearRecentProjects() {
        recentProjects.clear();
        saveRecentProjects();
    }
    
    /**
     * Save the list of currently open projects.
     */
    public void saveOpenProjects(List<File> openFiles) {
        Properties props = loadProperties();
        
        // Clear existing open projects from props
        List<String> keysToRemove = new ArrayList<>();
        for (String key : props.stringPropertyNames()) {
            if (key.startsWith(OPEN_PROJECT_KEY_PREFIX)) {
                keysToRemove.add(key);
            }
        }
        for (String key : keysToRemove) {
            props.remove(key);
        }
        
        // Add new open projects
        for (int i = 0; i < openFiles.size(); i++) {
            props.setProperty(OPEN_PROJECT_KEY_PREFIX + i, openFiles.get(i).getAbsolutePath());
        }
        
        saveProperties(props);
    }
    
    /**
     * Get the list of open project file paths.
     */
    public List<String> getOpenProjects() {
        Properties props = loadProperties();
        List<String> openProjects = new ArrayList<>();
        
        int i = 0;
        while (true) {
            String path = props.getProperty(OPEN_PROJECT_KEY_PREFIX + i);
            if (path == null) {
                break;
            }
            File file = new File(path);
            if (file.exists()) {
                openProjects.add(file.getAbsolutePath());
            }
            i++;
        }
        
        return openProjects;
    }

    private void loadRecentProjects() {
        Properties props = loadProperties();
        recentProjects.clear();
        for (int i = 0; i < MAX_RECENT; i++) {
            String path = props.getProperty(RECENT_KEY_PREFIX + i);
            if (path != null && !path.isEmpty()) {
                recentProjects.add(path);
            }
        }
    }
    
    private void saveRecentProjects() {
        Properties props = loadProperties();
        
        // Clear existing recent keys
        List<String> keysToRemove = new ArrayList<>();
        for (String key : props.stringPropertyNames()) {
            if (key.startsWith(RECENT_KEY_PREFIX)) {
                keysToRemove.add(key);
            }
        }
        for (String key : keysToRemove) {
            props.remove(key);
        }

        for (int i = 0; i < recentProjects.size(); i++) {
            props.setProperty(RECENT_KEY_PREFIX + i, recentProjects.get(i));
        }
        
        saveProperties(props);
    }
    
    /**
     * Get the theme preference ("light" or "dark").
     */
    public String getThemePreference() {
        Properties props = loadProperties();
        return props.getProperty(THEME_KEY, "dark"); // Default to dark
    }
    
    /**
     * Set the theme preference ("light" or "dark").
     */
    public void setThemePreference(String theme) {
        Properties props = loadProperties();
        props.setProperty(THEME_KEY, theme);
        saveProperties(props);
    }

    public void setFrameSize(int width, int height) {
        Properties props = loadProperties();
        props.setProperty(UI_FRAME_WIDTH, String.valueOf(width));
        props.setProperty(UI_FRAME_HEIGHT, String.valueOf(height));
        saveProperties(props);
    }

    public java.awt.Dimension getFrameSize() {
        Properties props = loadProperties();
        String w = props.getProperty(UI_FRAME_WIDTH);
        String h = props.getProperty(UI_FRAME_HEIGHT);
        if (w != null && h != null) {
            try {
                return new java.awt.Dimension(Integer.parseInt(w), Integer.parseInt(h));
            } catch (NumberFormatException e) {
                // Ignore
            }
        }
        return null; // Default
    }

    public void setMainSplitLocation(int location) {
        Properties props = loadProperties();
        props.setProperty(UI_SPLIT_MAIN, String.valueOf(location));
        saveProperties(props);
    }

    public int getMainSplitLocation() {
        Properties props = loadProperties();
        String val = props.getProperty(UI_SPLIT_MAIN);
        if (val != null) {
            try {
                return Integer.parseInt(val);
            } catch (NumberFormatException e) {
                // Ignore
            }
        }
        return -1;
    }

    public void setConsoleSplitLocation(int location) {
        Properties props = loadProperties();
        props.setProperty(UI_SPLIT_CONSOLE, String.valueOf(location));
        saveProperties(props);
    }

    public int getConsoleSplitLocation() {
        Properties props = loadProperties();
        String val = props.getProperty(UI_SPLIT_CONSOLE);
        if (val != null) {
            try {
                return Integer.parseInt(val);
            } catch (NumberFormatException e) {
                // Ignore
            }
        }
        return -1;
    }

    private Properties loadProperties() {
        Properties props = new Properties();
        File prefsFile = new File(PREFS_FILE);
        if (prefsFile.exists()) {
            try (FileInputStream fis = new FileInputStream(prefsFile)) {
                props.load(fis);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return props;
    }

    private void saveProperties(Properties props) {
        try (FileOutputStream fos = new FileOutputStream(PREFS_FILE)) {
            props.store(fos, "Postman Clone Preferences");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
