package me.andarguy.authorizer.handler;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import me.andarguy.authorizer.Authorizer;
import me.andarguy.authorizer.settings.Settings;
import me.andarguy.cc.common.models.PlayerAccount;
import me.andarguy.cc.common.models.UserAccount;
import me.andarguy.cc.thirdparty.com.j256.ormlite.stmt.QueryBuilder;

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
        if (Settings.SETTINGS_FORCE_OFFLINE_MODE.asBoolean()) {
            return false;
        } else {
            try {
                return this.isPremiumExternal(name);
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
