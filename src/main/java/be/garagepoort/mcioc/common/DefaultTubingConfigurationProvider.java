package be.garagepoort.mcioc.common;

import be.garagepoort.mcioc.ConditionalOnMissingBean;
import be.garagepoort.mcioc.IocBean;
import be.garagepoort.mcioc.TubingPlugin;
import be.garagepoort.mcioc.configuration.files.ConfigMigrator;
import be.garagepoort.mcioc.configuration.files.ConfigurationFile;
import be.garagepoort.mcioc.load.InjectTubingPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.List;

@IocBean
@ConditionalOnMissingBean
public class DefaultTubingConfigurationProvider implements TubingConfigurationProvider {

    private TubingPlugin tubingPlugin;

    public DefaultTubingConfigurationProvider(@InjectTubingPlugin TubingPlugin tubingPlugin) {
        this.tubingPlugin = tubingPlugin;
    }

    @Override
    public List<ConfigMigrator> getConfigurationMigrators() {
        return Collections.emptyList();
    }

    @Override
    public List<ConfigurationFile> getConfigurationFiles() {
        InputStream defConfigStream = getResource(tubingPlugin, "config.yml");
        if (defConfigStream != null) {
            return Collections.singletonList(new ConfigurationFile("config.yml"));
        } else {
            return Collections.emptyList();
        }
    }

    private InputStream getResource(TubingPlugin tubingPlugin, String filename) {
        if (filename == null) {
            throw new IllegalArgumentException("Filename cannot be null");
        } else {
            try {
                URL url = tubingPlugin.getPluginClassLoader().getResource(filename);
                if (url == null) {
                    return null;
                } else {
                    URLConnection connection = url.openConnection();
                    connection.setUseCaches(false);
                    return connection.getInputStream();
                }
            } catch (IOException var4) {
                return null;
            }
        }
    }
}
