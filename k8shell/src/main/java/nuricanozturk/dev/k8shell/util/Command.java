package nuricanozturk.dev.k8shell.util;

public final class Command {

    private Command() {
    }

    // SELECT_CONFIG
    public static final String SELECT_CONFIG_SHORT_CMD = "scp";
    public static final String SELECT_CONFIG_LONG_CMD = "select config";
    public static final String SELECT_CONFIG_HELP = "Change the current configuration file in .kube directory";

    // LIST_CONFIG
    public static final String LIST_CONFIG_SHORT_CMD = "lcp";
    public static final String LIST_CONFIG_LONG_CMD = "list config";
    public static final String LIST_CONFIG_HELP = "Show available configuration files in .kube directory";

    // LIST_PODS
    public static final String LIST_PODS_SHORT_CMD = "lp";
    public static final String LIST_PODS_LONG_CMD = "list pods";
    public static final String LIST_PODS_HELP = "List all pods in the current namespace";

    // SELECT_POD
    public static final String SELECT_POD_SHORT_CMD = "sp";
    public static final String SELECT_POD_LONG_CMD = "select pod";
    public static final String SELECT_POD_HELP = "Select a specific pod to view details";

    // LIST_DEPLOYMENTS
    public static final String LIST_DEPLOYMENTS_SHORT_CMD = "ld";
    public static final String LIST_DEPLOYMENTS_LONG_CMD = "list deployments";
    public static final String LIST_DEPLOYMENTS_HELP = "List all deployments in the current namespace";

    // SELECT_DEPLOYMENT
    public static final String SELECT_DEPLOYMENT_SHORT_CMD = "sd";
    public static final String SELECT_DEPLOYMENT_LONG_CMD = "select deployment";
    public static final String SELECT_DEPLOYMENT_HELP = "Select a specific deployment";

    // SELECT_NAMESPACE
    public static final String SELECT_NAMESPACE_SHORT_CMD = "sn";
    public static final String SELECT_NAMESPACE_LONG_CMD = "select namespace";
    public static final String SELECT_NAMESPACE_HELP = "Switch to a different namespace";

    // LIST_NAMESPACES
    public static final String LIST_NAMESPACES_SHORT_CMD = "ln";
    public static final String LIST_NAMESPACES_LONG_CMD = "list namespaces";
    public static final String LIST_NAMESPACES_HELP = "List all available namespaces";

    // SELECT_SECRET
    public static final String SELECT_SECRET_SHORT_CMD = "ss";
    public static final String SELECT_SECRET_LONG_CMD = "select secret";
    public static final String SELECT_SECRET_HELP = "Select a specific secret to view or modify";

    // LIST_SECRETS
    public static final String LIST_SECRETS_SHORT_CMD = "ls";
    public static final String LIST_SECRETS_LONG_CMD = "list secrets";
    public static final String LIST_SECRETS_HELP = "List all secrets in the current namespace";

    // LIST_SERVICES
    public static final String LIST_SERVICES_SHORT_CMD = "lsv";
    public static final String LIST_SERVICES_LONG_CMD = "list services";
    public static final String LIST_SERVICES_HELP = "List all services in the current namespace";

    // SELECT_SERVICE
    public static final String SELECT_SERVICE_SHORT_CMD = "ssv";
    public static final String SELECT_SERVICE_LONG_CMD = "select service";
    public static final String SELECT_SERVICE_HELP = "Select a specific service to view details";

    // DESCRIBE SERVICE
    public static final String DESCRIBE_SERVICE_SHORT_CMD = "-d";
    public static final String DESCRIBE_SERVICE_LONG_CMD = "--describe";
    public static final String DESCRIBE_SERVICE_HELP = "Show detailed information about the selected service";

    // OUTPUT SERVICE
    public static final String OUTPUT_SERVICE_SHORT_CMD = "-o";
    public static final String OUTPUT_SERVICE_LONG_CMD = "--output";
    public static final String OUTPUT_SERVICE_HELP = "Specify the file to save the selected service details";

    // FORMAT SERVICE
    public static final String FORMAT_SERVICE_SHORT_CMD = "-f";
    public static final String FORMAT_SERVICE_LONG_CMD = "--format";
    public static final String FORMAT_SERVICE_HELP = "Specify the output format (e.g., yml, json)";

    // OPEN SERVICE
    public static final String OPEN_SERVICE_SHORT_CMD = "-b";
    public static final String OPEN_SERVICE_LONG_CMD = "--open-browser";
    public static final String OPEN_SERVICE_HELP = "Open the selected service URL in the default browser";

    // NAMESPACE SERVICE
    public static final String NAMESPACE_SERVICE_SHORT_CMD = "-n";
    public static final String NAMESPACE_SERVICE_LONG_CMD = "--namespace";
    public static final String NAMESPACE_SERVICE_HELP = "Specify the namespace of the service";

    // MEMORY SERVICE
    public static final String MEMORY_SERVICE_SHORT_CMD = "-m";
    public static final String MEMORY_SERVICE_LONG_CMD = "--memory";
    public static final String MEMORY_SERVICE_HELP = "Use previously saved service object from memory";

    // DECODE BASE64
    public static final String DECODE_BASE64_SHORT_CMD = "-d";
    public static final String DECODE_BASE64_LONG_CMD = "--decode";
    public static final String DECODE_BASE64_HELP = "Decode base64 encoded data";

    // SHOW SECRETS IN TABLE
    public static final String SHOW_TABLE_SHORT_CMD = "-t";
    public static final String SHOW_TABLE_LONG_CMD = "--show-table";
    public static final String SHOW_TABLE_HELP = "List decoded secrets in a table format";

    // SHOW SECRETS AS TEXT
    public static final String SHOW_TEXT_SHORT_CMD = "-s";
    public static final String SHOW_TEXT_LONG_CMD = "--show-text";
    public static final String SHOW_TEXT_HELP = "List decoded secrets as plain text";

    // USE MEMORIZED SECRET
    public static final String REMEMBER_SECRET_SHORT_CMD = "-m";
    public static final String REMEMBER_SECRET_LONG_CMD = "--memory";
    public static final String REMEMBER_SECRET_HELP = "Use the memorized secret object from memory";

    // DESCRIBE DEPLOYMENT
    public static final String DESCRIBE_DEPLOYMENT_SHORT_CMD = "-d";
    public static final String DESCRIBE_DEPLOYMENT_LONG_CMD = "--describe";
    public static final String DESCRIBE_DEPLOYMENT_HELP = "Describe the selected deployment";

    // OUTPUT DEPLOYMENT
    public static final String OUTPUT_DEPLOYMENT_SHORT_CMD = "-o";
    public static final String OUTPUT_DEPLOYMENT_LONG_CMD = "--output";
    public static final String OUTPUT_DEPLOYMENT_HELP = "Output the selected deployment details";

    // FORMAT DEPLOYMENT
    public static final String FORMAT_DEPLOYMENT_SHORT_CMD = "-f";
    public static final String FORMAT_DEPLOYMENT_LONG_CMD = "--format";
    public static final String FORMAT_DEPLOYMENT_HELP = "Specify the output format (e.g., yml, json)";

    // OPEN DEPLOYMENT IN BROWSER
    public static final String OPEN_DEPLOYMENT_SHORT_CMD = "-b";
    public static final String OPEN_DEPLOYMENT_LONG_CMD = "--open-browser";
    public static final String OPEN_DEPLOYMENT_HELP = "Open the selected deployment in the default browser";

    // USE MEMORIZED DEPLOYMENT
    public static final String MEMORY_DEPLOYMENT_SHORT_CMD = "-m";
    public static final String MEMORY_DEPLOYMENT_LONG_CMD = "--memory";
    public static final String MEMORY_DEPLOYMENT_HELP = "Use the remembered deployment object from memory";

    // SHOW KUBERNETES INFO
    public static final String SHOW_INFO_KEY_SHORT = "si";
    public static final String SHOW_INFO_KEY_LONG = "show info";
    public static final String SHOW_INFO_DESC = "Prints Kubernetes information";

    // CLEAR KUBERNETES INFO
    public static final String CLEAR_INFO_KEY_SHORT = "ci";
    public static final String CLEAR_INFO_KEY_LONG = "clear info";
    public static final String CLEAR_INFO_DESC = "Clears Kubernetes information";

    // CLEAR POD INFO
    public static final String CLEAR_POD_KEY_SHORT = "cp";
    public static final String CLEAR_POD_KEY_LONG = "clear pod";
    public static final String CLEAR_POD_DESC = "Clears pod information";

    // CLEAR DEPLOYMENT INFO
    public static final String CLEAR_DEPLOYMENT_KEY_SHORT = "cd";
    public static final String CLEAR_DEPLOYMENT_KEY_LONG = "clear deployment";
    public static final String CLEAR_DEPLOYMENT_DESC = "Clears deployment information";

    // CLEAR SECRET INFO
    public static final String CLEAR_SECRET_KEY_SHORT = "cs";
    public static final String CLEAR_SECRET_KEY_LONG = "clear secret";
    public static final String CLEAR_SECRET_DESC = "Clears secret information";

    // CLEAR SERVICE INFO
    public static final String CLEAR_SERVICE_KEY_SHORT = "cse";
    public static final String CLEAR_SERVICE_KEY_LONG = "clear service";
    public static final String CLEAR_SERVICE_DESC = "Clears service information";

    // CLEAR NAMESPACE INFO
    public static final String CLEAR_NAMESPACE_KEY_SHORT = "cn";
    public static final String CLEAR_NAMESPACE_KEY_LONG = "clear namespace";
    public static final String CLEAR_NAMESPACE_DESC = "Clears namespace information";

    // CLEAR CONFIG PATH INFO
    public static final String CLEAR_CONFIG_PATH_KEY_SHORT = "ccp";
    public static final String CLEAR_CONFIG_PATH_KEY_LONG = "clear config path";
    public static final String CLEAR_CONFIG_PATH_DESC = "Clears config path information";

    // EXIT APPLICATION
    public static final String EXIT_KEY = "q";
    public static final String EXIT_DESC = "Exit application";
}
