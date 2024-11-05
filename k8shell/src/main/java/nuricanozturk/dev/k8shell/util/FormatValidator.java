package nuricanozturk.dev.k8shell.util;

public final class FormatValidator {
    private FormatValidator() {
    }

    public static boolean checkFileFormat(final String format) {
        if (format == null || format.isBlank() || (!format.equalsIgnoreCase("json") && !format.equalsIgnoreCase("yaml") && !format.equalsIgnoreCase("yml"))) {
            throw new IllegalArgumentException("Invalid format. Use 'json' or 'yaml'");
        }
        return true;
    }

    // ...
}
