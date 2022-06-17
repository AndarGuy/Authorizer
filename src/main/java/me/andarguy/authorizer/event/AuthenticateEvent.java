package me.andarguy.authorizer.event;

import lombok.Getter;
import me.andarguy.cc.common.models.PlayerAccount;

public class AuthenticateEvent {
  @Getter
  private final PlayerAccount account;

  public AuthenticateEvent(PlayerAccount account) {
    this.account = account;
  }
}
