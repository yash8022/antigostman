package com.example.antig.swing.service;

import com.example.antig.swing.model.PostmanCollection;
import com.example.antig.swing.model.xml.XmlCollection;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.io.File;
import java.io.IOException;

public class ProjectService {

    private final XmlMapper xmlMapper;

    public ProjectService() {
        this.xmlMapper = new XmlMapper();
        this.xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public void saveProject(PostmanCollection collection, File file) throws IOException {
        if (collection == null) {
            throw new IllegalArgumentException("Cannot save null collection");
        }
        if (file == null) {
            throw new IllegalArgumentException("Cannot save to null file");
        }
        
        try {
            // Convert PostmanCollection to XmlCollection (no parent references)
            XmlCollection xmlCollection = (XmlCollection) NodeConverter.toXmlNode(collection);
            
            if (xmlCollection == null) {
                throw new IOException("Failed to convert collection to XML format");
            }
            
            xmlMapper.writeValue(file, xmlCollection);
        } catch (IOException e) {
            throw new IOException("Failed to save project to " + file.getAbsolutePath() + ": " + e.getMessage(), e);
        }
    }

    public PostmanCollection loadProject(File file) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("Cannot load from null file");
        }
        if (!file.exists()) {
            throw new IOException("File does not exist: " + file.getAbsolutePath());
        }
        if (!file.canRead()) {
            throw new IOException("Cannot read file: " + file.getAbsolutePath());
        }
        
        try {
            // Deserialize to XmlCollection, then convert to PostmanCollection
            XmlCollection xmlCollection = xmlMapper.readValue(file, XmlCollection.class);
            
            if (xmlCollection == null) {
                throw new IOException("Failed to parse XML file - result is null");
            }
            
            PostmanCollection collection = (PostmanCollection) NodeConverter.toPostmanNode(xmlCollection);
            
            if (collection == null) {
                throw new IOException("Failed to convert XML to PostmanCollection");
            }
            
            return collection;
        } catch (IOException e) {
            throw new IOException("Failed to load project from " + file.getAbsolutePath() + ": " + e.getMessage(), e);
        }
    }
}
