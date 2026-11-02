package com.kgroup.kore;

public class KoreFile {
    long id;
    String name;
    String path;
    
    public KoreFile(long id, String name, String path) {
        this.id = id;
        this.name = name;
        this.path = path;
    }
    
    @Override
    public String toString() {
        return this.name;
    }
}
