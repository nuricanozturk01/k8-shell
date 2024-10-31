package nuricanozturk.dev.k8shell.k8s;

import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Secret;
import nuricanozturk.dev.k8shell.Calculator;
import nuricanozturk.dev.k8shell.ComponentProvider;
import nuricanozturk.dev.k8shell.KubernetesData;
import nuricanozturk.dev.k8shell.ShellPrinter;
import nuricanozturk.dev.k8shell.exception.ItemNotFoundException;
import org.fusesource.jansi.Ansi;
import org.springframework.shell.component.SingleItemSelector;
import org.springframework.shell.component.support.SelectorItem;
import org.springframework.shell.standard.*;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@ShellComponent
@ShellCommandGroup("Kubernetes Secret Commands")
public class SecretCommand extends AbstractShellComponent {
    private final ShellPrinter shellPrinter;
    private final ComponentProvider componentProvider;
    private final CoreV1Api api;

    public SecretCommand(ShellPrinter shellPrinter, ComponentProvider componentProvider, CoreV1Api api) {
        this.shellPrinter = shellPrinter;
        this.componentProvider = componentProvider;
        this.api = api;
    }

    @ShellMethod(key = {"list secrets", "ls"}, value = "List secrets in the current namespace")
    public void listSecrets() throws ApiException {
        final var secrets = getSecrets();

        final var headers = List.of("Namespace", "Name", "Type", "Data", "Age");
        final var items = secrets.stream()
                .map(this::toRow)
                .collect(Collectors.toList());

        final var result = componentProvider.toTable(headers, items);
        shellPrinter.print(result, Ansi.Color.GREEN);
    }


    @ShellMethod(key = {"ss", "select secret"}, value = "Select a secret")
    public void selectSecret(
            @ShellOption(value = "d64", help = "Decode base64 encoded data", optOut = true) boolean decode64,
            @ShellOption(value = "lt", help = "List decoded secrets in a table", optOut = true) boolean showTable,
            @ShellOption(value = "ls", help = "List decoded secrets like string", optOut = true) boolean showText,
            @ShellOption(value = "mem", help = "Remember the selected secret. You should select the show optiosn", defaultValue = "false", optOut = true) boolean remember
    ) throws ApiException {

        if (remember) {
            if (!showTable && !showText) {
                shellPrinter.print("No output option selected. You should enter the command like: ss --d64 --mem --lt (or --ls)", Ansi.Color.YELLOW);
            }
            if (decode64) {
                decodeAndPrintSecret(KubernetesData.getInstance().getSecret(), showTable, showText);
            }
            return;
        }
        final var secrets = getSecrets();

        if (secrets.isEmpty()) {
            throw new ItemNotFoundException("No secrets found in the current namespace.");
        }

        final var items = secrets.stream()
                .map(s -> SelectorItem.of(Objects.requireNonNull(s.getMetadata()).getName(), s))
                .collect(Collectors.toList());

        final var selectedSecret = new ComponentProvider.SingleSelectionBuilder<V1Secret>()
                .setTerminal(getTerminal())
                .setResourceLoader(getResourceLoader())
                .setTemplateExecutor(getTemplateExecutor())
                .setItems(items)
                .setMessage("Select a secret")
                .setDefaultItem(null)
                .setPrintResults(false)
                .build()
                .run(SingleItemSelector.SingleItemSelectorContext.empty())
                .getResultItem().flatMap(si -> Optional.ofNullable(si.getItem())).orElseGet(() -> {
                    shellPrinter.print("No secret selected.", Ansi.Color.RED);
                    return null;
                });

        if (selectedSecret == null) {
            throw new ItemNotFoundException("No secret selected.");
        }

        KubernetesData.getInstance().setSecret(selectedSecret);

        if (decode64) {
            decodeAndPrintSecret(selectedSecret, showTable, showText);
        }
    }

    private void decodeAndPrintSecret(V1Secret selectedSecret, boolean showTable, boolean showText) {
        final var decodedSecrets = getDecodedSecrets(selectedSecret);
        if (showTable) {
            final var headers = List.of("Key", "Value");
            final var decodedItems = decodedSecrets.entrySet().stream()
                    .map(e -> new String[]{e.getKey(), e.getValue()})
                    .collect(Collectors.toList());

            final var result = componentProvider.toTable(headers, decodedItems);
            shellPrinter.print(result, Ansi.Color.GREEN);
        } else if (showText) {
            decodedSecrets.forEach((key, value) -> shellPrinter.print(key + ": " + value, Ansi.Color.GREEN));
        }
    }

    private Map<String, String> getDecodedSecrets(V1Secret selectedSecret) {
        final var secretData = selectedSecret.getData();

        if (secretData == null) {
            throw new ItemNotFoundException("No data found in the selected secret.");
        }

        return secretData.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> StandardCharsets.UTF_8.decode(ByteBuffer.wrap(entry.getValue())).toString()
                ));
    }

    private String[] toRow(V1Secret secret) {
        final var secretAge = Calculator.calculateAge(Objects.requireNonNull(secret.getMetadata()).getCreationTimestamp());

        return new String[]{
                Objects.requireNonNull(secret.getMetadata()).getNamespace(),
                secret.getMetadata().getName(),
                Objects.requireNonNull(secret.getType()),
                String.valueOf((long) Objects.requireNonNull(secret.getData()).keySet().size()),
                secretAge
        };
    }

    private List<V1Secret> getSecrets() throws ApiException {
        final var namespace = KubernetesData.getInstance().getNamespace();

        if (namespace == null) {
            throw new ItemNotFoundException("Namespace not selected.");
        }

        final var allPods = api.listNamespacedSecret(namespace).executeWithHttpInfo().getData().getItems();

        if (allPods.isEmpty()) {
            throw new ItemNotFoundException("No pods found in namespace: " + namespace);
        }

        return allPods;

    }
}
