package me.andarguy.authorizer.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.permission.Tristate;
import com.velocitypowered.api.proxy.Player;
import me.andarguy.authorizer.Authorizer;
import me.andarguy.authorizer.settings.Messages;
import me.andarguy.authorizer.utils.CryptoUtils;
import me.andarguy.cc.common.models.PlayerAccount;
import me.andarguy.cc.common.models.UserAccount;

public class ChangePasswordCommand implements SimpleCommand {

    private final Authorizer plugin;

    public ChangePasswordCommand(Authorizer plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (source instanceof Player) {
            if (args.length == 2) {

                PlayerAccount playerAccount = this.plugin.getCoreAPI().getPlayerAccount(((Player) source).getUsername());

                if (playerAccount == null) {
                    source.sendMessage(Messages.NOT_REGISTERED.asComponent());
                    return;
                }

                UserAccount userAccount = this.plugin.getCoreAPI().getUserAccount(playerAccount);

                if (!userAccount.getHashedPassword().isEmpty() && !CryptoUtils.checkPassword(args[0], userAccount.getHashedPassword())) {
                    source.sendMessage(Messages.WRONG_PASSWORD.asComponent());
                    return;
                }

                try {
                    userAccount.setHashedPassword(CryptoUtils.generateHash(args[1]));
                    this.plugin.getCoreAPI().updateUserAccount(userAccount);
                    this.plugin.getAccountHandler().cleanup(playerAccount.getName());
                    source.sendMessage(Messages.CHANGE_PASSWORD_SUCCESSFUL.asComponent());
                } catch (Exception e) {
                    source.sendMessage(Messages.ERROR_OCCURRED.asComponent());
                    e.printStackTrace();
                }
            } else {
                source.sendMessage(Messages.CHANGE_PASSWORD_USAGE.asComponent());
            }
        } else {
            source.sendMessage(Messages.NOT_PLAYER.asComponent());
        }
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().getPermissionValue("authorizer.commands.changepassword") == Tristate.TRUE;
    }
}
