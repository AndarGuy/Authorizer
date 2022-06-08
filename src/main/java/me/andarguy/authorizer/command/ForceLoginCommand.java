package me.andarguy.authorizer.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import me.andarguy.authorizer.Authorizer;
import me.andarguy.authorizer.settings.Messages;
import net.elytrium.java.commons.mc.velocity.commands.SuggestUtils;

import java.util.List;
import java.util.Optional;

public class ForceLoginCommand implements SimpleCommand {

    private final Authorizer plugin;

    public ForceLoginCommand(Authorizer plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        return SuggestUtils.suggestPlayers(this.plugin.getServer(), invocation.arguments(), 0);
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (args.length == 1) {
            String playerNick = args[0];

            Optional<Player> player = this.plugin.getServer().getPlayer(playerNick);
            if (player.isPresent()) {
                if (!this.plugin.getAccountHandler().isAuthenticated(playerNick)) {
                    player.get().sendMessage(Messages.FORCE_LOGIN_MESSAGE.asComponent());
                }
                this.plugin.getProcessHandler().performLogin(player.get());
                source.sendMessage(Messages.FORCE_LOGIN_SUCCESSFUL.asComponent());
            } else source.sendMessage(Messages.FORCE_LOGIN_NOT_FOUND.asComponent());

        } else {
            source.sendMessage(Messages.FORCE_LOGIN_USAGE.asComponent());
        }
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("authorizer.admin.forcelogin");
    }
}
