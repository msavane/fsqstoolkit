package util;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class FileDiscoveryUtil {

    public static List<Path> discoverTestFiles(String folderName, String extension) throws IOException {
        List<Path> matchingFiles = new ArrayList<>();

        // Locate the folder within the classpath
        URL folderUrl = Thread.currentThread().getContextClassLoader().getResource(folderName);
        if (folderUrl == null) {
            System.err.println("❌ Folder not found in classpath: " + folderName);
            return matchingFiles;
        }

        Path folderPath;
        try {
            // Convert URL to Path safely (handles spaces, special chars)
            folderPath = Paths.get(folderUrl.toURI());
        } catch (URISyntaxException e) {
            System.err.println("❌ Invalid folder URL: " + e.getMessage());
            return matchingFiles;
        }

        try (Stream<Path> paths = Files.walk(folderPath)) {
            paths.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith(extension))
                    .forEach(matchingFiles::add);
        }

        return matchingFiles;
    }
}

/*package util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class FileDiscoveryUtil {

    public static List<Path> discoverTestFiles(String directory, String extension) throws IOException {
        return Files.list(Paths.get(directory))
                .filter(path -> path.toString().endsWith(extension))
                .collect(Collectors.toList());
    }
}
*/