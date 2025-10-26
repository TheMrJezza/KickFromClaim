package dev.themrjezza.kickfromclaim.api;

import java.util.concurrent.CompletableFuture;

public interface Reloadable {
    CompletableFuture<Boolean> reload();
}
