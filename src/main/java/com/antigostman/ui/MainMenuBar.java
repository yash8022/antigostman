package com.antigostman.ui;

import javax.swing.*;
import java.io.File;

public class MainMenuBar extends JMenuBar {
    private final AntigostmanController controller;
    private JMenu recentProjectsMenu;

    public interface AntigostmanController {
        void saveProject();
        void loadProject();
        void newProject();
        void importPostmanCollection();
        void toggleTheme();
        void toggleConsole();
        void clearConsole();
        void openLogFile();
        void openAboutPage();
        void saveAllProjects();
    }

    public MainMenuBar(AntigostmanController controller) {
        this.controller = controller;
        initComponents();
    }

    private void initComponents() {
        JMenu fileMenu = new JMenu("File");

        JMenuItem newProjectItem = new JMenuItem("New Project");
        newProjectItem.addActionListener(e -> controller.newProject());
        fileMenu.add(newProjectItem);

        JMenuItem saveItem = new JMenuItem("Save Project");
        saveItem.addActionListener(e -> controller.saveProject());
        fileMenu.add(saveItem);

        JMenuItem loadItem = new JMenuItem("Load Project");
        loadItem.addActionListener(e -> controller.loadProject());
        fileMenu.add(loadItem);

        JMenuItem importPostmanItem = new JMenuItem("Import Postman Collection");
        importPostmanItem.addActionListener(e -> controller.importPostmanCollection());
        fileMenu.add(importPostmanItem);

        fileMenu.addSeparator();

        recentProjectsMenu = new JMenu("Recent Projects");
        fileMenu.add(recentProjectsMenu);

        fileMenu.addSeparator();

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> {
            controller.saveAllProjects();
            System.exit(0);
        });
        fileMenu.add(exitItem);

        add(fileMenu);

        JMenu viewMenu = new JMenu("View");
        JMenuItem toggleThemeItem = new JMenuItem("Toggle Theme (Light/Dark)");
        toggleThemeItem.addActionListener(e -> controller.toggleTheme());
        viewMenu.add(toggleThemeItem);

        viewMenu.addSeparator();
        JMenuItem toggleConsoleItem = new JMenuItem("Toggle Console");
        toggleConsoleItem.addActionListener(e -> controller.toggleConsole());
        viewMenu.add(toggleConsoleItem);

        JMenuItem clearConsoleItem = new JMenuItem("Clear Console");
        clearConsoleItem.addActionListener(e -> controller.clearConsole());
        viewMenu.add(clearConsoleItem);

        viewMenu.addSeparator();
        JMenuItem openLogItem = new JMenuItem("Open Log File");
        openLogItem.addActionListener(e -> controller.openLogFile());
        viewMenu.add(openLogItem);

        add(viewMenu);

        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About Antigostman");
        aboutItem.addActionListener(e -> controller.openAboutPage());
        helpMenu.add(aboutItem);

        add(helpMenu);
    }

    public JMenu getRecentProjectsMenu() {
        return recentProjectsMenu;
    }
}
