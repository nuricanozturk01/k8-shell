package nuricanozturk.dev.k8shell;

import java.time.Duration;
import java.time.OffsetDateTime;

public final class Calculator {
    private Calculator() {
    }

    public static String calculateAge(OffsetDateTime creationTimestamp) {
        final var age = Duration.between(creationTimestamp, OffsetDateTime.now());

        String ageDisplay;
        if (age.getSeconds() < 60) {
            ageDisplay = age.getSeconds() + " seconds ago";
        } else if (age.toMinutes() < 60) {
            ageDisplay = age.toMinutes() + " minutes ago";
        } else if (age.toHours() < 24) {
            ageDisplay = age.toHours() + " hours ago";
        } else {
            ageDisplay = age.toDays() + " days ago";
        }

        return ageDisplay;
    }
}
