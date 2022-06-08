package me.andarguy.authorizer.handler;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import me.andarguy.authorizer.Authorizer;
import me.andarguy.authorizer.model.Account;
import me.andarguy.authorizer.settings.Settings;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class PremiumHandler extends Handler implements Loadable {
    private final HttpClient client;
    private final Cache<String, Boolean> cache;

    public PremiumHandler(Authorizer plugin) {
        super(plugin);
        this.client = HttpClient.newHttpClient();
        this.cache = CacheBuilder.newBuilder()
                .expireAfterWrite(8, TimeUnit.HOURS)
                .build();

    }


    public boolean isPremiumExternal(String name) {
        String username = name.toLowerCase(Locale.ROOT);
        try {
            return this.cache.get(name, () -> {
                try {
                    int statusCode = this.client.send(
                            HttpRequest.newBuilder()
                                    .uri(URI.create(String.format("https://api.mojang.com/users/profiles/minecraft/%s", URLEncoder.encode(username, StandardCharsets.UTF_8))))
                                    .build(),
                            HttpResponse.BodyHandlers.ofString()
                    ).statusCode();

                    return statusCode == 200;
                } catch (IOException | InterruptedException e) {
                    Authorizer.getLogger().error("Unable to authenticate with Mojang.", e);
                    return false;
                }
            });
        } catch (ExecutionException e) {
            Authorizer.getLogger().error("Unable to authenticate with Mojang.", e);
            return false;
        }
    }

    public boolean isPremium(String name) {
        DatabaseHandler handler = this.plugin.getDatabaseHandler();
        if (Settings.SETTINGS_FORCE_OFFLINE_MODE.asBoolean()) {
            return false;
        } else {
            try {
                if (this.isPremiumExternal(name)) {
                    QueryBuilder<Account, String> premiumRegisteredQuery = handler.getPlayerDao().queryBuilder();
                    premiumRegisteredQuery.where()
                            .eq("id", name.toLowerCase(Locale.ROOT))
                            .and()
                            .ne("password", "");
                    premiumRegisteredQuery.setCountOf(true);

                    QueryBuilder<Account, String> premiumUnregisteredQuery = handler.getPlayerDao().queryBuilder();
                    premiumUnregisteredQuery.where()
                            .eq("id", name.toLowerCase(Locale.ROOT))
                            .and()
                            .eq("password", "");
                    premiumUnregisteredQuery.setCountOf(true);

                    if (Settings.SETTINGS_ONLINE_MODE_NEED_AUTH.asBoolean()) {
                        return handler.getPlayerDao().countOf(premiumRegisteredQuery.prepare()) == 0 && handler.getPlayerDao().countOf(premiumUnregisteredQuery.prepare()) != 0;
                    } else {
                        return handler.getPlayerDao().countOf(premiumRegisteredQuery.prepare()) == 0;
                    }
                } else {
                    return false;
                }
            } catch (Exception e) {
                Authorizer.getLogger().error("Unable to authenticate with Mojang.", e);
                return false;
            }
        }
    }

    @Override
    public boolean reload() {
        this.cache.cleanUp();
        return true;
    }
}
