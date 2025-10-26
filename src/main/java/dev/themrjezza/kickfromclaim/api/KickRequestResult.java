package dev.themrjezza.kickfromclaim.api;

public enum KickRequestResult {
    SUCCESS,
    TARGET_NOT_IN_ANY_CLAIM,
    TARGET_NOT_IN_MANAGED_CLAIM,
    TARGET_HAS_TRUST_IN_CLAIM,
    TARGET_HAS_EXEMPT_PERMISSION,
}
