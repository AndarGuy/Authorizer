package me.andarguy.authorizer.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.permission.Tristate;
import com.velocitypowered.api.proxy.Player;
import me.andarguy.authorizer.Authorizer;
import me.andarguy.authorizer.settings.Messages;

public class LogoutCommand implements SimpleCommand {

  private final Authorizer plugin;

  public LogoutCommand(Authorizer plugin) {
    this.plugin = plugin;
  }

  @Override
  public void execute(Invocation invocation) {
    CommandSource source = invocation.source();

    if (source instanceof Player) {
      this.plugin.getAccountHandler().closeSession((Player) source);
      ((Player) source).disconnect(Messages.DESTROY_SESSION_SUCCESSFUL.asComponent());
    } else {
      source.sendMessage(Messages.NOT_PLAYER.asComponent());
    }
  }

  @Override
  public boolean hasPermission(Invocation invocation) {
    return invocation.source().getPermissionValue("authorizer.commands.logout") == Tristate.TRUE;
  }
}
