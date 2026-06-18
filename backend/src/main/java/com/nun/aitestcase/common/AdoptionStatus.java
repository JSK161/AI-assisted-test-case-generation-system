package com.nun.aitestcase.common;

import java.util.Set;

public final class AdoptionStatus {

    public static final String PENDING = "PENDING";
    public static final String ADOPTED = "ADOPTED";
    public static final String NEEDS_REVISION = "NEEDS_REVISION";
    public static final String REJECTED = "REJECTED";

    private static final Set<String> VALUES = Set.of(PENDING, ADOPTED, NEEDS_REVISION, REJECTED);

    private AdoptionStatus() {
    }

    public static boolean isValid(String value) {
        return VALUES.contains(value);
    }
}
