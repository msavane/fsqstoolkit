package util;

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
