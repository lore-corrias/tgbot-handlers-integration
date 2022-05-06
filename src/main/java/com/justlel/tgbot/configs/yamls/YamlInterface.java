package com.justlel.tgbot.configs.yamls;


import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * Interface for all classes who are abstractions of a Yaml configuration file.
 * This interface can be used to define a class that will be used to read a Yaml configuration file,
 * in order to easily access its properties at any time.
 *
 * @author just
 * @version 1.0
 */
public interface YamlInterface {

    /**
     * Returns the filename of the Yaml file, with the ".yaml" extension included.
     * The path of the file is defined as "configs/{filename}".
     *
     * @return Name of the config File.
     */
    String getFilename();

    /**
     * Verify the validity of a config file.
     * This method should assert that the given yaml file is valid,
     * and throw an exception otherwise.
     * The check can also not be performed, and in this case the method can just return.
     *
     * @throws IllegalArgumentException In case a config file is invalid.
     */
    void checkConfigValidity() throws IllegalArgumentException, IOException;

    /**
     * Returns an object representing the properties to be written onto the Yaml file.
     * This method can return any kind of object, as Jackson can handle pretty much all kinds of data.
     * However, if the yaml file should not be rewritten at runtime, then this method can simply return "null".
     *
     * @return An object representing the properties to be written onto the Yaml file, or null if the file should not be rewritten.
     */
    @Nullable
    Object getDumpableData();
}
