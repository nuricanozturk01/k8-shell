package nuricanozturk.dev.k8shell.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class PropertyService {
    private final AtomicReference<String> configPath;

    @Value("${k8s-shell.config-path.default}")
    private String configPathStr;

    @PostConstruct
    public void init() {
        configPathStr = configPathStr.replace("/", File.separator);
    }

    public PropertyService() {
        this.configPath = new AtomicReference<>(configPathStr);
    }

    public void setConfigPath(final String value) {
        configPath.set(value);
    }

    public String getConfigPath() {
        return configPath.get();
    }
}