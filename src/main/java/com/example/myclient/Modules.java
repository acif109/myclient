package com.example.myclient;

import java.util.List;

public final class Modules {
    public static final Module FPS = new Module("FPS", true);
    public static final Module COORDS = new Module("Koordynaty", true);
    public static final Module CPS = new Module("CPS", false);
    public static final Module PING = new Module("Ping", false);
    public static final Module DIRECTION = new Module("Kierunek", false);
    public static final Module CLOCK = new Module("Zegar", false);
    public static final Module ARMOR = new Module("Pancerz", false);
    public static final Module KEYSTROKES = new Module("Klawisze WASD", false);
    public static final Module ZOOM = new Module("Zoom (klawisz C)", true);

    public static final List<Module> ALL = List.of(
            FPS, COORDS, CPS, PING, DIRECTION, CLOCK, ARMOR, KEYSTROKES, ZOOM
    );

    private Modules() {}
}
