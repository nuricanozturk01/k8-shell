package nuricanozturk.dev.k8shell.k8s;

import io.kubernetes.client.openapi.models.V1ContainerStatus;
import io.kubernetes.client.openapi.models.V1Pod;
import nuricanozturk.dev.k8shell.KubernetesData;
import nuricanozturk.dev.k8shell.ShellPrinter;
import nuricanozturk.dev.k8shell.ComponentProvider;
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

@ShellComponent
@ShellCommandGroup("Kubernetes Commands")
public class PodCommands extends AbstractShellComponent {

    private final CoreV1Api api;
    private final ShellPrinter shellPrinter;
    private final ComponentProvider componentProvider;

    public PodCommands(CoreV1Api api, ShellPrinter shellPrinter, ComponentProvider componentProvider) {
        this.api = api;
        this.shellPrinter = shellPrinter;
        this.componentProvider = componentProvider;
    }

    private List<V1Pod> getPods() throws ApiException {
        final var namespace = KubernetesData.getInstance().getNamespace();

        if (namespace == null) {
            throw new ItemNotFoundException("Namespace not selected.");
        }

        final var allPods = api.listNamespacedPod(namespace).executeWithHttpInfo().getData().getItems();

        if (allPods.isEmpty()) {
            throw new ItemNotFoundException("No pods found in namespace: " + namespace);
        }

        return allPods;

    }

    @ShellMethod(key = {"list pods", "lp", "list-pods"}, value = "List some pods")
    public void listPods() throws ApiException {
        final var pods = getPods();

        final var headers = List.of("Namespace", "Name", "Status", "Restarts");
        final var items = pods.stream()
                .map(this::toRow)
                .collect(Collectors.toList());

        final var result = componentProvider.toTable(headers, items);
        shellPrinter.print(result, Ansi.Color.GREEN);
    }


    @ShellMethod(key = {"select pod", "sp"}, value = "Select a pod")
    public void selectPod() {
        try {
            final var pods = getPods();

            if (pods.isEmpty()) {
                shellPrinter.print("No pods found.", Ansi.Color.RED);
                return;
            }

            final var items = pods.stream()
                    .map(n -> SelectorItem.of(Objects.requireNonNull(n.getMetadata()).getName(), n))
                    .collect(Collectors.toList());

            final var selectedPod = new ComponentProvider.SingleSelectionBuilder<V1Pod>()
                    .setTerminal(getTerminal())
                    .setResourceLoader(getResourceLoader())
                    .setTemplateExecutor(getTemplateExecutor())
                    .setItems(items)
                    .setMessage("Select a Pod")
                    .setDefaultItem(null)
                    .setPrintResults(false)
                    .build()
                    .run(SingleItemSelector.SingleItemSelectorContext.empty())
                    .getResultItem().flatMap(si -> Optional.ofNullable(si.getItem())).orElseGet(() -> {
                        shellPrinter.print("No pod selected.", Ansi.Color.RED);
                        return null;
                    });

            if (selectedPod == null) {
                shellPrinter.print("No pod selected.", Ansi.Color.RED);
                return;
            }
            KubernetesData.getInstance().setPod(selectedPod);
            shellPrinter.printSuccess("Pod selected: " + Objects.requireNonNull(selectedPod.getMetadata()).getName());
        } catch (Exception ex) {
            shellPrinter.printError("Error selecting pod: " + ex.getMessage());
        }
    }


    private String[] toRow(V1Pod pod) {
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
