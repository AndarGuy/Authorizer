package me.andarguy.authorizer.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import me.andarguy.authorizer.Authorizer;
import me.andarguy.authorizer.settings.Messages;
import net.elytrium.java.commons.mc.serialization.Serializer;
import net.elytrium.java.commons.mc.velocity.commands.SuggestUtils;

import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;

public class ForceUnregisterCommand implements SimpleCommand {

  private final Authorizer plugin;

  public ForceUnregisterCommand(Authorizer plugin) {
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

      Serializer serializer = Authorizer.getSerializer();
      try {
        this.plugin.getDatabaseHandler().getPlayerDao().deleteById(playerNick.toLowerCase(Locale.ROOT));
        this.plugin.getAccountHandler().cleanup(playerNick);
        this.plugin.getServer().getPlayer(playerNick).ifPresent(player -> player.disconnect(Messages.FORCE_UNREGISTER_KICK.asComponent()));
        source.sendMessage(serializer.deserialize(MessageFormat.format(Messages.FORCE_UNREGISTER_SUCCESSFUL.asString(), playerNick)));
      } catch (SQLException e) {
        source.sendMessage(serializer.deserialize(MessageFormat.format(Messages.FORCE_UNREGISTER_NOT_SUCCESSFUL.asString(), playerNick)));
        e.printStackTrace();
      }
    } else {
      source.sendMessage(Messages.FORCE_UNREGISTER_USAGE.asComponent());
    }
  }

  @Override
  public boolean hasPermission(Invocation invocation) {
    return invocation.source().hasPermission("authorizer.admin.forceunregister");
  }
}
