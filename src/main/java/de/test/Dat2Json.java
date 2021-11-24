package de.test;

import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class Dat2Json {

    public static void main(String[] args) throws IOException {

        Map<String, Map<String, Object>> resultMap = new HashMap<>();
        Dat2Json d = new Dat2Json();

        d.handleFile(resultMap, "SchuelerBasisdaten.dat");
        d.handleFile(resultMap, "SchuelerZusatzdaten.dat");
        d.handleFile(resultMap, "SchuelerMerkmale.dat");
        d.handleFile(resultMap, "SchuelerBisherigeSchulen.dat");
        d.handleFile(resultMap, "SchuelerLeistungsdaten.dat");
        d.handleFile(resultMap, "SchuelerErzieher.dat");

        try (FileWriter w = new FileWriter("../fathi_dat2json/src/main/resources/result.json")) {
            w.append(new GsonBuilder().setPrettyPrinting()
                    .create().toJson(new ArrayList<>(resultMap.values())));
        }
    }

    private void handleFile(Map<String, Map<String, Object>> resultMap, String file) {
        Map<String, List<Map<String, Object>>> m = getContentByStudent(file);
        m.forEach((k, v) -> {
            // k = nachname|vorname|geburtsdatum
            // v = List<map with attributes>
            Map<String, Object> map = resultMap.get(k);
            if (map == null) {
                map = new HashMap<>();
                map.put("nachname", k.split("\\|")[0]);
                map.put("vorname", k.split("\\|")[1]);
                map.put("geburtsdatum", k.split("\\|")[2]);
                resultMap.put(k, map);
            }

            if (v.size() == 1) {
                map.put(file.substring(8, file.length() - 4)
                        .toLowerCase(Locale.ROOT), v.get(0));
            } else if (v.size() > 1) {
                map.put(file.substring(8, file.length() - 4)
                        .toLowerCase(Locale.ROOT), v);
            }
        });
    }

    private Map<String, List<Map<String, Object>>> getContentByStudent(String file) {
        List<String> lines = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/" + file)))
                .lines().parallel().collect(Collectors.toList());

        String[] headerAttributes = lines.get(0).split("\\|");

        Map<String, List<Map<String, Object>>> map = new HashMap<>();
        for (int i = 1; i < lines.size(); i++) {
            String[] values = lines.get(i).split("\\|");

            String key = values[0].trim() + "|" + values[1].trim() + "|" + values[2].trim();

            List<Map<String, Object>> listByStudent = map.get(key);
            if (listByStudent == null) {
                listByStudent = new ArrayList<>();
                map.put(key, listByStudent);
            }

            Map<String, Object> studentSet = new HashMap<>();
            listByStudent.add(studentSet);

            // skip first 3 elements
            for (int j = 3; j < headerAttributes.length; j++) {
                studentSet.put(mapKey(headerAttributes[j].trim()),
                        (j < values.length ? values[j].trim() : "")
                );
            }
        }
        return map;
    }

    private String mapKey(String key) {
        return key.toLowerCase(Locale.ROOT).trim()
                .replaceAll("[\\ \\-\\.]+", "_")
                .replaceAll("ä", "ae")
                .replaceAll("ö", "oe")
                .replaceAll("ü", "ue")
                .replaceAll("ß", "ss");
    }
}
