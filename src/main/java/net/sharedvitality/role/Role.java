package net.sharedvitality.role;

public enum Role {
    NONE("None", "Нет роли"),
    GUARDIAN("Guardian", "Страж"),
    APOTHECARY("Apothecary", "Алхимик / Повар"),
    DELVER("Delver", "Геолог"),
    SCOUT("Scout", "Следопыт");

    private final String id;
    private final String displayName;

    Role(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Role fromString(String name) {
        for (Role role : values()) {
            if (role.name().equalsIgnoreCase(name) || role.id.equalsIgnoreCase(name)) {
                return role;
            }
        }
        return NONE;
    }
}
