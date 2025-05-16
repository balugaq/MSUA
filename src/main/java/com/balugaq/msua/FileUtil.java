package com.balugaq.msua;

import java.io.File;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class FileUtil {
    public static final File pluginFolder = MSUA.instance().getDataFolder().getParentFile();

    public static File getJarFile(Class<?> clazz) {
        try {
            URL codeSourceUrl = clazz.getProtectionDomain().getCodeSource().getLocation();
            String decodedPath = URLDecoder.decode(codeSourceUrl.getPath(), StandardCharsets.UTF_8);
            if (decodedPath.startsWith("/") && System.getProperty("os.name").contains("Windows")) {
                decodedPath = decodedPath.substring(1);
            }
            File jarFile = new File(decodedPath);
            return jarFile.isFile() ? jarFile : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
