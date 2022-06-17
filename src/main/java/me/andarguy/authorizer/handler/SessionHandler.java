package me.andarguy.authorizer.handler;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.velocitypowered.api.proxy.Player;
import lombok.NonNull;
import me.andarguy.authorizer.Authorizer;

import java.util.HashSet;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class SessionHandler extends Handler {

    private final HashSet<String> logged = new HashSet<>();

    private final Cache<String, Boolean> sessionCache = CacheBuilder.newBuilder()
            .expireAfterWrite(3, TimeUnit.DAYS)
            .build();

    public SessionHandler(Authorizer plugin) {
        super(plugin);
    }

    public void cleanup(@NonNull String name) {
        String toLower = name.toLowerCase();
        logged.remove(toLower);
    }

    public void closeSession(Player player) {
        String username = player.getUsername().toLowerCase(Locale.ROOT);
        sessionCache.invalidate(username + ":" + player.getRemoteAddress().getAddress());
    }

    public void openSession(Player player) {
        String username = player.getUsername().toLowerCase(Locale.ROOT);
        sessionCache.put(username + ":" + player.getRemoteAddress().getAddress(), true);
    }

    public boolean hasSession(Player player) {
        String username = player.getUsername().toLowerCase(Locale.ROOT);
        Boolean has = sessionCache.getIfPresent(username + ":" + player.getRemoteAddress().getAddress());
        if (has != null) return has;
        return false;
    }

    public void setAuthenticated(@NonNull String name) {
        logged.add(name.toLowerCase());
    }

    public boolean isAuthenticated(@NonNull String name) {
        return logged.contains(name.toLowerCase());
    }

    @Override
    public boolean reload() {
        return false;
    }
}
