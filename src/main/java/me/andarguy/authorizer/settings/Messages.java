package me.andarguy.authorizer.settings;

import lombok.Getter;
import lombok.NonNull;
import me.andarguy.authorizer.Authorizer;
import net.kyori.adventure.text.Component;

import java.text.MessageFormat;

public enum Messages {
    RELOAD("reload"),
    RELOAD_FAILED("reload-failed"),
    ERROR_OCCURRED("error-occurred"),
    NON_EXISTING_COMMAND("non-existing-command"),
    DATABASE_ERROR_KICK("database-error-kick"),
    NOT_PLAYER("not-player"),
    NOT_REGISTERED("not-registered"),
    CRACKED_COMMAND("cracked-command"),
    WRONG_PASSWORD("wrong-password"),
    NICKNAME_INVALID_KICK("nickname-invalid-kick"),
    IP_LIMIT_KICK("ip-limit-kick"),
    WRONG_NICKNAME_CASE_KICK("wrong-nickname-case-kick"),
    BOSSBAR("bossbar"),
    TIMES_UP("times-up"),

    LOGIN_PREMIUM("login-premium"),
    LOGIN_FLOODGATE("login-floodgate"),
    LOGIN("login"),
    LOGIN_WRONG_PASSWORD("login-wrong-password"),
    LOGIN_WRONG_PASSWORD_KICK("login-wrong-password-kick"),
    LOGIN_SUCCESSFUL("login-successful"),
    LOGIN_BY_WEB("login-by-web"),
    DIFFERENT_AUTHORIZATION_TYPE("different-authorization-type"),

    REGISTER("register"),
    REGISTER_DIFFERENT_PASSWORDS("register-different-passwords"),
    REGISTER_PASSWORD_TOO_SHORT("register-password-too-short"),
    REGISTER_PASSWORD_TOO_LONG("register-password-too-long"),
    REGISTER_PASSWORD_UNSAFE("register-password-unsafe"),
    REGISTER_SUCCESSFUL("register-successful"),

    UNREGISTER_SUCCESSFUL("unregister-successful"),
    UNREGISTER_USAGE("unregister-usage"),

    PREMIUM_SUCCESSFUL("premium-successful"),
    ALREADY_PREMIUM("already-premium"),
    NOT_PREMIUM("not-premium"),
    PREMIUM_USAGE("premium-usage"),

    EVENT_CANCELLED("event-cancelled"),

    FORCE_UNREGISTER_SUCCESSFUL("force-unregister-successful"),
    FORCE_UNREGISTER_KICK("force-unregister-kick"),
    FORCE_UNREGISTER_NOT_SUCCESSFUL("force-unregister-not-successful"),
    FORCE_UNREGISTER_USAGE("force-unregister-usage"),

    FORCE_LOGIN_SUCCESSFUL("force-login-successful"),
    FORCE_LOGIN_MESSAGE("force-login-message"),
    FORCE_LOGIN_NOT_FOUND("force-login-not-found"),
    FORCE_LOGIN_USAGE("force-login-usage"),

    CHANGE_PASSWORD_SUCCESSFUL("change-password-successful"),
    CHANGE_PASSWORD_USAGE("change-password-usage"),

    FORCE_CHANGE_PASSWORD_SUCCESSFUL("force-change-password-successful"),
    FORCE_CHANGE_PASSWORD_MESSAGE("force-change-password-message"),
    FORCE_CHANGE_PASSWORD_NOT_SUCCESSFUL("force-change-password-not-successful"),
    FORCE_CHANGE_PASSWORD_USAGE("force-change-password-usage"),

    DESTROY_SESSION_SUCCESSFUL("destroy-session-successful"),
    NO_PASSTHROUGH("no-passthrough")
    ;


    @Getter
    private final String key;

    Messages(String key) {
        this.key = "messages." + key;
    }

    /**
     * Add a message to settings map
     *
     * @param message the message to define
     * @param value   the message value
     */
    public static void define(@NonNull Messages message, Object value) {
        Settings.SETTINGS.put(message.key, value);
    }

    public String asString() {
        return asString("<red>Missing message: " + key + "</red>");
    }

    public String asString(@NonNull String def) {
        Object obj = Settings.SETTINGS.get(key);
        return (String) (!(obj instanceof String) ? def : obj);
    }

    public Component asComponent() {
        return Authorizer.getSerializer().deserialize(asString());
    }

    public Component asFormattedComponent(Object... arguments) {
        return Authorizer.getSerializer().deserialize(MessageFormat.format(asString(), arguments));
    }

}
