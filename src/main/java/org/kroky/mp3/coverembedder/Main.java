package org.kroky.mp3.coverembedder;

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.NotSupportedException;
import com.mpatric.mp3agic.UnsupportedTagException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {

    private static final Logger LOG = LogManager.getFormatterLogger(Main.class);

    public static void main(String[] args) throws IOException {
        String rootDirNamePath;

        if (args != null && args.length > 0) {
            rootDirNamePath = args[0];
        } else {
            throw new RuntimeException(
                    "Supply which directory to run this in (this will embed covers in all subdirs recursively)\n"
                            + "Example: java -jar mp3-cover-embedder.jar \"d:\\downloads\\my_album\"");
        }

        Path rootDir = Paths.get(rootDirNamePath);
        Files.walk(rootDir).filter(path -> path.getFileName().endsWith("cover.jpg"))
                .forEach(path -> embedCoverInDir(path));
    }

    private static void embedCoverInDir(Path coverFilePath) {
        try {
            byte[] imageData = IOUtils.toByteArray(Files.newInputStream(coverFilePath));
            Path parent = coverFilePath.getParent();
            Files.list(parent).filter(path -> !Files.isDirectory(path) && !path.getFileName().endsWith("cover.jpg"))
                    .forEach(file -> embedCover(file, imageData));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void embedCover(Path file, byte[] imageData) {
        LOG.info("Processing file: %s", file);

        try {
            Mp3File mp3file = new Mp3File(file);
            if (mp3file.hasId3v2Tag()) {
                mp3file.getId3v2Tag().setAlbumImage(imageData, "image/jpeg");
                String newName = file.resolveSibling("embedded_" + file.getFileName().toString()).toFile()
                        .getCanonicalPath();
                mp3file.save(newName);
            }
        } catch (UnsupportedTagException | InvalidDataException | IOException | NotSupportedException e) {
            throw new RuntimeException(e);
        }

    }

}
