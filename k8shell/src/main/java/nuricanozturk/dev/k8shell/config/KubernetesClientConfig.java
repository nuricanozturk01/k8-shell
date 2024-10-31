package nuricanozturk.dev.k8shell.config;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Configuration;

import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.util.Config;

import nuricanozturk.dev.k8shell.KubernetesData;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import java.io.*;

@Component
public class KubernetesClientConfig {
    public static final String BEAN_API_CLIENT = "API_CLIENT";
    private final PropertyService propertyService;

    public KubernetesClientConfig(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    @Bean(BEAN_API_CLIENT)
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public ApiClient getApiClient() throws IOException {
        var client = Config.fromConfig(propertyService.getProperty());
        client.setVerifyingSsl(false);
        Configuration.setDefaultApiClient(client);
        return client;
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public CoreV1Api getCoreV1API(@Qualifier(BEAN_API_CLIENT) ApiClient client) {
        KubernetesData.getInstance().setConfigPath(propertyService.getProperty());
        return new CoreV1Api(client);
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public AppsV1Api getAppsV1API(@Qualifier(BEAN_API_CLIENT) ApiClient client) {
        return new AppsV1Api(client);
    }
}
