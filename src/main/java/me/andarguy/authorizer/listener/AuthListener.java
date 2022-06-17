package me.andarguy.authorizer.listener;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.player.GameProfileRequestEvent;
import me.andarguy.authorizer.Authorizer;
import net.elytrium.limboapi.api.event.LoginLimboRegisterEvent;

public class AuthListener {

    private final Authorizer plugin;

    public AuthListener(Authorizer plugin) {
        this.plugin = plugin;
    }

    @Subscribe
    public void onPreLoginEvent(PreLoginEvent event) {
        if (!event.getResult().isForceOfflineMode()) {
            if (!this.plugin.getPremiumHandler().isPremium(event.getUsername())) {
                event.setResult(PreLoginEvent.PreLoginComponentResult.forceOfflineMode());
            } else {
                event.setResult(PreLoginEvent.PreLoginComponentResult.forceOnlineMode());
            }
        }
    }

    @Subscribe
    public void onPostLogin(PostLoginEvent event) {

    }

    @Subscribe
    public void onLoginLimboRegister(LoginLimboRegisterEvent event) {
        event.addCallback(() -> this.plugin.getProcessHandler().processJoin(event.getPlayer()));
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onGameProfileRequest(GameProfileRequestEvent event) {

    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        this.plugin.getAccountHandler().cleanup(event.getPlayer().getUsername());
    }
}
