package me.andarguy.authorizer.event;

import lombok.Getter;
import me.andarguy.authorizer.model.Account;

public class AuthenticateEvent {
  @Getter
  private final Account account;

  public AuthenticateEvent(Account account) {
    this.account = account;
  }
}
