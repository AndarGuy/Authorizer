package me.andarguy.authorizer.event;

import lombok.Getter;
import lombok.Setter;
import me.andarguy.authorizer.model.Account;

public class LoginEvent implements Cancellable {

    @Getter
    private final Account account;

    @Getter
    @Setter
    boolean cancelled;

    public LoginEvent(Account account) {
        this.account = account;
    }
}
