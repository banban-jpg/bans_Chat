package egovframework.example.common.fixture;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

/** Reads checked-in synthetic PSV resources for offline tests and the Oracle seed source. */
public final class FixtureRows {
    private FixtureRows() { }

    public static List<String[]> read(String name, int columns) {
        String resource = "/fixtures/" + name;
        try (var stream = FixtureRows.class.getResourceAsStream(resource)) {
            if (stream == null) throw new IllegalStateException("Missing synthetic fixture: " + name);
            try (var reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                var lines = reader.lines().filter(line -> !line.isBlank() && !line.startsWith("#")).toList();
                if (lines.isEmpty()) throw new IllegalStateException("Empty synthetic fixture: " + name);
                return lines.stream().skip(1).map(line -> {
                    String[] fields = line.split("\\|", -1);
                    if (fields.length != columns) throw new IllegalStateException("Invalid synthetic fixture: " + name);
                    return fields;
                }).toList();
            }
        } catch (java.io.IOException exception) {
            throw new IllegalStateException("Cannot read synthetic fixture: " + name);
        }
    }

    public static String optional(String value) {
        return "-".equals(value) ? null : value;
    }
}
