package com.gabriel.tensuranewworld.data;

import java.util.Arrays;
import java.util.Optional;

public enum CharacterAttribute {
    STRENGTH("strength", "Forca"),
    VITALITY("vitality", "Vitalidade"),
    AGILITY("agility", "Agilidade"),
    INTELLIGENCE("intelligence", "Inteligencia"),
    WILL("will", "Vontade");

    private final String id;
    private final String displayName;

    CharacterAttribute(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    public static Optional<CharacterAttribute> byId(String id) {
        return Arrays.stream(values()).filter(attribute -> attribute.id.equalsIgnoreCase(id)).findFirst();
    }
}
