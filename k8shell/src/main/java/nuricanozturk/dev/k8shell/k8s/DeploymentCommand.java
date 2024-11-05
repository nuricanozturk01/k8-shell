package nuricanozturk.dev.k8shell.k8s;

import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.models.V1Deployment;
import nuricanozturk.dev.k8shell.*;
import nuricanozturk.dev.k8shell.component.ComponentProvider;
import nuricanozturk.dev.k8shell.exception.ItemNotFoundException;
import nuricanozturk.dev.k8shell.util.Calculator;
import nuricanozturk.dev.k8shell.util.Command;
import org.fusesource.jansi.Ansi;
import org.springframework.shell.component.SingleItemSelector;
import org.springframework.shell.component.support.SelectorItem;
import org.springframework.shell.standard.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static nuricanozturk.dev.k8shell.util.Command.*;

@ShellComponent
@ShellCommandGroup("Kubernetes Deployment Commands")
public class DeploymentCommand extends AbstractShellComponent {

    private final CommandlinePrinter commandlinePrinter;
    private final ComponentProvider componentProvider;
    private final AppsV1Api appsApi;
    private final FileService fileService;

    public DeploymentCommand(final CommandlinePrinter commandlinePrinter,
                             final ComponentProvider componentProvider,
                             final AppsV1Api appsApi,
                             final FileService fileService) {
        this.commandlinePrinter = commandlinePrinter;
        this.componentProvider = componentProvider;
        this.appsApi = appsApi;
        this.fileService = fileService;
    }

    @ShellMethod(key = {LIST_DEPLOYMENTS_SHORT_CMD, LIST_DEPLOYMENTS_LONG_CMD}, value = LIST_DEPLOYMENTS_HELP)
    public void listDeployments() throws ApiException {
        final var headers = List.of("Namespace", "Name", "Ready", "Up-to-date", "Available", "Age");
        final var items = getDeployments().stream().map(this::toRow).collect(Collectors.toList());

        final var renderedTable = componentProvider.renderTable(headers, items);
        commandlinePrinter.print(renderedTable, Ansi.Color.GREEN);
    }

    @ShellMethod(key = {SELECT_DEPLOYMENT_SHORT_CMD, SELECT_DEPLOYMENT_LONG_CMD}, value = SELECT_DEPLOYMENT_HELP, prefix = "-")
    public void selectDeployment(
            @ShellOption(value = {Command.DESCRIBE_DEPLOYMENT_SHORT_CMD, Command.DESCRIBE_DEPLOYMENT_LONG_CMD},
                    help = Command.DESCRIBE_DEPLOYMENT_HELP,
                    optOut = true,
                    defaultValue = "false") final boolean describe,

            @ShellOption(value = {Command.OUTPUT_DEPLOYMENT_SHORT_CMD, Command.OUTPUT_DEPLOYMENT_LONG_CMD},
                    help = Command.OUTPUT_DEPLOYMENT_HELP,
                    optOut = true,
                    defaultValue = "") final String output,

            @ShellOption(value = {Command.FORMAT_DEPLOYMENT_SHORT_CMD, Command.FORMAT_DEPLOYMENT_LONG_CMD},
                    help = Command.FORMAT_DEPLOYMENT_HELP,
                    optOut = true,
                    defaultValue = "yml") final String format,

            @ShellOption(value = {Command.OPEN_DEPLOYMENT_SHORT_CMD, Command.OPEN_DEPLOYMENT_LONG_CMD},
                    help = Command.OPEN_DEPLOYMENT_HELP,
                    optOut = true,
                    defaultValue = "false") final boolean open,

            @ShellOption(value = {Command.MEMORY_DEPLOYMENT_SHORT_CMD, Command.MEMORY_DEPLOYMENT_LONG_CMD},
                    help = Command.MEMORY_DEPLOYMENT_HELP,
                    optOut = true,
                    defaultValue = "false") final boolean memory

    ) throws ApiException {
        if (memory) {
            final var selectedDeployment = KubernetesData.getInstance().getDeployment();
            checkAndApplyOptions(selectedDeployment, describe, output, format, open);
            return;
        }

        final var deploymentSelectorItemList = getDeployments().stream()
                .map(s -> SelectorItem.of(Objects.requireNonNull(s.getMetadata()).getName(), s))
                .collect(Collectors.toList());

        final var context = new ComponentProvider.SingleSelectionBuilder<V1Deployment>()
                .setTerminal(getTerminal())
                .setResourceLoader(getResourceLoader())
                .setTemplateExecutor(getTemplateExecutor())
                .setItems(deploymentSelectorItemList)
                .setMessage("Select a secret")
                .setPrintResults(false)
                .build();

        final var selectedDeployment = context.run(SingleItemSelector.SingleItemSelectorContext.empty())
                .getResultItem()
                .flatMap(si -> Optional.ofNullable(si.getItem()))
                .orElseThrow(() -> new ItemNotFoundException("No deployment selected."));

        KubernetesData.getInstance().setDeployment(selectedDeployment);
        commandlinePrinter.printSuccess("Deployment is selected: " + Objects.requireNonNull(selectedDeployment.getMetadata()).getName());

        checkAndApplyOptions(selectedDeployment, describe, output, format, open);
    }

    private void checkAndApplyOptions(final V1Deployment selectedDeployment,
                                      final boolean describe,
                                      final String output,
                                      final String format,
                                      final boolean open) {
        if (open) {
            commandlinePrinter.printKubernetesObject(selectedDeployment, format);
        }

        if (describe) {
            printDescribe(selectedDeployment);
        }

        if (output != null && !output.isEmpty() && !output.isBlank()) {
            fileService.exportKubernetesObjectWithFormat(selectedDeployment, format, output);
        }
    }

    @SuppressWarnings("all")
    private void printDescribe(final V1Deployment deployment) {
        commandlinePrinter.print("Name: ", deployment.getMetadata().getName());
        commandlinePrinter.print("Namespace: ", deployment.getMetadata().getNamespace());
        commandlinePrinter.print("Labels: ", deployment.getMetadata().getLabels().toString());
        commandlinePrinter.print("Annotations: ", deployment.getMetadata().getAnnotations().toString());
        commandlinePrinter.print("Selector: ", deployment.getSpec().getSelector().getMatchLabels().toString());

        commandlinePrinter.print("\nReplicas: ", Optional.ofNullable(deployment.getSpec().getReplicas()).orElse(0).toString());
        commandlinePrinter.print("Strategy: ", deployment.getSpec().getStrategy().getType());

        final var template = deployment.getSpec().getTemplate();
        commandlinePrinter.print("\nPod Template:", Ansi.Color.GREEN);
        commandlinePrinter.print("  Containers:", Ansi.Color.GREEN);
        template.getSpec().getContainers().forEach(container -> {
            commandlinePrinter.print("    Name: ", container.getName());
            commandlinePrinter.print("    Image: ", container.getImage());
            commandlinePrinter.print("    Ports: ", container.getPorts().toString());
            commandlinePrinter.print("    Resources: ", container.getResources().toString());
        });

        final var status = deployment.getStatus();
        commandlinePrinter.print("\nStatus:", Ansi.Color.GREEN);
        commandlinePrinter.print("  Available Replicas: ", Optional.ofNullable(status.getAvailableReplicas()).orElse(0).toString());
        commandlinePrinter.print("  Ready Replicas: ", Optional.ofNullable(status.getReadyReplicas()).orElse(0).toString());
        commandlinePrinter.print("  Updated Replicas: ", Optional.ofNullable(status.getUpdatedReplicas()).orElse(0).toString());
        commandlinePrinter.print("  Conditions:", Ansi.Color.GREEN);

        for (var condition : status.getConditions()) {
            commandlinePrinter.print("    Type: ", condition.getType());
            commandlinePrinter.print("    Status: ", condition.getStatus());
            commandlinePrinter.print("    Last Transition Time: ", Objects.requireNonNull(condition.getLastTransitionTime()).format(DateTimeFormatter.ISO_DATE_TIME));
            commandlinePrinter.print("    Reason: ", condition.getReason());
            commandlinePrinter.print("    Message: ", condition.getMessage());
            System.out.println();
        }
    }

    private String[] toRow(final V1Deployment v1Deployment) {
        final var metadata = v1Deployment.getMetadata();
        final var status = v1Deployment.getStatus();
        final var spec = v1Deployment.getSpec();
        final var age = Calculator.calculateAge(Objects.requireNonNull(metadata).getCreationTimestamp());

        return new String[]{
                metadata.getNamespace(),
                metadata.getName(),
                Objects.requireNonNull(status).getReadyReplicas() + "/" + Objects.requireNonNull(spec).getReplicas(),
                Objects.requireNonNull(status.getUpdatedReplicas()).toString(),
                Objects.requireNonNull(status.getAvailableReplicas()).toString(),
                age
        };
    }


    private List<V1Deployment> getDeployments() throws ApiException {
        final var namespace = KubernetesData.getInstance().getNamespace();

        if (namespace == null) {
            throw new ItemNotFoundException("Namespace not selected.");
        }

        final var deploymentList = appsApi.listNamespacedDeployment(namespace).executeWithHttpInfo().getData().getItems();

        if (deploymentList.isEmpty()) {
            throw new ItemNotFoundException("No deployment found in namespace: " + namespace);
        }

        return deploymentList;
    }
}
