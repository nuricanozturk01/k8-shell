package nuricanozturk.dev.k8shell.k8s;

import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.openapi.models.V1ServicePort;
import nuricanozturk.dev.k8shell.component.ComponentProvider;
import nuricanozturk.dev.k8shell.FileService;
import nuricanozturk.dev.k8shell.KubernetesData;
import nuricanozturk.dev.k8shell.CommandlinePrinter;
import nuricanozturk.dev.k8shell.exception.ItemNotFoundException;
import nuricanozturk.dev.k8shell.util.Calculator;
import nuricanozturk.dev.k8shell.util.Command;
import org.fusesource.jansi.Ansi;
import org.springframework.shell.component.SingleItemSelector;
import org.springframework.shell.component.support.SelectorItem;
import org.springframework.shell.standard.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static nuricanozturk.dev.k8shell.util.Command.*;

@ShellComponent
@ShellCommandGroup("Kubernetes Service Commands")
public class ServiceCommand extends AbstractShellComponent {
    private final CommandlinePrinter commandlinePrinter;
    private final ComponentProvider componentProvider;
    private final CoreV1Api coreV1Api;
    private final FileService fileService;

    public ServiceCommand(final CommandlinePrinter commandlinePrinter,
                          final ComponentProvider componentProvider,
                          final CoreV1Api coreV1Api,
                          final FileService fileService) {
        this.commandlinePrinter = commandlinePrinter;
        this.componentProvider = componentProvider;
        this.coreV1Api = coreV1Api;
        this.fileService = fileService;
    }

    @ShellMethod(key = {LIST_SERVICES_SHORT_CMD, LIST_SERVICES_LONG_CMD}, value = LIST_SERVICES_HELP)
    public void list() throws ApiException {
        final var serviceList = getServices(Optional.empty());

        final var headers = List.of("Namespace", "Name", "Type", "Cluster Ip", "Port(s)", "Age");
        final var items = serviceList.stream()
                .map(this::toRow)
                .collect(Collectors.toList());

        final var result = componentProvider.renderTable(headers, items);
        commandlinePrinter.print(result, Ansi.Color.GREEN);
    }

    private String[] toRow(final V1Service v1Service) {
        final var metadata = Objects.requireNonNull(v1Service.getMetadata());
        final var spec = Objects.requireNonNull(v1Service.getSpec());

        return new String[]{
                metadata.getNamespace(),
                metadata.getName(),
                spec.getType(),
                spec.getClusterIP(),
                Objects.requireNonNull(spec.getPorts()).stream()
                        .map(p -> p.getPort() + "/" + p.getProtocol())
                        .collect(Collectors.joining(", ")),
                Calculator.calculateAge(metadata.getCreationTimestamp())
        };
    }

    @ShellMethod(key = {SELECT_SERVICE_SHORT_CMD, SELECT_SERVICE_LONG_CMD}, value = SELECT_SERVICE_HELP, prefix = "-")
    public void select(
            @ShellOption(value = {Command.DESCRIBE_SERVICE_SHORT_CMD, Command.DESCRIBE_SERVICE_LONG_CMD},
                    help = Command.DESCRIBE_SERVICE_HELP,
                    optOut = true,
                    defaultValue = "false") final boolean describe,

            @ShellOption(value = {Command.OUTPUT_SERVICE_SHORT_CMD, Command.OUTPUT_SERVICE_LONG_CMD},
                    help = Command.OUTPUT_SERVICE_HELP,
                    optOut = true,
                    defaultValue = "") final String output,

            @ShellOption(value = {Command.FORMAT_SERVICE_SHORT_CMD, Command.FORMAT_SERVICE_LONG_CMD},
                    help = Command.FORMAT_SERVICE_HELP,
                    optOut = true,
                    defaultValue = "yml") final String format,

            @ShellOption(value = {Command.OPEN_SERVICE_SHORT_CMD, Command.OPEN_SERVICE_LONG_CMD},
                    help = Command.OPEN_SERVICE_HELP,
                    optOut = true,
                    defaultValue = "false") final boolean open,

            @ShellOption(value = {Command.NAMESPACE_SERVICE_SHORT_CMD, Command.NAMESPACE_SERVICE_LONG_CMD},
                    help = Command.NAMESPACE_SERVICE_HELP,
                    optOut = true,
                    defaultValue = "") final String namespace,

            @ShellOption(value = {Command.MEMORY_SERVICE_SHORT_CMD, Command.MEMORY_SERVICE_LONG_CMD},
                    help = Command.MEMORY_SERVICE_HELP,
                    optOut = true,
                    defaultValue = "false") final boolean memory

    ) throws ApiException {
        if (memory) {
            final var selectedService = KubernetesData.getInstance().getService();
            checkAndApplyOptions(selectedService, describe, output, format, open);
            return;
        }

        final var namespaceOpt = Optional.ofNullable(namespace).filter(s -> !s.isEmpty() && !s.isBlank());
        final var serviceSelectorItemList = getServices(namespaceOpt).stream()
                .map(s -> SelectorItem.of(Objects.requireNonNull(s.getMetadata()).getName(), s))
                .collect(Collectors.toList());

        final var context = new ComponentProvider.SingleSelectionBuilder<V1Service>()
                .setTerminal(getTerminal())
                .setResourceLoader(getResourceLoader())
                .setTemplateExecutor(getTemplateExecutor())
                .setItems(serviceSelectorItemList)
                .setMessage("Select a secret")
                .setPrintResults(false)
                .build();

        final var selectedService = context.run(SingleItemSelector.SingleItemSelectorContext.empty())
                .getResultItem()
                .flatMap(si -> Optional.ofNullable(si.getItem()))
                .orElseThrow(() -> new ItemNotFoundException("No service selected."));

        KubernetesData.getInstance().setService(selectedService);
        commandlinePrinter.printSuccess("Service is selected: " + Objects.requireNonNull(selectedService.getMetadata()).getName());

        checkAndApplyOptions(selectedService, describe, output, format, open);
    }

    private void checkAndApplyOptions(final V1Service service, final boolean describe, final String output, final String format, final boolean open) {
        if (open) {
            commandlinePrinter.printKubernetesObject(service, format);
        }

        if (describe) {
            printDescribe(service);
        }

        if (output != null && !output.isEmpty() && !output.isBlank()) {
            fileService.exportKubernetesObjectWithFormat(service, format, output);
        }
    }

    @SuppressWarnings("all")
    private void printDescribe(final V1Service service) {
        commandlinePrinter.print("Name", service.getMetadata().getName());
        commandlinePrinter.print("Namespace", service.getMetadata().getNamespace());
        commandlinePrinter.print("Labels", service.getMetadata().getLabels().toString());
        commandlinePrinter.print("Annotations", service.getMetadata().getAnnotations() != null ? service.getMetadata().getAnnotations().toString() : "None");

        commandlinePrinter.print("Selector", service.getSpec().getSelector().toString());

        commandlinePrinter.print("Type", service.getSpec().getType());
        commandlinePrinter.print("Cluster IP", service.getSpec().getClusterIP());
        commandlinePrinter.print("IP Family Policy", service.getSpec().getIpFamilyPolicy());
        commandlinePrinter.print("IP Families", service.getSpec().getIpFamilies().toString());

        final var ports = service.getSpec().getPorts();
        if (ports != null) {
            for (V1ServicePort port : ports) {
                commandlinePrinter.print("Port", port.getPort() + "/" + port.getProtocol());
                commandlinePrinter.print("TargetPort", port.getTargetPort().toString());
            }
        }

        commandlinePrinter.print("Endpoints", service.getSpec().getClusterIPs().toString());
        commandlinePrinter.print("Session Affinity", service.getSpec().getSessionAffinity());
        commandlinePrinter.print("Internal Traffic Policy", service.getSpec().getInternalTrafficPolicy());
    }

    private List<V1Service> getServices(final Optional<String> namespaceOpt) throws ApiException {
        final var namespace = namespaceOpt.orElseGet(KubernetesData.getInstance()::getNamespace);
        if (namespace == null) {
            throw new ItemNotFoundException("Namespace not selected.");
        }

        final var serviceList = coreV1Api.listNamespacedService(namespace).executeWithHttpInfo().getData().getItems();
        if (serviceList.isEmpty()) {
            throw new ItemNotFoundException("No service found in namespace: " + namespace);
        }

        return serviceList;
    }
}
