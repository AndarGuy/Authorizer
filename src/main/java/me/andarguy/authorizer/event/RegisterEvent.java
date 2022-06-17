package me.andarguy.authorizer.event;

import lombok.Getter;
import lombok.Setter;
import me.andarguy.cc.common.models.PlayerAccount;

public class RegisterEvent implements Cancellable {

    @Getter
    private final PlayerAccount account;

    @Getter
    @Setter
    boolean cancelled;

    public RegisterEvent(PlayerAccount account) {
        this.account = account;
    }
}
