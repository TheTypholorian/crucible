package net.typho.crucible.installer;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

// written in java so we don't have to bundle kotlin stdlib
public class CrucibleInstaller {
    public static void main(String[] args) throws IOException, URISyntaxException, NoSuchAlgorithmException, InterruptedException {
        var propertiesFile = Paths.get("installer.crucible.properties").toAbsolutePath();
        var properties = new Properties();

        try (InputStream in = Files.newInputStream(propertiesFile)) {
            properties.load(in);
        }

        var version = properties.get("version");
        URL url;

        if (version == null) {
            url = new URI((String) Objects.requireNonNull(properties.get("url"), "Must specify either 'version' or 'url' in " + propertiesFile)).toURL();
        } else {
            url = new URI("https://typho.net/maven/net/typho/crucible.wrapper/" + version + "/crucible.wrapper-" + version + ".jar").toURL();
        }

        var cacheDir = Paths.get(System.getProperty("user.home"), ".crucible", "caches", "installer");
        Files.createDirectories(cacheDir);

        Path jarFile;

        if (url.getProtocol().equals("file")) {
            jarFile = Paths.get(url.toURI());
        } else {
            jarFile = cacheDir.resolve(HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(url.toString().getBytes())) + ".jar");

            if (!Files.exists(jarFile)) {
                try (InputStream in = url.openStream()) {
                    Files.copy(in, jarFile, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }

        var arguments = new ArrayList<String>();
        arguments.add("java");
        arguments.add("-jar");
        arguments.add(jarFile.toString());
        arguments.addAll(Arrays.asList(args));

        System.exit(
                new ProcessBuilder(arguments)
                        .inheritIO()
                        .start()
                        .waitFor()
        );
    }
}