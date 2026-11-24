package com.example.myclient;

import java.util.List;

public final class Modules {
    public static final String INFO = "Informacje";
    public static final String VISUAL = "Wizualne";

    public static final Module FPS = new Module("FPS", true, INFO, 5, 5);
    public static final Module COORDS = new Module("Koordynaty", true, INFO, 5, 24);
    public static final Module CPS = new Module("CPS", false, INFO, 5, 43);
    public static final Module PING = new Module("Ping", false, INFO, 5, 62);
    public static final Module DIRECTION = new Module("Kierunek", false, INFO, 5, 81);
    public static final Module CLOCK = new Module("Zegar", false, INFO, 5, 100);
    public static final Module ARMOR = new Module("Pancerz", false, VISUAL, 5, 124);
    public static final Module KEYSTROKES = new Module("Klawisze", false, VISUAL, 5, 200);
    public static final Module ZOOM = new Module("Zoom (C)", true, VISUAL);

    public static final List<Module> ALL = List.of(
            FPS, COORDS, CPS, PING, DIRECTION, CLOCK, ARMOR, KEYSTROKES, ZOOM
    );

    private Modules() {}
}
