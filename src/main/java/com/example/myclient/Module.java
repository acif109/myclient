package com.example.myclient;

public class Module {
    public final String name;
    public boolean enabled;

    public Module(String name, boolean enabled) {
        this.name = name;
        this.enabled = enabled;
    }
}
