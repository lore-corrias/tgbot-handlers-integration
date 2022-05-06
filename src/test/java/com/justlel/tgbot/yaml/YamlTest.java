package com.justlel.tgbot.yaml;

import com.justlel.tgbot.configs.YamlManager;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.*;

public class YamlTest {

    private final YamlManager yamlManager = YamlManager.getInstance();

    @Test
    public void assertConfigDirectory() throws IOException {
        yamlManager.setConfigDirectory("configs_test");
        assertTrue(new File("configs_test").exists());
    }

    @Test
    public void assertResourcesDirectory() throws IOException {
        InputStream inputStream = YamlManager.getResource("test_config.yml");
        try (InputStream defaultStream = this.getClass().getClassLoader().getResourceAsStream("test_config.yml")) {
            assertNotNull(defaultStream);
            assertNotNull(inputStream);
            assertArrayEquals(inputStream.readAllBytes(), defaultStream.readAllBytes());
        }
    }

    @Test
    public void assertConfigLoad() throws IOException, RuntimeException {
        yamlManager.setConfigDirectory("configs_test");
        yamlManager.loadYaml(TestYaml.class);
        assertEquals("Hello World!", TestYaml.getTestProperty());
    }
}
