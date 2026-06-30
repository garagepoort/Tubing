package be.garagepoort.mcioc.configuration;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import be.garagepoort.mcioc.diagnostics.TubingDiagnosticException;

public class ConfigInjectionContext {

    private final Class<?> beanClass;
    private final String target;
    private final Class<?> expectedType;
    private final String property;
    private final ConfigTransformer configTransformer;

    public ConfigInjectionContext(Class<?> beanClass, String target, Class<?> expectedType, String property, ConfigTransformer configTransformer) {
        this.beanClass = beanClass;
        this.target = target;
        this.expectedType = expectedType;
        this.property = property;
        this.configTransformer = configTransformer;
    }

    public TubingDiagnosticException missingRequired(String customError) {
        return new TubingDiagnosticException(
            customError == null || customError.isEmpty() ? "Missing required configuration" : customError,
            baseDetails(null),
            Collections.singletonList("Add '" + property + "' to the configured yaml file.")
        );
    }

    public TubingDiagnosticException conversionFailed(Object value, Throwable cause) {
        return new TubingDiagnosticException(
            "Invalid configuration value",
            baseDetails(value),
            Collections.singletonList("Update '" + property + "' to a value compatible with " + expectedType.getSimpleName() + "."),
            cause
        );
    }

    public TubingDiagnosticException transformerFailed(Class<? extends IConfigTransformer> transformerClass, Object value, Throwable cause) {
        return new TubingDiagnosticException(
            "Configuration transformer failed",
            withTransformerDetails(value, transformerClass),
            Collections.singletonList("Check the value for '" + property + "' and the transformer " + transformerClass.getName() + "."),
            cause
        );
    }

    public String getProperty() {
        return property;
    }

    private List<String> baseDetails(Object value) {
        java.util.ArrayList<String> details = new java.util.ArrayList<>();
        details.add("Bean: " + beanClass.getName());
        details.add("Target: " + target);
        details.add("Property: " + property);
        details.add("Expected type: " + expectedType.getName());
        if (value != null) {
            details.add("Actual value: " + value + " (" + value.getClass().getName() + ")");
        }
        if (configTransformer != null) {
            details.add("Transformers: " + Arrays.stream(configTransformer.value()).map(Class::getName).collect(Collectors.joining(", ")));
        }
        return details;
    }

    private List<String> withTransformerDetails(Object value, Class<? extends IConfigTransformer> transformerClass) {
        List<String> details = baseDetails(value);
        details.add("Failed transformer: " + transformerClass.getName());
        return details;
    }
}
