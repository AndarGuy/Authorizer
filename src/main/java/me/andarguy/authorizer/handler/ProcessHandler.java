package me.andarguy.authorizer.handler;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.velocitypowered.api.proxy.Player;
import lombok.Getter;
import me.andarguy.authorizer.Authorizer;
import me.andarguy.authorizer.event.AuthenticateEvent;
import me.andarguy.authorizer.event.AwaitingAuthorizationEvent;
import me.andarguy.authorizer.model.Account;
import me.andarguy.authorizer.settings.Messages;
import me.andarguy.authorizer.utils.StringUtils;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ProcessHandler extends Handler {

    @Getter
    private final Cache<String, AuthorizationSessionHandler> sessions = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES).build();

    public ProcessHandler(Authorizer plugin) {
        super(plugin);
    }

    public void processJoin(Player player) {
        String nickname = player.getUsername();

        // check for valid nickname

        if (!StringUtils.validate(nickname)) {
            player.disconnect(Messages.NICKNAME_INVALID_KICK.asComponent());
            return;
        }

        Account account = this.plugin.getAccountHandler().getAccount(nickname);

        // check for registration to access

        if (account == null) {
            this.plugin.getAccountHandler().cleanup(nickname);
            player.disconnect(Messages.NO_PASSTHROUGH.asComponent());
            return;
        }

        // check for session

        if (this.plugin.getAccountHandler().hasSession(player)) {
            this.forceLogin(player);
            return;
        }

        AuthorizationSessionHandler sessionHandler = new AuthorizationSessionHandler(this.plugin, player);

        // run authorization session process

        this.plugin.getServer().getEventManager().fire(new AwaitingAuthorizationEvent(sessionHandler)).thenAcceptAsync((event) -> {
            if (!event.isCancelled()) {
                this.plugin.getLimbo().spawnPlayer(player, event.getAuthorizationHandler());
                sessions.put(player.getUsername().toLowerCase(Locale.ROOT), sessionHandler);
            }
            else player.disconnect(Messages.EVENT_CANCELLED.asComponent());
        });
    }

    public void forceLogin(Player player) {
        Account account = this.plugin.getAccountHandler().getAccount(player.getUsername());
        if (account == null) return;
            performLogin(player);
    }

    public void performLogin(Player player) {
        String toLower = player.getUsername().toLowerCase(Locale.ROOT);
        Account account = this.plugin.getAccountHandler().getAccount(toLower);
        if (!this.plugin.getAccountHandler().isAuthenticated(toLower)) {
            AuthorizationSessionHandler sessionHandler = sessions.getIfPresent(toLower);
            if (sessionHandler != null) {
                sessionHandler.getLimboPlayer().disconnect();
            } else {
                this.plugin.getFactory().passLoginLimbo(player);
            }
            this.plugin.getAccountHandler().openSession(player);
            this.plugin.getAccountHandler().setAuthenticated(toLower);
            this.plugin.getServer().getEventManager().fireAndForget(new AuthenticateEvent(account));
        }
    }

    @Override
    public boolean reload() {
        return true;
    }
}