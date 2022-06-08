package me.andarguy.authorizer.settings;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;

@RequiredArgsConstructor
public enum Settings {
    SETTINGS_AUTH_TIME("settings.auth-time", 60000),
    SETTINGS_SESSION_DURATION("settings.session-duration", 604800000),
    SETTINGS_ENABLE_BOSSBAR("settings.enable-bossbar", true),
    SETTINGS_BOSSBAR_COLOR("settings.bossbar-color", "WHITE"),
    SETTINGS_BOSSBAR_OVERLAY("settings.bossbar-overlay", "PROGRESS"),

    SETTINGS_MIN_PASSWORD_LENGTH("settings.min-password-length", 4),
    SETTINGS_MAX_PASSWORD_LENGTH("settings.max-password-length", 32),
    SETTINGS_ONLINE_MODE_NEED_AUTH("settings.online-mode-need-auth", true),
    SETTINGS_FLOODGATE_NEED_AUTH("settings.floodgate-need-auth", true),
    SETTINGS_FORCE_OFFLINE_MODE("settings.force-offline-mode", true),
    SETTINGS_FORCE_OFFLINE_UUID("settings.force-offline-uuid", true),
    SETTINGS_SAVE_UUID("settings.save-uuid", true),
    SETTINGS_REGISTER_NEED_REPEAT_PASSWORD("settings.register-need-repeat-password", true),
    SETTINGS_CHANGE_PASSWORD_NEED_OLD_PASSWORD("settings.change-password-need-old-password", true),
    SETTINGS_LOGIN_ATTEMPTS("settings.login-attempts", 3),

    SETTINGS_WORLD_DIMENSION("settings.world.dimension", "OVERWORLD"),
    SETTINGS_WORLD_TICKS("settings.world.ticks", 1000L),
    SETTINGS_LOAD_WORLD("settings.world.load", false),
    SETTINGS_WORLD_FILE_TYPE("settings.world.world-file-type", "structure"),
    SETTINGS_WORLD_FILE_PATH("settings.world.world-file-path", "house.nbt"),

    SETTINGS_AUTH_COORDS_X("settings.auth-coords.x", 0f),
    SETTINGS_AUTH_COORDS_Y("settings.auth-coords.y", 0f),
    SETTINGS_AUTH_COORDS_Z("settings.auth-coords.z", 0f),
    SETTINGS_AUTH_COORDS_YAW("settings.auth-coords.yaw", 0f),
    SETTINGS_AUTH_COORDS_PITCH("settings.auth-coords.pitch", 0f),

    DATABASE_STORAGE_TYPE("database.storage-type", "mysql"),
    DATABASE_HOSTNAME("database.hostname", "0.0.0.0:3306"),
    DATABASE_USER("database.user", "USER"),
    DATABASE_PASSWORD("database.password", "PASSWORD"),
    DATABASE_DATABASE("database.database", "DATABASE"),
    DATABASE_CONNECTION_PARAMETERS("database.connection-parameters", "?autoReconnect=true&initialTimeout=1&useSSL=false");

    static final HashMap<String, Object> SETTINGS = new HashMap<>();

    @Getter
    private final String key;
    private final Object def;

    /**
     * Add a setting to map
     *
     * @param setting the setting to define
     * @param value   the setting value
     */
    public static void define(@NonNull Settings setting, Object value) {
        SETTINGS.put(setting.key, value);
    }

    /**
     * Clears the settings map
     */
    public static void clear() {
        SETTINGS.clear();
    }

    public String asString() {
        return get(String.class);
    }

    public int asInt() {
        return get(Integer.class);
    }

    public boolean asBoolean() {
        return get(Boolean.class);
    }

    public float asFloat() {return get(Float.class); }

    public long asLong() {return get(Long.class); }

    @SuppressWarnings("unchecked")
    private <T> T get(@NonNull Class<T> clasz) {
        if (def != null && !clasz.isAssignableFrom(def.getClass())) {
            throw new ClassCastException("Setting " + key + " is not assignable to " + clasz.getCanonicalName() + "!");
        }
        Object obj = SETTINGS.get(key);
        return (T) (obj == null || !clasz.isAssignableFrom(obj.getClass()) ? def : obj);
    }

}
