package nuricanozturk.dev.k8shell;

public final class KubernetesData {

    private String namespace;
    private String podName;
    private String configPath;

    private KubernetesData() {
    }

    private static final class InstanceHolder {
        private static final KubernetesData instance = new KubernetesData();
    }

    public static KubernetesData getInstance() {
        return InstanceHolder.instance;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getPodName() {
        return podName;
    }

    public void setPodName(String podName) {
        this.podName = podName;
    }

    public String getConfigPath() {
        return configPath;
    }

    public void setConfigPath(String configPath) {
        this.configPath = configPath;
    }
}
