package nuricanozturk.dev.k8shell.k8s;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Deployment;
import io.kubernetes.client.util.Yaml;
import nuricanozturk.dev.k8shell.Calculator;
import nuricanozturk.dev.k8shell.ComponentProvider;
import nuricanozturk.dev.k8shell.KubernetesData;
import nuricanozturk.dev.k8shell.ShellPrinter;
import nuricanozturk.dev.k8shell.exception.ItemNotFoundException;
import org.fusesource.jansi.Ansi;
import org.springframework.shell.component.SingleItemSelector;
import org.springframework.shell.component.support.SelectorItem;
import org.springframework.shell.standard.*;

import java.io.File;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@ShellComponent
@ShellCommandGroup("Kubernetes Deployment Commands")
public class DeploymentCommand extends AbstractShellComponent {
    private final ShellPrinter shellPrinter;
    private final ComponentProvider componentProvider;
    private final CoreV1Api coreApi;
    private final AppsV1Api appsApi;

    public DeploymentCommand(ShellPrinter shellPrinter, ComponentProvider componentProvider, CoreV1Api coreApi, AppsV1Api appsApi) {
        this.shellPrinter = shellPrinter;
        this.componentProvider = componentProvider;
        this.coreApi = coreApi;
        this.appsApi = appsApi;
    }

    @ShellMethod(key = {"list deployments", "ld"}, value = "List deployments in the current namespace")
    public void listDeployments() throws ApiException {
        final var deployments = getDeployments();

        final var headers = List.of("Namespace", "Name", "Ready", "Up-to-date", "Available", "Age");
        final var items = deployments.stream()
                .map(this::toRow)
                .collect(Collectors.toList());

        final var result = componentProvider.toTable(headers, items);
        shellPrinter.print(result, Ansi.Color.GREEN);
    }

    @ShellMethod(key = {"sd", "select deployment"}, value = "Select a deployment", prefix = "-")
    public void selectDeployment(
            @ShellOption(value = "d", help = "Describe the selected deployment", optOut = true, defaultValue = "false") boolean describe,
            @ShellOption(value = "o", help = "Output the selected deployment", optOut = true, defaultValue = "") String output,
            @ShellOption(value = "f", help = "Output format", optOut = true, defaultValue = "yml") String format,
            @ShellOption(value = "s", help = "Open the selected deployment in the browser", optOut = true, defaultValue = "false") boolean open,
            @ShellOption(value = "m", help = "Use remembered deployment object", optOut = true, defaultValue = "false") boolean memory
    ) throws ApiException {
        if (memory) {
            final var selectedDeployment = KubernetesData.getInstance().getDeployment();
            checkAndApplyOptions(selectedDeployment, describe, output, format, open);
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
                .setDefaultItem(null)
                .setPrintResults(false)
                .build();

        final var selectedDeployment = context.run(SingleItemSelector.SingleItemSelectorContext.empty())
                .getResultItem()
                .flatMap(si -> Optional.ofNullable(si.getItem()))
                .orElseThrow(() -> new ItemNotFoundException("No deployment selected."));

        KubernetesData.getInstance().setDeployment(selectedDeployment);
        shellPrinter.printSuccess("Deployment is selected: " + Objects.requireNonNull(selectedDeployment.getMetadata()).getName());

        checkAndApplyOptions(selectedDeployment, describe, output, format, open);
    }

    private void checkAndApplyOptions(V1Deployment selectedDeployment, boolean describe, String output, String format, boolean open) {
        if (open) {
            openDeploymentWithFormat(selectedDeployment, format);
        }

        if (describe) {
            printDescribe(selectedDeployment);
        }

        if (output != null && !output.isEmpty() && !output.isBlank()) {
            exportDeployment(selectedDeployment, format, output);
        }
    }

    public void openDeploymentWithFormat(V1Deployment selectedDeployment, String format) {
        try {
            var fileFormat = "yaml";
            if (format == null || format.isBlank() || (!format.equalsIgnoreCase("json") && !format.equalsIgnoreCase("yaml") && !format.equalsIgnoreCase("yml"))) {
                throw new IllegalArgumentException("Invalid format. Use 'json' or 'yaml'");
            } else {
                fileFormat = format.toLowerCase();
            }
            if (fileFormat.equals("json")) {
                ObjectMapper jsonMapper = new ObjectMapper();
                String jsonOutput = jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(selectedDeployment);
                System.out.println("JSON Output:\n" + jsonOutput);
            } else {
                String yamlOutput = Yaml.dump(selectedDeployment);
                System.out.println("YAML Output:\n" + yamlOutput);
            }
        } catch (Exception e) {
            System.err.println("Error printing deployment: " + e.getMessage());
        }
    }

    public void exportDeployment(V1Deployment deployment, String format, String output) {
        try {
            // Dosya formatını belirle
            var fileFormat = "yaml";
            if (format == null || format.isBlank() || (!format.equalsIgnoreCase("json") && !format.equalsIgnoreCase("yaml") && !format.equalsIgnoreCase("yml"))) {
                throw new IllegalArgumentException("Invalid format. Use 'json' or 'yaml'");
            } else {
                fileFormat = format.toLowerCase();
            }

            // ObjectMapper'ı oluştur ve JSR310 modülünü ekle
            ObjectMapper mapper;
            if (fileFormat.equals("json")) {
                mapper = new ObjectMapper();
            } else {
                mapper = new ObjectMapper(new YAMLFactory());
            }

            // Java 8 tarih/saat türlerini desteklemek için JavaTimeModule ekleyin
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            // Deployment nesnesini belirtilen dosya yoluna yazdır
            mapper.writeValue(new File(output), deployment);
            System.out.println("Deployment exported successfully to " + fileFormat.toUpperCase() + " format at: " + output);
        } catch (IOException e) {
            System.out.println("An error occurred while exporting the deployment: " + e.getMessage());
        }
    }

    private void printDescribe(V1Deployment deployment) {
        // Metadata
        shellPrinter.print("Name: ", deployment.getMetadata().getName());
        shellPrinter.print("Namespace: ", deployment.getMetadata().getNamespace());
        shellPrinter.print("Labels: ", deployment.getMetadata().getLabels().toString());
        shellPrinter.print("Annotations: ", deployment.getMetadata().getAnnotations().toString());
        shellPrinter.print("Selector: ", deployment.getSpec().getSelector().getMatchLabels().toString());

        // Spec
        shellPrinter.print("\nReplicas: ", Optional.ofNullable(deployment.getSpec().getReplicas()).orElse(0).toString());
        shellPrinter.print("Strategy: ", deployment.getSpec().getStrategy().getType());

        // Pod Template
        final var template = deployment.getSpec().getTemplate();
        shellPrinter.print("\nPod Template:", Ansi.Color.GREEN);
        shellPrinter.print("  Containers:", Ansi.Color.GREEN);
        template.getSpec().getContainers().forEach(container -> {
            shellPrinter.print("    Name: ", container.getName());
            shellPrinter.print("    Image: ", container.getImage());
            shellPrinter.print("    Ports: ", container.getPorts().toString());
            shellPrinter.print("    Resources: ", container.getResources().toString());
        });

        // Status
        final var status = deployment.getStatus();
        shellPrinter.print("\nStatus:", Ansi.Color.GREEN);
        shellPrinter.print("  Available Replicas: ", Optional.ofNullable(status.getAvailableReplicas()).orElse(0).toString());
        shellPrinter.print("  Ready Replicas: ", Optional.ofNullable(status.getReadyReplicas()).orElse(0).toString());
        shellPrinter.print("  Updated Replicas: ", Optional.ofNullable(status.getUpdatedReplicas()).orElse(0).toString());
        shellPrinter.print("  Conditions:", Ansi.Color.GREEN);

        for (var condition : status.getConditions()) {
            shellPrinter.print("    Type: ", condition.getType());
            shellPrinter.print("    Status: ", condition.getStatus());
            shellPrinter.print("    Last Transition Time: ", Objects.requireNonNull(condition.getLastTransitionTime()).format(DateTimeFormatter.ISO_DATE_TIME));
            shellPrinter.print("    Reason: ", condition.getReason());
            shellPrinter.print("    Message: ", condition.getMessage());
            System.out.println();
        }
    }

    private String[] toRow(V1Deployment v1Deployment) {
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
