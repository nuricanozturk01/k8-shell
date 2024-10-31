package nuricanozturk.dev.k8shell;

import io.kubernetes.client.openapi.models.V1Deployment;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1Secret;
import io.kubernetes.client.openapi.models.V1Service;

import java.util.*;

public final class KubernetesData {
    private static final KubernetesData INSTANCE = new KubernetesData();

    private String namespace;
    private String configPath;
    private V1Pod pod;
    private V1Deployment deployment;
    private V1Secret secret;
    private V1Service service;

    private KubernetesData() {
    }

    public static KubernetesData getInstance() {
        return INSTANCE;
    }

    public V1Deployment getDeployment() {
        return deployment;
    }

    public void setDeployment(final V1Deployment deployment) {
        this.deployment = deployment;
    }

    public V1Secret getSecret() {
        return secret;
    }

    public void setSecret(final V1Secret secret) {
        this.secret = secret;
    }

    public V1Service getService() {
        return service;
    }

    public void setService(final V1Service service) {
        this.service = service;
    }

    public V1Pod getPod() {
        return pod;
    }

    public void setPod(final V1Pod pod) {
        this.pod = pod;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(final String namespace) {
        this.namespace = namespace;
    }

    public String getConfigPath() {
        return configPath;
    }

    public void setConfigPath(final String configPath) {
        this.configPath = configPath;
    }

    public List<String[]> getAllData() {
        final var podName = Optional.ofNullable(pod).map(p -> p.getMetadata().getName()).orElse("N/A");
        final var serviceName = Optional.ofNullable(service).map(s -> s.getMetadata().getName()).orElse("N/A");
        final var deploymentName = Optional.ofNullable(deployment).map(d -> d.getMetadata().getName()).orElse("N/A");
        final var secretName = Optional.ofNullable(secret).map(s -> s.getMetadata().getName()).orElse("N/A");
        final var namespaceName = Optional.ofNullable(namespace).orElse("N/A");
        final var configPathName = Optional.ofNullable(configPath).orElse("N/A");

        return List.of(
                new String[]{"Pod", podName},
                new String[]{"Service", serviceName},
                new String[]{"Deployment", deploymentName},
                new String[]{"Secret", secretName},
                new String[]{"Namespace", namespaceName},
                new String[]{"Config Path", configPathName}
        );
    }

    public void clearData() {
        this.configPath = null;
        this.namespace = null;
        this.pod = null;
        this.service = null;
        this.deployment = null;
        this.secret = null;
    }

    public void changeNamespace(final String selectedNamespace) {
        KubernetesData.getInstance().setPod(null);
        KubernetesData.getInstance().setSecret(null);
        KubernetesData.getInstance().setService(null);
        KubernetesData.getInstance().setDeployment(null);
        KubernetesData.getInstance().setNamespace(selectedNamespace);
    }
}
