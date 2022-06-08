package me.andarguy.authorizer.command;

import com.j256.ormlite.stmt.UpdateBuilder;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import me.andarguy.authorizer.Authorizer;
import me.andarguy.authorizer.model.Account;
import me.andarguy.authorizer.settings.Messages;
import me.andarguy.authorizer.utils.CryptoUtils;
import net.elytrium.java.commons.mc.velocity.commands.SuggestUtils;

import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

public class ForceChangePasswordCommand implements SimpleCommand {

  private final Authorizer plugin;

  public ForceChangePasswordCommand(Authorizer plugin) {
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

    if (args.length == 2) {
      String nickname = args[0];
      String newPassword = args[1];
      try {
        UpdateBuilder<Account, String> updateBuilder = this.plugin.getDatabaseHandler().getPlayerDao().updateBuilder();
        updateBuilder.where().eq("id", nickname.toLowerCase(Locale.ROOT));
        updateBuilder.updateColumnValue("password", CryptoUtils.generateHash(newPassword));
        updateBuilder.update();

        this.plugin.getServer().getPlayer(nickname).ifPresent(player -> player.sendMessage(Messages.FORCE_CHANGE_PASSWORD_MESSAGE.asFormattedComponent(newPassword)));

        source.sendMessage(Messages.FORCE_CHANGE_PASSWORD_SUCCESSFUL.asFormattedComponent(nickname));
      } catch (SQLException e) {
        source.sendMessage(Messages.FORCE_CHANGE_PASSWORD_NOT_SUCCESSFUL.asFormattedComponent(nickname));
        e.printStackTrace();
      }
    } else {
      source.sendMessage(Messages.FORCE_CHANGE_PASSWORD_USAGE.asComponent());
    }
  }

  @Override
  public boolean hasPermission(Invocation invocation) {
    return invocation.source().hasPermission("authorizer.admin.forcechangepassword");
  }
}
