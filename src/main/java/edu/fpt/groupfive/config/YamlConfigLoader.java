package edu.fpt.groupfive.config;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

public class YamlConfigLoader {

    public static Map<String, Object> load() {
        Yaml yaml = new Yaml();
        InputStream inputStream =
                YamlConfigLoader.class
                        .getClassLoader()
                        .getResourceAsStream("application.yml");

        if (inputStream == null) {
            throw new RuntimeException("Không tìm thấy application.yml");
        }

        return yaml.load(inputStream);
    }
}
