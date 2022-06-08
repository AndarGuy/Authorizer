package me.andarguy.authorizer.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.permission.Tristate;
import com.velocitypowered.api.proxy.Player;
import me.andarguy.authorizer.Authorizer;
import me.andarguy.authorizer.model.Account;
import me.andarguy.authorizer.settings.Messages;
import me.andarguy.authorizer.utils.CryptoUtils;

import java.sql.SQLException;
import java.util.Locale;

public class UnregisterCommand implements SimpleCommand {

  private final Authorizer plugin;

  public UnregisterCommand(Authorizer plugin) {
    this.plugin = plugin;
  }

  @Override
  public void execute(Invocation invocation) {
    CommandSource source = invocation.source();
    String[] args = invocation.arguments();

    if (source instanceof Player) {
      if (args.length == 2) {
        if ("confirm".equalsIgnoreCase(args[1])) {
          String username = ((Player) source).getUsername();
          Account player = plugin.getAccountHandler().getAccount(username);
          if (player == null) {
            source.sendMessage(Messages.NOT_REGISTERED.asComponent());
          } else if (player.getHashedPassword().isEmpty()) {
            source.sendMessage(Messages.CRACKED_COMMAND.asComponent());
          } else if (CryptoUtils.checkPassword(args[0], player.getHashedPassword())) {
            try {
              this.plugin.getDatabaseHandler().getPlayerDao().deleteById(username.toLowerCase(Locale.ROOT));
              this.plugin.getAccountHandler().cleanup(username);
              ((Player) source).disconnect(Messages.UNREGISTER_SUCCESSFUL.asComponent());
            } catch (SQLException e) {
              source.sendMessage(Messages.ERROR_OCCURRED.asComponent());
              e.printStackTrace();
            }
          } else {
            source.sendMessage(Messages.WRONG_PASSWORD.asComponent());
          }

          return;
        }
      }

      source.sendMessage(Messages.UNREGISTER_USAGE.asComponent());
    } else {
      source.sendMessage(Messages.NOT_PLAYER.asComponent());
    }
  }

  @Override
  public boolean hasPermission(Invocation invocation) {
    return invocation.source().getPermissionValue("authorizer.commands.unregister") == Tristate.TRUE;
  }
}
