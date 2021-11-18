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

        List<Map<String, Object>> resultMap = new ArrayList<>();
        Dat2Json d = new Dat2Json();

        d.handleFile(resultMap, "SchuelerBasisdaten.dat");
        d.handleFile(resultMap, "SchuelerZusatzdaten.dat");
        d.handleFile(resultMap, "SchuelerMerkmale.dat");

        try (FileWriter w = new FileWriter("result.json")) {
            w.append(new GsonBuilder().setPrettyPrinting()
                    .create().toJson(resultMap));
        }
    }

    private void handleFile(List<Map<String, Object>> resultMap, String file) {
        Map<String, Map> m = getContentByStudent(file);
        m.forEach((k, v) -> {
            Map<String, Object> map = new HashMap<>();
            resultMap.add(map);
            map.put("nachname", k.split("\\|")[0]);
            map.put("vorname", k.split("\\|")[1]);
            map.put("geburtsdatum", k.split("\\|")[2]);
            map.put(file.substring(8, file.length() - 4)
                    .toLowerCase(Locale.ROOT), v);
        });
    }

    private Map<String, Map> getContentByStudent(String file) {
        List<String> lines = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/" + file)))
                .lines().parallel().collect(Collectors.toList());

        String[] headerAttributes = lines.get(0).split("\\|");

        Map<String, Map> map = new HashMap<>();
        for (int i = 1; i < lines.size(); i++) {
            String[] values = lines.get(i).split("\\|");

            if (false && headerAttributes.length != values.length) {
                System.out.println(
                        file + " line " + i
                                + ";" + headerAttributes.length + "=" + values.length
                                + "\n" + values[0]
                                + "\n" + headerAttributes[10] + "=" + values[10]
                                + "\n" + headerAttributes[20] + "=" + values[20]
                                + "\n" + headerAttributes[30] + "=" + values[30]
                                + "\n" + headerAttributes[40] + "=" + values[40]
                );
            }
            Map<String, Object> m = new HashMap<>();
            // skip first 3 elements
            for (int j = 3; j < headerAttributes.length; j++) {
                m.put(mapKey(headerAttributes[j].trim()),
                        (j < values.length ? values[j].trim() : "")
                );
            }
            map.put(values[0].trim() + "|" + values[1].trim() + "|" + values[2].trim(), m);
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
