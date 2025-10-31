package neutron;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ResourceExtractor {

    public static void ensureOnFilesystem(String resourcePath) {
        try {
         extractToMainDir(findMainClass(), resourcePath);
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract resource: " + resourcePath, e);
        }
    }

    static Class<?> findMainClass() {
        try {
            String mainClassName = System.getProperty("sun.java.command");
            if (mainClassName != null) {
                // The property may contain arguments, e.g. "com.app.Main arg1 arg2"
                String realClassName = mainClassName.split(" ")[0];
                return Class.forName(realClassName);
            }
        } catch (Exception ignored) { }
        return ResourceExtractor.class; // fallback
    }

    /**
     * Extracts a resource directory (like "ui/index.html") to the same folder where the main class is located.
     * Automatically determines if resources come from a JAR or the file system.
     */
    private static void extractToMainDir(Class<?> mainClass, String resourcePath) throws Exception {
        if (!resourcePath.startsWith("/")) {
            resourcePath = "/" + resourcePath;
        }
        URL resourceUrl = mainClass.getResource(resourcePath);
        if (resourceUrl == null) {
            throw new FileNotFoundException("Resource not found: " + resourcePath);
        }
        if (!resourceUrl.getProtocol().equals("jar")) {
            return;
        }
        String basePath = resourcePath.substring(0, resourcePath.lastIndexOf('/') + 1);
        URL mainClassUrl = mainClass.getProtectionDomain().getCodeSource().getLocation();
        
        var mainClassUrlpath = mainClassUrl.getPath();
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            mainClassUrlpath = mainClassUrlpath.substring(1);
            mainClassUrlpath = mainClassUrlpath.replace('/', '\\');
        }
        Path mainDir = Paths.get(URLDecoder.decode( mainClassUrlpath, "UTF-8")).toAbsolutePath();

        if (Files.isRegularFile(mainDir)) {
            mainDir = mainDir.getParent();
        }
        Path targetDir = mainDir.resolve(basePath.replaceFirst("^/", ""));
        extractFromJar(resourceUrl, basePath, targetDir);
    }

    private static void extractFromJar(URL resourceUrl, String basePath, Path targetDir) throws IOException {
        String jarPathStr = resourceUrl.getPath().substring(5, resourceUrl.getPath().indexOf("!"));
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            jarPathStr = jarPathStr.substring(1);
            jarPathStr = jarPathStr.replace('/', '\\');
        }
        Path jarPath = Paths.get(URLDecoder.decode(jarPathStr, "UTF-8"));

        try (JarFile jar = new JarFile(jarPath.toFile())) {
            for (JarEntry entry : jar.stream().toList()) {
                if (entry.getName().startsWith(basePath.substring(1)) && !entry.isDirectory()) {
                    Path outPath = targetDir.resolve(entry.getName().substring(basePath.length() - 1));
                    Files.createDirectories(outPath.getParent());
                    if (!Files.exists(outPath)) {
                        try (InputStream in = jar.getInputStream(entry)) {
                            Files.copy(in, outPath);
                        }
                    }
                }
            }
        }
    }
}
