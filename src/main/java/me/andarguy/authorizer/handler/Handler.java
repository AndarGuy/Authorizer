package me.andarguy.authorizer.handler;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.andarguy.authorizer.Authorizer;

@RequiredArgsConstructor
 abstract class Handler implements Loadable {
    @Getter
    protected final Authorizer plugin;
}
