package me.andarguy.authorizer.handler;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.velocitypowered.api.proxy.Player;
import lombok.NonNull;
import me.andarguy.authorizer.Authorizer;
import me.andarguy.authorizer.model.Account;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class AccountHandler extends Handler {

    private final HashSet<String> logged = new HashSet<>();

    private final Cache<String, Account> accountCache = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .build();

    private final Cache<String, Boolean> sessionCache = CacheBuilder.newBuilder()
            .expireAfterWrite(3, TimeUnit.DAYS)
            .build();

    public AccountHandler(Authorizer plugin) {
        super(plugin);
    }

    public void cleanup(@NonNull String name) {
        String toLower = name.toLowerCase();
        logged.remove(toLower);
        accountCache.invalidate(toLower);
    }

    private Account getAccount(String value, String field) {
        Account account = accountCache.getIfPresent(value);
        if (account == null) {
            try {
                List<Account> playerList = this.plugin.getDatabaseHandler().getPlayerDao().queryForEq(field, value);
                account = playerList.get(0);
            } catch (Exception e) {
                return null;
            }
            accountCache.put(value, account);
        }
        return account;
    }

    public Account getAccount(String name) {
        name = name.toLowerCase(Locale.ROOT);
        return getAccount(name, "id");
    }

    public Account getAccount(Player player) {
        return getAccount(player.getUsername());
    }

    public Account getAccount(UUID uuid) {
        return getAccount(uuid.toString(), "uuid");
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
