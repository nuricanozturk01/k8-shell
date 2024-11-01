package nuricanozturk.dev.k8shell.k8s;

import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Secret;
import nuricanozturk.dev.k8shell.util.Calculator;
import nuricanozturk.dev.k8shell.component.ComponentProvider;
import nuricanozturk.dev.k8shell.KubernetesData;
import nuricanozturk.dev.k8shell.CommandlinePrinter;
import nuricanozturk.dev.k8shell.exception.ItemNotFoundException;
import org.fusesource.jansi.Ansi;
import org.springframework.shell.component.SingleItemSelector.SingleItemSelectorContext;
import org.springframework.shell.component.support.SelectorItem;
import org.springframework.shell.standard.*;

import java.util.*;
import java.util.stream.Collectors;

import static java.nio.ByteBuffer.wrap;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toMap;

@ShellComponent
@ShellCommandGroup("Kubernetes Secret Commands")
public class SecretCommand extends AbstractShellComponent {
    private final CommandlinePrinter commandlinePrinter;
    private final ComponentProvider componentProvider;
    private final CoreV1Api api;

    public SecretCommand(CommandlinePrinter commandlinePrinter, ComponentProvider componentProvider, CoreV1Api api) {
        this.commandlinePrinter = commandlinePrinter;
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

        final var result = componentProvider.renderTable(headers, items);
        commandlinePrinter.print(result, Ansi.Color.GREEN);
    }


    @ShellMethod(key = {"ss", "select secret"}, value = "Select a secret", prefix = "-")
    public void selectSecret(
            @ShellOption(value = "d", help = "Decode base64 encoded data", optOut = true, defaultValue = "false") final boolean decode64,
            @ShellOption(value = "t", help = "List decoded secrets in a table", optOut = true, defaultValue = "false") final boolean showTable,
            @ShellOption(value = "s", help = "List decoded secrets like string", optOut = true, defaultValue = "false") final boolean showText,
            @ShellOption(value = "r", help = "Use memorized secret", defaultValue = "false", optOut = true) final boolean remember
    ) throws ApiException {

        if (remember) {
            if (!showTable && !showText) {
                commandlinePrinter.print("No output option selected. You should enter the command like: ss -d64 -r -lt (or -ls)", Ansi.Color.YELLOW);
            }
            final var selectedSecret = KubernetesData.getInstance().getSecret();
            checkAndApplyOptions(selectedSecret, decode64, showTable, showText);
            return;
        }

        final var selectedSelectorItemList = getSecrets().stream()
                .map(s -> SelectorItem.of(Objects.requireNonNull(s.getMetadata()).getName(), s))
                .collect(Collectors.toList());

        final var context = new ComponentProvider.SingleSelectionBuilder<V1Secret>()
                .setTerminal(getTerminal())
                .setResourceLoader(getResourceLoader())
                .setTemplateExecutor(getTemplateExecutor())
                .setItems(selectedSelectorItemList)
                .setMessage("Select a secret")
                .setPrintResults(false)
                .build();

        final var selectedSecret = context.run(SingleItemSelectorContext.empty())
                .getResultItem()
                .flatMap(si -> Optional.ofNullable(si.getItem()))
                .orElseThrow(() -> new ItemNotFoundException("No secret selected."));

        KubernetesData.getInstance().setSecret(selectedSecret);

        checkAndApplyOptions(selectedSecret, decode64, showTable, showText);
    }

    private void checkAndApplyOptions(final V1Secret selectedSecret,
                                      final boolean decode64,
                                      final boolean showTable,
                                      final boolean showText) {
        if (decode64) {
            decodeAndPrintSecret(selectedSecret, showTable, showText);
        }
    }

    private void decodeAndPrintSecret(final V1Secret selectedSecret, final boolean showTable, final boolean showText) {
        final var decodedSecrets = getDecodedSecrets(selectedSecret);
        if (showTable) {
            final var headers = List.of("Key", "Value");

            final var decodedItems = decodedSecrets.entrySet().stream()
                    .map(e -> new String[]{e.getKey(), e.getValue()})
                    .collect(Collectors.toList());

            final var result = componentProvider.renderTable(headers, decodedItems);
            commandlinePrinter.print(result, Ansi.Color.GREEN);
        } else if (showText) {
            decodedSecrets.forEach((key, value) -> commandlinePrinter.print(key + ": " + value, Ansi.Color.GREEN));
        }
    }

    private Map<String, String> getDecodedSecrets(final V1Secret selectedSecret) {
        final var secretData = selectedSecret.getData();

        if (secretData == null) {
            throw new ItemNotFoundException("No data found in the selected secret.");
        }

        return secretData.entrySet().stream()
                .collect(toMap(Map.Entry::getKey, entry -> UTF_8.decode(wrap(entry.getValue())).toString()));
    }

    private String[] toRow(final V1Secret secret) {
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

        final var secretList = api.listNamespacedSecret(namespace).executeWithHttpInfo().getData().getItems();
        if (secretList.isEmpty()) {
            throw new ItemNotFoundException("No pods found in namespace: " + namespace);
        }

        return secretList;
    }
}
