package brut.androlib.src;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by changye.zeng on 17/3/17.
 * 新增的工具类
 */
public class MainDexConfigParser {
    private static final String SMALI_TYPE = ".smali";
    private static final String ANY_SMALI = "**";

    public static String getMainDexFilesRegex(String path) throws Exception {
        Set<String> files = getMainDexFiles(path);
        StringBuilder sb = new StringBuilder();
        Iterator<String> iterator = files.iterator();
        if (iterator.hasNext()) {
            sb.append(iterator.next());
        }
        while (iterator.hasNext()) {
            sb.append("|");
            sb.append(iterator.next());
        }
        return sb.toString();
    }

    private static Set<String> getMainDexFiles(String path) throws Exception {
        Set<String> files = new HashSet<>();
        File configFile = new File(path);

        BufferedReader reader = new BufferedReader(new FileReader(configFile));
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.length() == 0) {
                continue;
            }

            // # 是注释
            int rem = line.indexOf('#');
            if (rem != -1) {
                if (rem == 0) {
                    continue;
                } else {
                    line = line.substring(0, rem).trim();
                }
            }

            String classPath = line.trim();
            if (classPath.length() > 0) {
                classPath = classPath.trim();
                if (classPath.endsWith(SMALI_TYPE)) {
                    classPath = classPath.substring(0, classPath.length() - SMALI_TYPE.length())
                            .replace('.', '/') + "\\.smali";
                    files.add(classPath);
                } else if (classPath.endsWith(ANY_SMALI)) {
                    String dirPath = classPath.substring(0, classPath.length() - ANY_SMALI.length())
                            .replace('.', '/')
                            + ".*\\.smali";
                    files.add(dirPath);
                }
            }
        }
        reader.close();
        return files;
    }
}
