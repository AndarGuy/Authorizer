package me.andarguy.authorizer;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;
import me.andarguy.authorizer.command.*;
import me.andarguy.authorizer.configuration.file.YamlConfiguration;
import me.andarguy.authorizer.handler.*;
import me.andarguy.authorizer.listener.AuthListener;
import me.andarguy.authorizer.settings.Messages;
import me.andarguy.authorizer.settings.Settings;
import me.andarguy.authorizer.utils.FileUtils;
import me.andarguy.cc.common.CoreAPI;
import me.andarguy.cc.velocity.CCVelocity;
import net.elytrium.java.commons.mc.serialization.Serializer;
import net.elytrium.java.commons.mc.serialization.Serializers;
import net.elytrium.limboapi.api.Limbo;
import net.elytrium.limboapi.api.LimboFactory;
import net.elytrium.limboapi.api.chunk.Dimension;
import net.elytrium.limboapi.api.chunk.VirtualWorld;
import net.elytrium.limboapi.api.command.LimboCommandMeta;
import net.elytrium.limboapi.api.file.SchematicFile;
import net.elytrium.limboapi.api.file.StructureFile;
import net.elytrium.limboapi.api.file.WorldFile;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

@Plugin(
        id = "authorizer",
        name = "Authorizer",
        version = "0.1",
        url = "https://camel.su/",
        authors = {
                "andarguy",
        },
        dependencies = {
                @Dependency(id = "limboapi"),
                @Dependency(id = "cc")
        }
)
public class Authorizer {
    @Getter
    private static Authorizer instance;
    @Getter
    private static Logger logger;
    @Getter
    private static Serializer serializer;

    @Getter
    private CoreAPI coreAPI;

    @Getter
    private final ProxyServer server;
    private final Path data;
    @Getter
    private final LimboFactory factory;
    @Getter
    private final DatabaseHandler databaseHandler;
    @Getter
    private final ProcessHandler processHandler;
    @Getter
    private final SessionHandler accountHandler;
    @Getter
    private final PremiumHandler premiumHandler;
    @Getter
    private Limbo limbo;

    @Inject
    public Authorizer(Logger logger, ProxyServer server, @DataDirectory Path data) {
        Authorizer.instance = this;
        Authorizer.logger = logger;
        Authorizer.serializer = new Serializer(Objects.requireNonNullElse(Serializers.MINIMESSAGE.getSerializer(), Serializers.PLAIN.getSerializer()));

        this.server = server;
        this.data = data;

        this.factory = (LimboFactory) this.server.getPluginManager().getPlugin("limboapi").flatMap(PluginContainer::getInstance).orElseThrow();

        this.databaseHandler = new DatabaseHandler(this);
        this.processHandler = new ProcessHandler(this);
        this.premiumHandler = new PremiumHandler(this);
        this.accountHandler = new SessionHandler(this);
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        System.setProperty("com.j256.simplelogging.level", "ERROR");
        this.reload();
    }

    public void reload() {

        if (!setupSettings()) {
            this.server.shutdown();
            return;
        }

        logger.info("Settings module loaded...");

        if (!setupLimbo()) {
            this.server.shutdown();
            return;
        }

        logger.info("Limbo module loaded...");

        if (!this.databaseHandler.reload()) {
            this.server.shutdown();
            return;
        }

        this.coreAPI = CCVelocity.getApi();

        logger.info("Database module loaded...");

        this.premiumHandler.reload();

        logger.info("Premium module loaded...");

        logger.info("TaskEvent module loaded...");

        if (!setupCommands()) {
            this.server.shutdown();
            return;
        }

        logger.info("Commands module loaded...");

        if (!registerEvents()) {
            this.server.shutdown();
            return;
        }

        logger.info("Events module loaded...");


    }

    private boolean registerEvents() {
        EventManager eventManager = this.server.getEventManager();
        eventManager.unregisterListeners(this);
        eventManager.register(this, new AuthListener(this));

        return true;
    }


    private boolean setupSettings() {
        File configurationFile = new File(data.toFile(), "config.yml");
        if (!configurationFile.exists() && !FileUtils.copyFromJar("me/andarguy/authorizer/resources/config.yml", configurationFile)) {
            logger.error("Failed to create 'config.yml' file.");
            return false;
        }
        YamlConfiguration settings = YamlConfiguration.loadConfiguration(configurationFile);

        Settings.clear();
        for (Settings setting : Settings.values()) {
            Settings.define(setting, settings.get(setting.getKey()));
        }

        File messagesFile = new File(this.data.toFile(), "messages.yml");
        if (!messagesFile.exists() && !FileUtils.copyFromJar("me/andarguy/authorizer/resources/messages.yml", messagesFile)) {
            logger.error("§cFailed to create language file.");
            return false;
        }

        YamlConfiguration messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        for (Messages message : Messages.values()) {
            Messages.define(message, messagesConfig.get(message.getKey()));
        }

        return true;
    }

    private boolean setupLimbo() {
        VirtualWorld world = this.factory.createVirtualWorld(
                Dimension.valueOf(Settings.SETTINGS_WORLD_DIMENSION.asString()),
                Settings.SETTINGS_AUTH_COORDS_X.asFloat(), Settings.SETTINGS_AUTH_COORDS_Y.asFloat(), Settings.SETTINGS_AUTH_COORDS_Y.asFloat(),
                Settings.SETTINGS_AUTH_COORDS_YAW.asFloat(), Settings.SETTINGS_AUTH_COORDS_PITCH.asFloat()
        );

        if (Settings.SETTINGS_LOAD_WORLD.asBoolean()) {
            try {
                Path path = this.data.resolve(Settings.SETTINGS_WORLD_FILE_PATH.asString());
                WorldFile worldFile;
                switch (Settings.SETTINGS_WORLD_FILE_TYPE.asString()) {
                    case "schematic": {
                        worldFile = new SchematicFile(path);
                        break;
                    }
                    case "structure": {
                        worldFile = new StructureFile(path);
                        break;
                    }
                    default: {
                        logger.error("Incorrect world file type.");
                        return false;
                    }
                }

                worldFile.toWorld(this.factory, world, 0, 0, 0);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        this.limbo = this.factory
                .createLimbo(world)
                .setName("Authorization")
                .setWorldTime(Settings.SETTINGS_WORLD_TICKS.asLong())
                .registerCommand(new LimboCommandMeta(List.of("login", "l")))
        ;

        return true;
    }

    private boolean setupCommands() {
        CommandManager manager = this.server.getCommandManager();
        manager.unregister("forceunregister");
        manager.unregister("changepassword");
        manager.unregister("forcechangepassword");
        manager.unregister("forcelogin");
        manager.unregister("logout");

        manager.register("forceunregister", new ForceUnregisterCommand(this));
        manager.register("forcechangepassword", new ForceChangePasswordCommand(this));
        manager.register("changepassword", new ChangePasswordCommand(this));
        manager.register("forcelogin", new ForceLoginCommand(this));
        manager.register("logout", new LogoutCommand(this), "exit");

        return true;
    }
}
