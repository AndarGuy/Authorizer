package me.andarguy.authorizer.event;

import net.kyori.adventure.text.Component;

public interface Cancellable {
    boolean isCancelled();

    void setCancelled(boolean cancel);
}