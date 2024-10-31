package nuricanozturk.dev.k8shell;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.kubernetes.client.common.KubernetesObject;
import nuricanozturk.dev.k8shell.util.FormatValidator;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class FileService {

    private final CommandlinePrinter commandlinePrinter;

    public FileService(final CommandlinePrinter commandlinePrinter) {
        this.commandlinePrinter = commandlinePrinter;
    }


    public void exportKubernetesObjectWithFormat(final KubernetesObject kubernetesObject, final String format, final String output) {
        try {
            final var fileFormat = FormatValidator.checkFileFormat(format) ? format : "yaml";
            final var mapper = fileFormat.equals("json") ? new ObjectMapper() : new ObjectMapper(new YAMLFactory());

            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            mapper.writeValue(new File(output), kubernetesObject);

            commandlinePrinter.printSuccess("Deployment exported successfully to " + fileFormat.toUpperCase() + " format at: " + output);
        } catch (IOException e) {
            commandlinePrinter.printError("An error occurred while exporting the deployment: " + e.getMessage());
        }
    }
}
