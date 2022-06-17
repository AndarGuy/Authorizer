package me.andarguy.authorizer.handler;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.scheduler.ScheduledTask;
import lombok.Getter;
import me.andarguy.authorizer.Authorizer;
import me.andarguy.authorizer.event.LoginEvent;
import me.andarguy.authorizer.model.AuthorizationRequest;
import me.andarguy.authorizer.settings.Messages;
import me.andarguy.authorizer.settings.Settings;
import me.andarguy.cc.common.models.PlayerAccount;
import me.andarguy.cc.common.models.UserAccount;
import net.elytrium.limboapi.api.Limbo;
import net.elytrium.limboapi.api.LimboSessionHandler;
import net.elytrium.limboapi.api.player.LimboPlayer;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class AuthorizationSessionHandler extends Handler implements LimboSessionHandler {

    @Getter
    private final Player proxyPlayer;
    private final PlayerAccount playerAccount;
    private final UserAccount userAccount;
    private final List<ScheduledTask> tasks = new ArrayList<>();
    @Getter
    private LimboPlayer limboPlayer;
    private BossBar bossBar;

    private UserAccount.AuthorizationType authorizationType;

    private int attempts;
    private AuthorizationRequest authorizationRequest;

    public AuthorizationSessionHandler(Authorizer plugin, Player proxyPlayer) {
        super(plugin);
        this.proxyPlayer = proxyPlayer;
        this.playerAccount = plugin.getCoreAPI().getPlayerAccount(proxyPlayer.getUsername());
        this.userAccount = plugin.getCoreAPI().getUserAccount(this.playerAccount);
        this.attempts = Settings.SETTINGS_LOGIN_ATTEMPTS.asInt();
    }

    @Override
    public void onSpawn(Limbo server, LimboPlayer player) {
        this.limboPlayer = player;
        this.limboPlayer.disableFalling();

        if (!this.proxyPlayer.getUsername().equals(this.playerAccount.getName())) {
            this.proxyPlayer.disconnect(Messages.WRONG_NICKNAME_CASE_KICK.asComponent());
            return;
        }

        long loginTime = System.currentTimeMillis();
        int authTime = Settings.SETTINGS_AUTH_TIME.asInt();

        if (Settings.SETTINGS_ENABLE_BOSSBAR.asBoolean()) {
            float multiplier = 1000.0F / authTime;
            this.bossBar = BossBar.bossBar(Component.empty(), 0, BossBar.Color.valueOf(Settings.SETTINGS_BOSSBAR_COLOR.asString()), BossBar.Overlay.valueOf(Settings.SETTINGS_BOSSBAR_OVERLAY.asString()));
            tasks.add(
                    this.plugin.getServer().getScheduler().buildTask(this.plugin, () -> {
                        float timeLeft = (authTime - (System.currentTimeMillis() - loginTime)) / 1000.0F;
                        this.bossBar.name(Messages.BOSSBAR.asFormattedComponent((int) timeLeft));
                        // It's possible, that the progress value can overcome 1, e.g. 1.0000001.
                        this.bossBar.progress(Math.max(0f, Math.min(1f, timeLeft * multiplier)));
                    }).repeat(1, TimeUnit.SECONDS).schedule());
            this.proxyPlayer.showBossBar(this.bossBar);
        }

        this.authorizationType = this.userAccount.getAuthorizationType();

        System.out.println(this.authorizationType);

        if (this.authorizationType == UserAccount.AuthorizationType.INTERACTIVE) {
            try {
                this.authorizationRequest = new AuthorizationRequest(
                        UUID.randomUUID().toString(),
                        this.playerAccount.getId(),
                        System.currentTimeMillis(),
                        "default",
                        this.proxyPlayer.getRemoteAddress().getAddress().toString(),
                        AuthorizationRequest.Status.CREATED
                );
                this.plugin.getDatabaseHandler().getRequestDao().create(this.authorizationRequest);
                this.proxyPlayer.sendMessage(Messages.LOGIN_BY_WEB.asFormattedComponent(this.authorizationRequest.getId()));
                tasks.add(
                        this.plugin.getServer().getScheduler().buildTask(this.plugin, () -> {
                            try {
                                this.authorizationRequest.setStatus(this.plugin.getDatabaseHandler().getRequestDao().queryForSameId(this.authorizationRequest).getStatus());
                                if (this.authorizationRequest.getStatus() == AuthorizationRequest.Status.AUTHORIZED) {
                                    this.plugin.getProcessHandler().performLogin(this.proxyPlayer);
                                } else if (this.authorizationRequest.getStatus() == AuthorizationRequest.Status.BLOCKED) {
                                    this.proxyPlayer.disconnect(Component.empty());
                                }
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }).repeat(3, TimeUnit.SECONDS).schedule());
            } catch (SQLException e) {
                this.proxyPlayer.sendMessage(Messages.ERROR_OCCURRED.asComponent());
                this.authorizationType = UserAccount.AuthorizationType.STANDARD;
                e.printStackTrace();
            }
        }

        if (this.authorizationType == UserAccount.AuthorizationType.STANDARD) {
            this.proxyPlayer.sendMessage(Messages.LOGIN.asComponent());
        }

        tasks.add(
                this.plugin.getServer().getScheduler().buildTask(this.plugin, () -> {
                    if (System.currentTimeMillis() - loginTime > authTime) {
                        this.proxyPlayer.disconnect(Messages.TIMES_UP.asComponent());
                    }
                }).repeat(1, TimeUnit.SECONDS).schedule());
    }

    @Override
    public void onChat(String message) {
        if (!message.startsWith("/")) return;
        String[] args = message.split(" ");
        Command command = Command.parse(args[0]);
        switch (command) {
            case LOGIN:
                if (this.authorizationType != UserAccount.AuthorizationType.STANDARD) {
                    this.proxyPlayer.sendMessage(Messages.DIFFERENT_AUTHORIZATION_TYPE.asComponent());
                    break;
                }
                if (args.length != 2) break;
                String password = args[1];
                if (!this.userAccount.checkPassword(password)) {
                    this.attempts--;
                    if (this.attempts <= 0)
                        this.proxyPlayer.disconnect(Messages.LOGIN_WRONG_PASSWORD_KICK.asComponent());
                    else this.proxyPlayer.sendMessage(Messages.WRONG_PASSWORD.asFormattedComponent(this.attempts));
                    break;
                }
                this.plugin.getServer().getEventManager().fire(new LoginEvent(this.playerAccount)).thenAcceptAsync((loginEvent -> {
                    if (!loginEvent.isCancelled()) this.plugin.getProcessHandler().performLogin(this.proxyPlayer);
                    else this.proxyPlayer.sendMessage(Messages.EVENT_CANCELLED.asComponent());
                }));
                break;
            case REGISTER:
            case INVALID:
                this.proxyPlayer.sendMessage(Messages.NON_EXISTING_COMMAND.asComponent());
                break;
        }
    }

    public void cleanTasks() {
        for (ScheduledTask task : tasks) task.cancel();
    }

    @Override
    public void onDisconnect() {
        this.cleanTasks();
        try {
            plugin.getDatabaseHandler().getRequestDao().delete(this.authorizationRequest);
        } catch (Exception ignored) {
        }
        this.proxyPlayer.hideBossBar(this.bossBar);
        this.plugin.getProcessHandler().getSessions().invalidate(this.proxyPlayer.getUsername().toLowerCase(Locale.ROOT));
    }

    @Override
    public boolean reload() {
        return true;
    }


    private enum Command {
        INVALID,
        REGISTER,
        LOGIN;

        static Command parse(String command) {
            if (List.of("/register", "/reg", "/r").contains(command)) {
                return Command.REGISTER;
            } else if (List.of("/login", "/log", "/l").contains(command)) {
                return Command.LOGIN;
            } else {
                return Command.INVALID;
            }
        }
    }
}
