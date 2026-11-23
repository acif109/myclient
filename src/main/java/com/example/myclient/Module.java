package com.example.myclient;

public class Module {
    public final String name;
    public final String category;
    public boolean enabled;
    public boolean favorite = false;

    public Module(String name, boolean enabled, String category) {
        this.name = name;
        this.enabled = enabled;
        this.category = category;
    }
}
