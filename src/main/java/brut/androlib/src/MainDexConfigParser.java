package brut.androlib.src;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created by changye.zeng on 17/3/17.
 * 新增的工具类
 */
public class MainDexConfigParser {
    private static final String SMALI_TYPE = ".smali";
    private static final String ANY_SMALI = "**";
    private static final String SMALI_SUGGEST = "-suggest";

    private Set<String> includeFilesSet;
    private Set<String> suggestFilesSet;

    public String getIncludeFilesRegex() {
        if (includeFilesSet == null || includeFilesSet.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        Iterator<String> iterator = includeFilesSet.iterator();
        if (iterator.hasNext()) {
            sb.append(iterator.next());
        }
        while (iterator.hasNext()) {
            sb.append("|");
            sb.append(iterator.next());
        }
        return sb.toString();
    }

    public String getSuggestFilesRegex() {
        if (suggestFilesSet == null || suggestFilesSet.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        Iterator<String> iterator = suggestFilesSet.iterator();
        if (iterator.hasNext()) {
            sb.append(iterator.next());
        }
        while (iterator.hasNext()) {
            sb.append("|");
            sb.append(iterator.next());
        }
        return sb.toString();
    }

    public void parseMainDexFiles(String path) throws Exception {
        includeFilesSet = new HashSet<>();
        suggestFilesSet = new HashSet<>();
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
                boolean isMatchSuggest = false;
                if (matchCommand(classPath, SMALI_SUGGEST)) {
                    isMatchSuggest = true;
                    classPath = classPath.substring(SMALI_SUGGEST.length(), classPath.length());
                } else {
                    isMatchSuggest = false;
                }
                classPath = classPath.trim();
                if (classPath.endsWith(SMALI_TYPE)) {
                    classPath = classPath.substring(0, classPath.length() - SMALI_TYPE.length())
                            .replace('.', '/') + "\\.smali";
                    if (isMatchSuggest) {
                        suggestFilesSet.add(classPath);
                    } else {
                        includeFilesSet.add(classPath);
                    }
                } else if (classPath.endsWith(ANY_SMALI)) {
                    String dirPath = classPath.substring(0, classPath.length() - ANY_SMALI.length())
                            .replace('.', '/')
                            + ".*\\.smali";
                    if (isMatchSuggest) {
                        suggestFilesSet.add(dirPath);
                    } else {
                        includeFilesSet.add(dirPath);
                    }
                }
            }
        }
        reader.close();
    }

    private boolean matchCommand(String text, String cmd) {
        Pattern pattern = Pattern.compile("^" + cmd + "\\s+");
        return pattern.matcher(text).find();
    }
}
