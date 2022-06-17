package me.andarguy.authorizer.event;

import lombok.Getter;
import lombok.Setter;
import me.andarguy.cc.common.models.PlayerAccount;

public class LoginEvent implements Cancellable {

    @Getter
    private final PlayerAccount account;

    @Getter
    @Setter
    boolean cancelled;

    public LoginEvent(PlayerAccount account) {
        this.account = account;
    }
}
