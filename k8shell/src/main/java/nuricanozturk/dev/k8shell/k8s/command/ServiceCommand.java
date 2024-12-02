package nuricanozturk.dev.k8shell.k8s.command;

import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.openapi.models.V1ServicePort;
import nuricanozturk.dev.k8shell.component.ComponentProvider;
import nuricanozturk.dev.k8shell.file.FileService;
import nuricanozturk.dev.k8shell.k8s.AgeCalculator;
import nuricanozturk.dev.k8shell.k8s.CommandInfo;
import nuricanozturk.dev.k8shell.k8s.KubernetesData;
import nuricanozturk.dev.k8shell.printer.CommandlinePrinter;
import nuricanozturk.dev.k8shell.exception.ItemNotFoundException;
import org.fusesource.jansi.Ansi;
import org.springframework.shell.component.SingleItemSelector;
import org.springframework.shell.component.support.SelectorItem;
import org.springframework.shell.standard.*;


import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static nuricanozturk.dev.k8shell.k8s.CommandInfo.*;

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
                AgeCalculator.calculate(metadata.getCreationTimestamp())
        };
    }

    @ShellMethod(key = {SELECT_SERVICE_SHORT_CMD, SELECT_SERVICE_LONG_CMD}, value = SELECT_SERVICE_HELP, prefix = "-")
    public void select(
            @ShellOption(value = {CommandInfo.DESCRIBE_SERVICE_SHORT_CMD, CommandInfo.DESCRIBE_SERVICE_LONG_CMD},
                    help = CommandInfo.DESCRIBE_SERVICE_HELP,
                    optOut = true,
                    defaultValue = "false") final boolean describe,

            @ShellOption(value = {CommandInfo.OUTPUT_SERVICE_SHORT_CMD, CommandInfo.OUTPUT_SERVICE_LONG_CMD},
                    help = CommandInfo.OUTPUT_SERVICE_HELP,
                    optOut = true,
                    defaultValue = "") final String output,

            @ShellOption(value = {CommandInfo.FORMAT_SERVICE_SHORT_CMD, CommandInfo.FORMAT_SERVICE_LONG_CMD},
                    help = CommandInfo.FORMAT_SERVICE_HELP,
                    optOut = true,
                    defaultValue = "yml") final String format,

            @ShellOption(value = {CommandInfo.OPEN_SERVICE_SHORT_CMD, CommandInfo.OPEN_SERVICE_LONG_CMD},
                    help = CommandInfo.OPEN_SERVICE_HELP,
                    optOut = true,
                    defaultValue = "false") final boolean open,

            @ShellOption(value = {CommandInfo.NAMESPACE_SERVICE_SHORT_CMD, CommandInfo.NAMESPACE_SERVICE_LONG_CMD},
                    help = CommandInfo.NAMESPACE_SERVICE_HELP,
                    optOut = true,
                    defaultValue = "") final String namespace,

            @ShellOption(value = {CommandInfo.MEMORY_SERVICE_SHORT_CMD, CommandInfo.MEMORY_SERVICE_LONG_CMD},
                    help = CommandInfo.MEMORY_SERVICE_HELP,
                    optOut = true,
                    defaultValue = "false") final boolean memory

    ) throws ApiException {
        if (memory) {
            final var selectedService = KubernetesData.getInstance().getService();
            checkAndApplyOptions(selectedService, describe, output, format, open);
            return;
        }

        final var namespaceOpt = Optional.ofNullable(namespace).filter(s -> !s.isBlank());
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

        if (output != null && !output.isBlank()) {
            writeFile(service, format, output);
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

    private void writeFile(final V1Service service, final String format, final String output) {
        final var fileWriteResponse = fileService.exportKubernetesObjectWithFormat(service, format, output);

        if (fileWriteResponse.containsKey(true)) {
            commandlinePrinter.printSuccess(fileWriteResponse.get(true));
        } else {
            commandlinePrinter.printError(fileWriteResponse.get(false));
        }
    }
}
