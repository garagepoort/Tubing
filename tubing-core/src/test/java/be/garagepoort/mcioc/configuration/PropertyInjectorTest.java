package be.garagepoort.mcioc.configuration;

import be.garagepoort.mcioc.configuration.yaml.configuration.file.FileConfiguration;
import be.garagepoort.mcioc.configuration.yaml.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PropertyInjectorTest {

    @Test
    void injectsYamlIntegerIntoLongSetter() {
        TestConfig config = new TestConfig();
        FileConfiguration fileConfiguration = new YamlConfiguration();
        fileConfiguration.set("time", 10);

        PropertyInjector.injectConfigurationProperties(config, Collections.singletonMap("config", fileConfiguration));

        assertEquals(10L, config.time);
    }

    @Test
    void injectsYamlIntegerIntoNumericWrapperFields() {
        TestConfig config = new TestConfig();
        FileConfiguration fileConfiguration = new YamlConfiguration();
        fileConfiguration.set("wrapper-long", 10);
        fileConfiguration.set("wrapper-double", 10);

        PropertyInjector.injectConfigurationProperties(config, Collections.singletonMap("config", fileConfiguration));

        assertEquals(Long.valueOf(10L), config.wrapperLong);
        assertEquals(Double.valueOf(10.0D), config.wrapperDouble);
    }

    private static class TestConfig {
        private long time;

        @ConfigProperty("config:time")
        public void setTime(long time) {
            this.time = time;
        }

        @ConfigProperty("config:wrapper-long")
        private Long wrapperLong;

        @ConfigProperty("config:wrapper-double")
        private Double wrapperDouble;
    }
}
