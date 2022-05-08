package io.github.justlel.samplebot;

import io.github.justlel.tgbot.configs.yamls.YamlInterface;
import org.jetbrains.annotations.Nullable;


public class SampleConfig implements YamlInterface {

    private static String token;


    public SampleConfig() {
    }

    public static String getToken() {
        return token;
    }

    public void setToken(String token) {
        SampleConfig.token = token;
    }

    @Override
    public String getFilename() {
        return "sample-configs.yml";
    }

    @Override
    public void checkConfigValidity() throws IllegalArgumentException {
    }

    @Nullable
    @Override
    public Object getDumpableData() {
        return null;
    }
}
