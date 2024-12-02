package nuricanozturk.dev.k8shell.k8s.command;

import io.kubernetes.client.openapi.models.V1ContainerStatus;
import io.kubernetes.client.openapi.models.V1Pod;
import nuricanozturk.dev.k8shell.k8s.KubernetesData;
import nuricanozturk.dev.k8shell.printer.CommandlinePrinter;
import nuricanozturk.dev.k8shell.component.ComponentProvider;
import nuricanozturk.dev.k8shell.exception.ItemNotFoundException;
import org.fusesource.jansi.Ansi;
import org.springframework.shell.component.SingleItemSelector;
import org.springframework.shell.component.support.SelectorItem;
import org.springframework.shell.standard.AbstractShellComponent;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static nuricanozturk.dev.k8shell.k8s.CommandInfo.*;

@ShellComponent
@ShellCommandGroup("Kubernetes Commands")
public class PodCommand extends AbstractShellComponent {

    private final CoreV1Api api;
    private final CommandlinePrinter commandlinePrinter;
    private final ComponentProvider componentProvider;

    public PodCommand(final CoreV1Api api, final CommandlinePrinter commandlinePrinter, final ComponentProvider componentProvider) {
        this.api = api;
        this.commandlinePrinter = commandlinePrinter;
        this.componentProvider = componentProvider;
    }

    private List<V1Pod> getPods() throws ApiException {
        final var currentNamespace = KubernetesData.getInstance().getNamespace();
        if (currentNamespace == null) {
            throw new ItemNotFoundException("Namespace not selected.");
        }

        final var podList = api.listNamespacedPod(currentNamespace).executeWithHttpInfo().getData().getItems();
        if (podList.isEmpty()) {
            throw new ItemNotFoundException("No pods found in namespace: " + currentNamespace);
        }

        return podList;

    }

    @ShellMethod(key = {LIST_PODS_SHORT_CMD, LIST_PODS_LONG_CMD}, value = LIST_PODS_HELP)
    public void listPods() throws ApiException {
        final var headers = List.of("Namespace", "Name", "Status", "Restarts");

        final var podTableItemList = getPods().stream()
                .map(this::toRow)
                .collect(Collectors.toList());

        final var renderedTable = componentProvider.renderTable(headers, podTableItemList);

        commandlinePrinter.print(renderedTable, Ansi.Color.GREEN);
    }


    @ShellMethod(key = {SELECT_POD_SHORT_CMD, SELECT_POD_LONG_CMD}, value = SELECT_POD_HELP)
    public void selectPod() throws ApiException {
        final var selectSecretItemList = getPods().stream()
                .map(n -> SelectorItem.of(Objects.requireNonNull(n.getMetadata()).getName(), n))
                .collect(Collectors.toList());

        final var context = new ComponentProvider.SingleSelectionBuilder<V1Pod>()
                .setTerminal(getTerminal())
                .setResourceLoader(getResourceLoader())
                .setTemplateExecutor(getTemplateExecutor())
                .setItems(selectSecretItemList)
                .setMessage("Select a Pod")
                .setPrintResults(false)
                .build();

        final var selectedPod = context.run(SingleItemSelector.SingleItemSelectorContext.empty())
                .getResultItem()
                .flatMap(si -> Optional.ofNullable(si.getItem()))
                .orElseThrow(() -> new ItemNotFoundException("No pod selected."));

        KubernetesData.getInstance().setPod(selectedPod);
        commandlinePrinter.printSuccess("Pod selected: " + Objects.requireNonNull(selectedPod.getMetadata()).getName());
    }


    private String[] toRow(final V1Pod pod) {
        final var restartCount = Objects.requireNonNull(Objects.requireNonNull(pod.getStatus()).getContainerStatuses()).stream()
                .mapToInt(V1ContainerStatus::getRestartCount)
                .sum();

        return new String[]{
                Objects.requireNonNull(pod.getMetadata()).getNamespace(),
                pod.getMetadata().getName(),
                Objects.requireNonNull(pod.getStatus()).getPhase(),
                String.valueOf(restartCount)
        };
    }
}
