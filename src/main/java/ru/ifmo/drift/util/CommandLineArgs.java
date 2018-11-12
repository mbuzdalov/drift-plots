package ru.ifmo.drift.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CommandLineArgs {
    private Map<String, String> values = new HashMap<>();
    private Set<String> options = new HashSet<>();

    public CommandLineArgs(String[] args) {
        for (String s : args) {
            if (s.startsWith("--")) {
                int equal = s.indexOf('=');
                if (equal == -1) {
                    options.add(s.substring(2));
                } else {
                    String key = s.substring(2, equal);
                    String value = s.substring(equal + 1);
                    values.put(key, value);
                }
            }
        }
    }

    public String getOption(String key, String defaultValue) {
        return values.getOrDefault(key, defaultValue);
    }

    public boolean isOptionSet(String option) {
        return options.contains(option);
    }
}
