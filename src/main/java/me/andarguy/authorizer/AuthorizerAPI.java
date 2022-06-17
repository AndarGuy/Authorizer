package me.andarguy.authorizer;

import me.andarguy.authorizer.handler.*;
import me.andarguy.authorizer.settings.Messages;
import me.andarguy.cc.common.models.PlayerAccount;
import net.elytrium.java.commons.mc.serialization.Serializer;
import org.slf4j.Logger;

import java.util.UUID;

public class AuthorizerAPI {
    public static Authorizer INSTANCE = Authorizer.getInstance();
    public static Logger LOGGER = Authorizer.getLogger();
    public static Serializer SERIALIZER = Authorizer.getSerializer();

    private final static DatabaseHandler databaseHandler = INSTANCE.getDatabaseHandler();
    private final static ProcessHandler processHandler = INSTANCE.getProcessHandler();
    private final static PremiumHandler premiumHandler = INSTANCE.getPremiumHandler();
    private final static SessionHandler accountHandler = INSTANCE.getAccountHandler();

//    public static boolean isPremium(String name) {
//        return premiumHandler.isPremium(name);
//    }

    public static PlayerAccount getAccount(String name) {
        return INSTANCE.getCoreAPI().getPlayerAccount(name);
    }

    public static PlayerAccount getAccount(UUID uuid) {
        return INSTANCE.getCoreAPI().getPlayerAccount(uuid);
    }

    public static void forceLogin(String name) {
        INSTANCE.getServer().getPlayer(name).ifPresent(value -> INSTANCE.getFactory().passLoginLimbo(value));
    }

    public static void forceLogout(String name) {
        INSTANCE.getServer().getPlayer(name).ifPresent(player -> {
            INSTANCE.getAccountHandler().closeSession(player);
            player.disconnect(Messages.DESTROY_SESSION_SUCCESSFUL.asComponent());
        });
    }

    public static boolean isLogged(String name) {
        return INSTANCE.getAccountHandler().isAuthenticated(name);
    }
}
