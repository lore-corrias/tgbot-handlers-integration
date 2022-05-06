package io.github.justlel.yaml;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.justlel.configs.yamls.YamlInterface;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class TestYaml implements YamlInterface {
    @JsonProperty("hello")
    private static String testProperty;

    public TestYaml() {
    }

    @JsonProperty("hello")
    public static String getTestProperty() {
        return testProperty;
    }

    @JsonProperty("hello")
    public void setTestProperty(String hello) {
        TestYaml.testProperty = hello;
    }

    @Override
    public String getFilename() {
        return "test_config.yml";
    }

    @Override
    public void checkConfigValidity() throws IllegalArgumentException {
        if (testProperty == null)
            throw new IllegalArgumentException("testProperty is null");
    }

    @Nullable
    @Override
    public Object getDumpableData() {
        return new HashMap<String, String>() {{
            put("test_property", testProperty);
        }};
    }
}