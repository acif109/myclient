package com.example.myclient;

import java.util.List;

public final class Modules {
    public static final String INFO = "Informacje";
    public static final String VISUAL = "Wizualne";

    public static final Module FPS = new Module("FPS", true, INFO);
    public static final Module COORDS = new Module("Koordynaty", true, INFO);
    public static final Module CPS = new Module("CPS", false, INFO);
    public static final Module PING = new Module("Ping", false, INFO);
    public static final Module DIRECTION = new Module("Kierunek", false, INFO);
    public static final Module CLOCK = new Module("Zegar", false, INFO);
    public static final Module ARMOR = new Module("Pancerz", false, VISUAL);
    public static final Module KEYSTROKES = new Module("Klawisze", false, VISUAL);
    public static final Module ZOOM = new Module("Zoom (C)", true, VISUAL);

    public static final List<Module> ALL = List.of(
            FPS, COORDS, CPS, PING, DIRECTION, CLOCK, ARMOR, KEYSTROKES, ZOOM
    );

    private Modules() {}
}
