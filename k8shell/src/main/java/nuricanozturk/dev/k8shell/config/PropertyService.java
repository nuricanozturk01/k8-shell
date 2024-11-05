package nuricanozturk.dev.k8shell.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class PropertyService {
    private final AtomicReference<String> configPath;

    public PropertyService(@Value("${k8s-shell.config-path.default}") final String configPath) {
        final var path = configPath.replace("/", File.separator);
        this.configPath = new AtomicReference<>(path);
    }

    public void setConfigPath(final String value) {
        configPath.set(value);
    }

    public String getConfigPath() {
        return configPath.get();
    }
}