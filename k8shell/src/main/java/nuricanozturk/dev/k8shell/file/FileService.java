package nuricanozturk.dev.k8shell.file;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.kubernetes.client.common.KubernetesObject;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@Service
public class FileService {

    public Map<Boolean, String> exportKubernetesObjectWithFormat(final KubernetesObject kubernetesObject, final String format, final String output) {
        try {
            final var fileFormat = checkFormat(format) ? format : "yaml";
            final var mapper = fileFormat.equals("json") ? new ObjectMapper() : new ObjectMapper(new YAMLFactory());

            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            mapper.writeValue(new File(output), kubernetesObject);

            final var message = "Deployment exported successfully to " + fileFormat.toUpperCase() + " format at: " + output;
            return Map.of(true, message);

        } catch (IOException e) {
            final var message = "An error occurred while exporting the deployment: " + e.getMessage();
            return Map.of(false, message);
        }
    }

    public boolean checkFormat(final String format) {
        if (format == null || format.isBlank() || (!format.equalsIgnoreCase("json") && !format.equalsIgnoreCase("yaml") && !format.equalsIgnoreCase("yml"))) {
            throw new IllegalArgumentException("Invalid format. Use 'json' or 'yaml'");
        }
        return true;
    }

}
