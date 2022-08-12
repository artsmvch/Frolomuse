package com.frolo.muse.di.impl.local;


final class Preconditions {

    // Checks that 'candidate' string iss not null and is listed in others
    static boolean isNotNullAndListed(
            String candidate,
            String[] others) {

        if (candidate == null) {
            return false;
        }

        for (String other : others) {
            if (candidate.equals(other)) {
                return true;
            }
        }
        return false;
    }

    // Returns 'candidate' string if it's not null and is listed in others or default
    static String takeIfNotNullAndListedOrDefault(
            String candidate,
            String[] others,
            String defString) {
        if (isNotNullAndListed(candidate, others)) {
            return candidate;
        } else {
            return defString;
        }
    }

    private Preconditions() {
    }
}
