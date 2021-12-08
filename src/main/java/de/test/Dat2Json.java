package de.test;

import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class Dat2Json {

    public static void main(String[] args) throws IOException {

        // Schüler -> (Key -> Data)
        // Schüler, z.B. Fathi|Durst|01.01.1950
        // Key in { 'geburtsdatum', 'nachname', 'vorname', 'basisdaten', ... }
        Map<String, Map<String, Object>> resultMap = new TreeMap<>();
        Dat2Json d = new Dat2Json();

        // Datenstruktur mit Inhalten aus Datei füllen
        d.handleFile(resultMap, "SchuelerBasisdaten.dat");
        d.handleFile(resultMap, "SchuelerBisherigeSchulen.dat");
        d.handleFile(resultMap, "SchuelerErzieher.dat");
        d.handleFile(resultMap, "SchuelerLeistungsdaten.dat");
        d.handleFile(resultMap, "SchuelerMerkmale.dat");
        d.handleFile(resultMap, "SchuelerZusatzdaten.dat");

        // wenn eine dat Datei leer ist, kannst du so leere Listen im Output erzeugen
        resultMap.forEach((student, map) -> map.put("adressen", Collections.EMPTY_LIST));

        try (FileWriter w = new FileWriter("../fathi_dat2json/src/main/resources/result.json")) {
            // Datenstruktur in Json konvertieren
            String json = new GsonBuilder().setPrettyPrinting()
                    .create().toJson(new ArrayList<>(resultMap.values()));

            // in die Datei schreiben
            w.append(json);
        }
    }

    // das file einlesen und die resultmap befüllen
    private void handleFile(Map<String, Map<String, Object>> resultMap, String file) {

        // Inhalt des Files einlesen
        // Schüler -> List (key -> value)
        Map<String, List<Map<String, Object>>> m = getContentByStudent(file);

        // max Anzahl an Datensätzen der Datei, um später die Datenstruktur zu bestimmen (List oder einzelner Eintrag)
        // wenn maxSize > 1, dann ist das eine Liste z.B. Erzieher
        // wenn maxSite = 1, dann ein Einzeltyp, z.B. Basisdaten
        int maxSizeTmp = 0;
        for (List l : m.values()) {
            if (l.size() > maxSizeTmp) maxSizeTmp = l.size();
        }
        final int maxSize = maxSizeTmp;

        // in das richtige Format bringen
        m.forEach((k, v) -> {
            // k = nachname|vorname|geburtsdatum
            // v = List<map with attributes>
            Map<String, Object> map = resultMap.get(k);
            if (map == null) {
                map = new LinkedHashMap<>();
                map.put("nachname", k.split("\\|")[0]);
                map.put("vorname", k.split("\\|")[1]);
                map.put("geburtsdatum", k.split("\\|")[2]);
                resultMap.put(k, map);
            }

            if (maxSize == 1 && v.size() == 1) {
                map.put(file.substring(8, file.length() - 4)
                        .toLowerCase(Locale.ROOT), v.get(0));
            } else if (maxSize > 1) {
                map.put(file.substring(8, file.length() - 4)
                        .toLowerCase(Locale.ROOT), v);
            }
        });
    }

    private Map<String, List<Map<String, Object>>> getContentByStudent(String file) {
        // file einlesen
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

            Map<String, Object> studentSet = new TreeMap<>();
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

    // da einige Characters in json nicht erlaubt sind, werden ' ', -, . in einen Unterstrich geändert
    // Umlaute werden konvertiert
    private String mapKey(String key) {
        return key.toLowerCase(Locale.ROOT).trim()
                .replaceAll("[\\ \\-\\.]+", "_")
                .replaceAll("ä", "ae")
                .replaceAll("ö", "oe")
                .replaceAll("ü", "ue")
                .replaceAll("ß", "ss");
    }
}
