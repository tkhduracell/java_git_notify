package com.feality.gitnotify;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONStyle;
import net.minidev.json.JSONValue;

/**
 *
 * @author Filip
 */
class Settings {
    public static final File DEFAULT_FILE = new File(".", "git-notify.json");
    
    public static final String INTERVALL = "intervall";
    public static final String PROJECT = "project";
    public static final String PASS = "pass";
    public static final String USER = "user";
    public static final String HOST = "host";
    public static final String PORT = "port";
    
    public static boolean hasSettings() {
        return DEFAULT_FILE.exists();
    }

    public static void writeDefaultFile() {
        JSONObject json = new JSONObject();
        json.put(INTERVALL, 120000L);
        json.put(PROJECT, "myproject.git");
        json.put(USER, "myuser");
        json.put(PASS, "mypass");
        json.put(HOST, "myhost.com");
        json.put(PORT, 80);
        
        try (PrintWriter pw = new PrintWriter(DEFAULT_FILE)) {
            final String jsonStr = json.toJSONString(JSONStyle.NO_COMPRESS)
                    .replace(",", ",\n\t")
                    .replace("{", "{\n\t")
                    .replace("}", "\n}");
            pw.println(jsonStr);
            pw.flush();
        } catch (FileNotFoundException ex) {
            System.err.print("Failed to save to file: "+DEFAULT_FILE.getAbsolutePath());
        }
    }

    public static JSONObject readSettings() {
        String content = getContents(DEFAULT_FILE);
        return (JSONObject) JSONValue.parse(content);  
    }
    
    private static String getContents(File f){
        try {
            return readStreamUTF8(new FileInputStream(f));
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    
    private static String readStreamUTF8(InputStream is){
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new  BufferedReader(new InputStreamReader(is, "UTF-8"))) {
            String line;
            while((line = br.readLine()) != null) sb.append(line).append("\n");
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return sb.toString();
    }
}
