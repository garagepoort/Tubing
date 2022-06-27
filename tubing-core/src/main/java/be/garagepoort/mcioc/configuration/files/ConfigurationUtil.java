package be.garagepoort.mcioc.configuration.files;

import be.garagepoort.mcioc.TubingPlugin;
import be.garagepoort.mcioc.configuration.yaml.configuration.file.FileConfiguration;
import be.garagepoort.mcioc.configuration.yaml.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class ConfigurationUtil {

    private ConfigurationUtil() {
    }

    public static void saveConfigFile(TubingPlugin tubingPlugin, String configurationFile) {
        File dataFolder = tubingPlugin.getDataFolder();
        String fullConfigResourcePath = (configurationFile).replace('\\', '/');

        InputStream in = getResource(fullConfigResourcePath);
        if (in == null) {
            tubingPlugin.getLogger().log(Level.SEVERE, "Could not find configuration file " + fullConfigResourcePath);
            return;
        }

        File outFile = new File(dataFolder, fullConfigResourcePath);
        int lastIndex = fullConfigResourcePath.lastIndexOf(47);
        File outDir = new File(dataFolder, fullConfigResourcePath.substring(0, Math.max(lastIndex, 0)));
        if (!outDir.exists()) {
            outDir.mkdirs();
        }

        try {
            if (!outFile.exists()) {
                try (OutputStream out = new FileOutputStream(outFile)) {
                    byte[] buf = new byte[1024];

                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                }
                in.close();
            }
        } catch (IOException var10) {
            tubingPlugin.getLogger().log(Level.SEVERE, "Could not save " + outFile.getName() + " to " + outFile, var10);
        }
    }

    private static InputStream getResource(String filename) {
        try {
            URL url = ConfigurationUtil.class.getClassLoader().getResource(filename);
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

    public static FileConfiguration loadConfiguration(TubingPlugin plugin, String path) {
        File file = Paths.get(plugin.getDataFolder() + File.separator + path).toFile();

        YamlConfiguration config = new YamlConfiguration();
        try {
            config.load(file);
        } catch (Exception e) {
            throw new ConfigurationException("Cannot be.garagepoort.mcioc.tubingvelocity.load " + file, e);
        }

        return config;
    }

    public static Map<String, String> loadFilters(String filtersString) {
        Map<String, String> filterMap = new HashMap<>();
        if (filtersString != null) {
            String[] split = filtersString.split(";");
            for (String filter : split) {
                String[] filterPair = filter.split("=");
                filterMap.put(filterPair[0].toLowerCase(), filterPair[1].toLowerCase());
            }
        }
        return filterMap;
    }
}
