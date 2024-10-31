package nuricanozturk.dev.k8shell;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.kubernetes.client.common.KubernetesObject;
import io.kubernetes.client.util.Yaml;
import nuricanozturk.dev.k8shell.util.FormatValidator;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;
import org.springframework.stereotype.Service;

@Service
public class CommandlinePrinter {

    public void print(final String message) {
        System.out.println(message);
    }

    public void print(final String message, final Ansi.Color color) {
        AnsiConsole.systemInstall();
        System.out.println(Ansi.ansi().fg(color).a(message).reset());
        AnsiConsole.systemUninstall();
    }

    public void printSuccess(final String message) {
        print(message, Ansi.Color.GREEN);
    }

    public void printError(final String message) {
        print(message, Ansi.Color.RED);
    }

    public void print(final String prefix, final String message) {
        String output = Ansi.ansi()
                .fg(Ansi.Color.YELLOW).a(prefix).reset()
                .a(": ")
                .fg(Ansi.Color.GREEN).a(message).reset()
                .toString();

        System.out.println(output);
    }

    public void printKubernetesObject(final KubernetesObject selectedDeployment, final String format) {
        try {
            var fileFormat = FormatValidator.checkFileFormat(format) ? format : "yaml";
            if (fileFormat.equals("json")) {
                final var jsonMapper = new ObjectMapper();
                jsonMapper.registerModule(new JavaTimeModule());
                jsonMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                final var jsonOutput = jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(selectedDeployment);
                print("JSON Output:\n" + jsonOutput);
            } else {
                final var yamlOutput = Yaml.dump(selectedDeployment);
                print("YAML Output:\n" + yamlOutput);
            }
        } catch (Exception e) {
            printError("An error occurred while opening the deployment: " + e.getMessage());
        }
    }
}
