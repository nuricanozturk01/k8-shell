package nuricanozturk.dev.k8shell;

import org.springframework.stereotype.Service;

@Service
public class FileService {

    public boolean checkFileFormat(String format) {
        if (format == null || format.isBlank() || (!format.equalsIgnoreCase("json") && !format.equalsIgnoreCase("yaml") && !format.equalsIgnoreCase("yml"))) {
            throw new IllegalArgumentException("Invalid format. Use 'json' or 'yaml'");
        }
        return true;
    }
}
