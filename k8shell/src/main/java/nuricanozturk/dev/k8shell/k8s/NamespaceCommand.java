package nuricanozturk.dev.k8shell.k8s;

import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import nuricanozturk.dev.k8shell.component.ComponentProvider;
import nuricanozturk.dev.k8shell.KubernetesData;
import nuricanozturk.dev.k8shell.CommandlinePrinter;
import nuricanozturk.dev.k8shell.exception.ItemNotFoundException;
import org.springframework.shell.component.SingleItemSelector;
import org.springframework.shell.component.support.SelectorItem;
import org.springframework.shell.context.InteractionMode;
import org.springframework.shell.standard.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@ShellComponent
@ShellCommandGroup("Namespace Commands")
public class NamespaceCommand extends AbstractShellComponent {
    private final CoreV1Api api;
    private final CommandlinePrinter commandlinePrinter;
    private final ComponentProvider componentProvider;

    public NamespaceCommand(final CoreV1Api api, final CommandlinePrinter commandlinePrinter, final ComponentProvider componentProvider) {
        this.api = api;
        this.commandlinePrinter = commandlinePrinter;
        this.componentProvider = componentProvider;
    }

    @ShellMethod(key = {"select namespaces", "set-namespaces", "sn"}, value = "Change namespace", prefix = "-")
    public void changeNamespace(
            @ShellOption(value = "n", help = "Namespace name", optOut = true, defaultValue = "") final String namespace
    ) throws ApiException {
        if (!namespace.isEmpty() && !namespace.isBlank()) {
            KubernetesData.getInstance().changeNamespace(namespace);
            commandlinePrinter.printSuccess("Namespace selected: " + namespace);
            return;
        }

        final var namespaces = api.listNamespace().executeWithHttpInfo();

        if (namespaces.getData() == null) {
            throw new ItemNotFoundException("No namespaces found.");
        }

        final var items = namespaces.getData().getItems().stream()
                .map(n -> SelectorItem.of(Objects.requireNonNull(n.getMetadata()).getName(), n.getMetadata().getName()))
                .toList();

        final var context = new ComponentProvider.SingleSelectionBuilder<String>()
                .setTerminal(getTerminal())
                .setResourceLoader(getResourceLoader())
                .setTemplateExecutor(getTemplateExecutor())
                .setItems(items)
                .setMessage("Select Namespace")
                .setPrintResults(false)
                .build();

        final var selectedNamespace = context.run(SingleItemSelector.SingleItemSelectorContext.empty())
                .getResultItem()
                .flatMap(si -> Optional.ofNullable(si.getItem()))
                .orElseThrow(() -> new ItemNotFoundException("No namespace selected."));

        KubernetesData.getInstance().changeNamespace(selectedNamespace);

        commandlinePrinter.printSuccess("Namespace selected: " + selectedNamespace);
    }

    @ShellMethod(key = {"ln", "list namespaces"}, interactionMode = InteractionMode.INTERACTIVE, value = "List namespaces")
    public void listNamespaces() throws Exception {
        final var namespaces = api.listNamespace().executeWithHttpInfo();

        if (namespaces.getData() == null || namespaces.getData().getItems().isEmpty()) {
            System.out.println("No namespaces found.");
        }

        final var items = namespaces.getData().getItems().stream()
                .map(n -> new String[]{Objects.requireNonNull(n.getMetadata()).getName()})
                .collect(Collectors.toList());


        final var result = this.componentProvider.renderTable(List.of("Namespaces"), items);

        commandlinePrinter.printSuccess(result);
    }
}
