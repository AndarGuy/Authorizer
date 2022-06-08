package me.andarguy.authorizer.command;

import com.j256.ormlite.stmt.UpdateBuilder;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.permission.Tristate;
import com.velocitypowered.api.proxy.Player;
import me.andarguy.authorizer.Authorizer;
import me.andarguy.authorizer.model.Account;
import me.andarguy.authorizer.settings.Messages;
import me.andarguy.authorizer.utils.CryptoUtils;

import java.sql.SQLException;

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

                Account player = this.plugin.getAccountHandler().getAccount(((Player) source));
                if (player == null) {
                    source.sendMessage(Messages.NOT_REGISTERED.asComponent());
                    return;
                } else if (!player.getHashedPassword().isEmpty() && !CryptoUtils.checkPassword(args[0], player.getHashedPassword())) {
                    source.sendMessage(Messages.WRONG_PASSWORD.asComponent());
                    return;
                }

                try {
                    UpdateBuilder<Account, String> updateBuilder = this.plugin.getDatabaseHandler().getPlayerDao().updateBuilder();
                    updateBuilder.where().eq("id", ((Player) source).getUsername());
                    updateBuilder.updateColumnValue("password", CryptoUtils.generateHash(args[1]));
                    updateBuilder.update();
                    this.plugin.getAccountHandler().cleanup(player.getName());
                    source.sendMessage(Messages.CHANGE_PASSWORD_SUCCESSFUL.asComponent());
                } catch (SQLException e) {
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
