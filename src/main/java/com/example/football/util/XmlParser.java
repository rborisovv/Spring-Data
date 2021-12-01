package com.example.football.util;

import javax.xml.bind.JAXBException;
import java.io.FileNotFoundException;

public interface XmlParser {
    public <T> T readFromFile(String path, Class<T> tClass) throws JAXBException, FileNotFoundException;

    public <T> void writeToFile(String filePath, T entity) throws JAXBException;
}
