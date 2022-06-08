package me.andarguy.authorizer.event;

import lombok.Getter;
import lombok.Setter;
import me.andarguy.authorizer.model.Account;

public class RegisterEvent implements Cancellable {

    @Getter
    private final Account account;

    @Getter
    @Setter
    boolean cancelled;

    public RegisterEvent(Account account) {
        this.account = account;
    }
}
