package com.nanocraft.game.tile;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

final class ResourceLoader {

    private ResourceLoader() {
    }

    static String resolveResourcePath(String mapFilePath, String relativePath) {
        String normalizedRelative = relativePath.replace("\\", "/");
        if (normalizedRelative.startsWith("/")) {
            return normalizedRelative;
        }

        int lastSlash = mapFilePath.lastIndexOf('/');
        String mapDir = lastSlash >= 0 ? mapFilePath.substring(0, lastSlash + 1) : "/";
        String combined = mapDir + normalizedRelative;
        String normalized = Paths.get(combined).normalize().toString().replace('\\', '/');
        return normalized.startsWith("/") ? normalized : "/" + normalized;
    }

    static BufferedImage loadScaledImage(String resourcePath, int tileSize) {
        try {
            BufferedImage image = ImageIO.read(getRequiredResourceStream(resourcePath));
            return scaleImage(image, tileSize, tileSize);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load image resource: " + resourcePath, e);
        }
    }

    static InputStream getRequiredResourceStream(String resourcePath) throws IOException {
        InputStream stream = openResourceStream(resourcePath);
        if (stream == null) {
            throw new IOException("Resource not found: " + resourcePath);
        }
        return stream;
    }

    static boolean resourceExists(String resourcePath) {
        try (InputStream ignored = openResourceStream(resourcePath)) {
            return ignored != null;
        } catch (IOException e) {
            return false;
        }
    }

    static InputStream openResourceStream(String resourcePath) throws IOException {
        if (resourcePath == null || resourcePath.isBlank()) {
            return null;
        }

        String normalized = resourcePath.replace("\\", "/");

        InputStream stream = ResourceLoader.class.getResourceAsStream(normalized);
        if (stream != null) {
            return stream;
        }

        String withoutLeadingSlash = normalized.startsWith("/") ? normalized.substring(1) : normalized;
        stream = ResourceLoader.class.getClassLoader().getResourceAsStream(withoutLeadingSlash);
        if (stream != null) {
            return stream;
        }

        if (withoutLeadingSlash.startsWith("res/")) {
            String strippedResPrefix = withoutLeadingSlash.substring("res/".length());
            stream = ResourceLoader.class.getClassLoader().getResourceAsStream(strippedResPrefix);
            if (stream != null) {
                return stream;
            }

            stream = ResourceLoader.class.getResourceAsStream("/" + strippedResPrefix);
            if (stream != null) {
                return stream;
            }
        }

        List<Path> candidates = new ArrayList<>();
        candidates.add(Paths.get(withoutLeadingSlash));
        if (!withoutLeadingSlash.startsWith("res/")) {
            candidates.add(Paths.get("res", withoutLeadingSlash));
        }
        if (withoutLeadingSlash.startsWith("res/")) {
            candidates.add(Paths.get(withoutLeadingSlash.substring("res/".length())));
        }

        for (Path candidate : candidates) {
            if (Files.isRegularFile(candidate)) {
                return Files.newInputStream(candidate);
            }
        }

        return null;
    }

    static String fileName(String resourcePath) {
        int lastSlash = resourcePath.lastIndexOf('/');
        return lastSlash >= 0 ? resourcePath.substring(lastSlash + 1) : resourcePath;
    }

    static BufferedImage scaleImage(BufferedImage original, int width, int height) {
        BufferedImage scaledImage = new BufferedImage(width, height, original.getType());
        Graphics2D g2 = scaledImage.createGraphics();
        g2.drawImage(original, 0, 0, width, height, null);
        g2.dispose();
        return scaledImage;
    }
}
