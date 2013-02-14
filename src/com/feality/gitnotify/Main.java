package com.feality.gitnotify;

import java.awt.Desktop;
import java.io.IOException;
import javax.swing.JOptionPane;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 *
 * @author Filip
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (!Settings.hasSettings()) {
            JOptionPane.showMessageDialog(null, "Please enter your settings in " + 
                    Settings.DEFAULT_FILE.getName()+
                    "\nThen restart!");
            Settings.writeDefaultFile();
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            return;
        }
        new GitNotify(Settings.readSettings()).run();
    }
}
