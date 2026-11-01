package io.gitlab.icestom.mod;

import java.util.HashSet;
import java.util.Set;

public class WellKnownRegistry {

    public static final Set<String> KNOWN_TAGS = new HashSet<>();

    public static final Set<String> KNOWN_REGIONS = new HashSet<>();
    public static final Set<String> KNOWN_TRIGGERS = new HashSet<>();
    public static final Set<String> KNOWN_LOCATIONS = new HashSet<>();

    static {
        KNOWN_REGIONS.add("icestom.reset");
        KNOWN_REGIONS.add("icestom.reset_");
        KNOWN_REGIONS.add("icestom.end");
        KNOWN_REGIONS.add("icestom.teleport_");

        KNOWN_TRIGGERS.add("icestom.reset");
        KNOWN_TRIGGERS.add("icestom.reset_");
        KNOWN_TRIGGERS.add("icestom.end");
        KNOWN_TRIGGERS.add("icestom.teleport_");

        KNOWN_LOCATIONS.add("icestom.reset_");
        KNOWN_LOCATIONS.add("icestom.leaderboard");
        KNOWN_LOCATIONS.add("icestom.podium_");
        KNOWN_LOCATIONS.add("icestom.podium_all");

        KNOWN_TAGS.add("icestom.author");
        KNOWN_TAGS.add("icestom.contributors");
        KNOWN_TAGS.add("icestom.license");
    }
}
