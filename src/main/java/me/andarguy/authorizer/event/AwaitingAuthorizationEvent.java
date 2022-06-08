package me.andarguy.authorizer.event;

import lombok.Getter;
import lombok.Setter;
import me.andarguy.authorizer.handler.AuthorizationSessionHandler;

public class AwaitingAuthorizationEvent implements Cancellable {
  @Getter
  private final AuthorizationSessionHandler authorizationHandler;

  @Getter
  @Setter
  boolean cancelled;

  public AwaitingAuthorizationEvent(AuthorizationSessionHandler authorizationHandler) {
    this.authorizationHandler = authorizationHandler;
  }
}
