package com.justlel.tgbot.configs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.justlel.tgbot.configs.yamls.YamlInterface;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Manager of the yaml files.
 * In order to load the Yaml files onto objects, the class uses the {@link com.fasterxml.jackson.dataformat.yaml} package.
 * This class cannot be instantiated: to get an instance of it, use the {@link #getInstance()} method.
 * The method responsible for the loading of the yaml files is {@link #loadYaml}.
 * The method responsible for the dump of properties into the yaml files is {@link #dumpYaml}.
 * The default config files are obtained from the "resources" directory using the {@link #getResource} method.
 *
 * @author just
 * @version 1.0
 * @see YamlInterface
 */
public class YamlManager {

    /**
     * Instance of the class. Retrieved by the method {@link #getInstance()}.
     */
    private static YamlManager yamlManager;
    /**
     * ObjectMapper used to load the yaml files. Instance of the class {@link ObjectMapper}.
     */
    private final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    /**
     * Path to the directory where the yaml files are stored.
     */
    private String configDirectory = "configs";


    /**
     * Private constructor.
     * Use the method {@link #getInstance()} to get an instance of the class.
     *
     * @throws IllegalAccessException If the class is instantiated a second time.
     */
    private YamlManager() throws IllegalAccessException {
        if (yamlManager != null)
            throw new IllegalAccessException("YamlManager is already instantiated");
        yamlManager = this;
    }

    /**
     * Get an instance of this class. If the instance is already created, returns it, otherwise creates it.
     *
     * @return Instance of the class.
     */
    public static YamlManager getInstance() {
        if (yamlManager == null) {
            try {
                return new YamlManager();
            } catch (IllegalAccessException ignored) {
            }
        }
        return yamlManager;
    }

    /**
     * Returns the resource with the given name.
     * Loads an InputStream from the file <i>"filename"</i> in the <i>"resources"</i> directory.
     * If the file is not present, returns null.
     *
     * @param filename Name of the file.
     * @return An {@link InputStream} loaded from the "resources" directory, or null if the file is not present.
     */
    public static @Nullable InputStream getResource(String filename) {
        try {
            return Objects.requireNonNull(YamlInterface.class.getClassLoader().getResourceAsStream(filename));
        } catch (NullPointerException e) {
            return null;
        }
    }

    /**
     * Load a Yaml file using a given class as model.
     * The given class must be a child of {@link YamlInterface}, in order to be used as model of a Yaml file.
     * By passing a {@link Class} as an argument, the method won't return an object; instead, the properties
     * of the Yaml file will be loaded into the given class assuming that the parameters can be accessed
     * via static Getters methods.
     * In order to load the Yaml file into the class, however, an object must be created, and thus the
     * setter properties must be accessible via instance methods. Check Jackson's documentation for more information.
     * The filename is given by the {@link YamlInterface#getFilename} method, and if the file doesn't exist,
     * it will be created by copying the one with same name present in the "resources" directory.
     * In case the resource file also doesn't exist, the method will throw an exception.
     * The Yaml properties are loaded using the {@link ObjectMapper#readValue} method.
     * Once loaded, the properties are checked using the {@link YamlInterface#checkConfigValidity()}} method.
     *
     * @param clazz Class representing the model of the Yaml file to be loaded.
     * @throws IOException              If the Yaml file is not present in the "resources" directory.
     * @throws IllegalArgumentException If the Yaml file is not valid.
     * @throws RuntimeException         If the given class cannot be instantiated.
     */
    public void loadYaml(Class<? extends YamlInterface> clazz) throws IOException, IllegalArgumentException, RuntimeException {
        try {
            String filename = clazz.getConstructor().newInstance().getFilename();
            this.validateConfig(filename);
            mapper.readValue(Files.newBufferedReader(new File(getConfigDirectory() + "/" + filename).toPath()), clazz).checkConfigValidity();
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Loads the properties of a given class into a Yaml file.
     * The given class must be a child of {@link YamlInterface}, in order to be used as model of a Yaml file.
     * By passing a {@link Class} as an argument, the properties are assumed to have been defined in a static way.
     * In order to load the properties into the Yaml file, however, an object must be created, so that
     * the class can call the {@link YamlInterface#getDumpableData} method.
     * The filename is given by the {@link YamlInterface#getFilename} method.
     * If the method getDumpableData returns null, an exception is thrown.
     * The final file will be located at "configDirectory/filename".
     *
     * @param clazz Class from which the dumpable properties are obtained.
     * @throws IOException              If an error occurs while writing on the file.
     * @throws IllegalArgumentException If the class has no dumpable data.
     */
    public void dumpYaml(Class<? extends YamlInterface> clazz) throws IOException, IllegalArgumentException {
        try {
            YamlInterface YamlInterface = clazz.getConstructor().newInstance();
            if (YamlInterface.getDumpableData() == null)
                throw new IllegalArgumentException("Dumpable data non definito");
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(getConfigDirectory() + "/" + YamlInterface.getFilename()), YamlInterface.getDumpableData());
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
                 InvocationTargetException e) {
            throw new IllegalArgumentException(e);
        }// Jackson prevents the creation of a class without an empty constructor.
    }

    /**
     * Verify that the file in "configDirectory/filename" is valid.
     * If the file does not exist, the default one found in "resources/filename" is copied to "configDirectory/filename".
     * If the resource file is also not found, or invalid, an exception is thrown.
     * If the config directory is not readable, an exception is thrown.
     *
     * @param filename Name of the Yaml file.
     * @throws IllegalArgumentException If a default Yaml file is not found in the "resources" dir.
     * @throws IOException              In case an error occurs while reading files.
     */
    private void validateConfig(String filename) throws IllegalArgumentException, IOException {
        Path filePath = Path.of(getConfigDirectory() + "/" + filename);
        if (!Files.exists(filePath)) {
            Files.createFile(filePath);
            InputStream resource = getResource(filename);
            if (resource == null)
                throw new IllegalArgumentException("Config file " + filename + " non trovato nella cartella resources..");
            if (!new File(getConfigDirectory()).isDirectory() && !new File(getConfigDirectory()).mkdir())
                throw new IOException("Impossibile creare la cartella config");
            Files.writeString(filePath, new String(resource.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    /**
     * Get the path to the config directory.
     *
     * @return Path to the config directory.
     */
    public String getConfigDirectory() {
        return configDirectory;
    }

    /**
     * Set the path to the config directory.
     *
     * @param directory Path to the config directory.
     * @throws IOException If the directory cannot be created.
     */
    public void setConfigDirectory(String directory) throws IOException {
        File dir = new File(directory);
        if (!dir.isDirectory() && !dir.mkdir())
            throw new IOException("The " + directory + " directory cannot be created.");
        configDirectory = directory;
    }
}
