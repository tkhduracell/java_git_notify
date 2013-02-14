package com.feality.gitnotify;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import net.minidev.json.JSONObject;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 *
 * @author Filip
 */ 
public class GitNotify implements Runnable {

    private Notifier nl;
    private final int intervall;
    private final String project;
    private final String pass;
    private final String user;
    private final String host;
    private final int port;

    public GitNotify(JSONObject json) {
        this.intervall = (int) json.get(Settings.INTERVALL);
        this.project = (String) json.get(Settings.PROJECT);
        this.pass = (String) json.get(Settings.PASS);
        this.user = (String) json.get(Settings.USER);
        this.host = (String) json.get(Settings.HOST);
        this.port = (int) json.get(Settings.PORT);
    }

    @Override
    public void run() {
        DefaultHttpClient c = new DefaultHttpClient();
        try{
            String get = getRSS(c);
            if(get == null || !get.contains("<description>")){
                throw new Exception("Invalid RSS");
            }
        } catch(Exception e){
            JOptionPane.showMessageDialog(null, "Invalid settings provided: \n"+e.getMessage());
            return;
        } finally {
            c.getConnectionManager().shutdown();
        }
        
        try {
            SystemTray tray = SystemTray.getSystemTray();
            PopupMenu popup = new PopupMenu();
            
            URL imgURL = ClassLoader.getSystemResource("git-logo.png");
            Image image = Toolkit.getDefaultToolkit().getImage(imgURL);
            
            final TrayIcon trayIcon = new TrayIcon(image, "Git-Notify", popup);
            trayIcon.setImageAutoSize(true);
            
            MenuItem exit = new MenuItem("Exit");
            exit.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.exit(0);
                }
            });
            popup.add(exit);
            
            tray.add(trayIcon);
            
            nl = new Notifier() {
                private final SimpleDateFormat from = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z"); //Wed, 13 Feb 2013 10:44:16 +0000
                private final SimpleDateFormat to = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss"); 
                
                @Override
                public void onCommit(String commitMessage, String commitDate, String commitUser) {
                    trayIcon.displayMessage(commitUser + " pushed changes!", commitMessage, TrayIcon.MessageType.INFO);
                    Toolkit.getDefaultToolkit().beep();
                }

                @Override
                public void onUpdate(long checkDate) {
                    String newDate = to.format(new Date(checkDate));
                    trayIcon.setToolTip("Last check: " + newDate);
                }
            };
            trayIcon.displayMessage("", "Watching "+host+":"+port, TrayIcon.MessageType.INFO);
            
            Timer t = new Timer();
            t.scheduleAtFixedRate(new GitCheckTask(), 1000, intervall);
            
        } catch (AWTException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }
    
    
    private class GitCheckTask extends TimerTask {
        private DefaultHttpClient client;
        private String lastBuildDate;
        
        @Override
        public void run() {
            try {
                client = new DefaultHttpClient();
                String text = getRSS(client);
                
                int i0 = text.indexOf("<lastBuildDate>");
                int i1 = text.indexOf("</lastBuildDate>", i0);
                
                String newlastBuildDate = text.substring(i0 + 15 , i1);
                
                if(lastBuildDate != null && !(lastBuildDate.equalsIgnoreCase(newlastBuildDate))){
                    
                    i0 = text.indexOf("<description>", text.indexOf("<description>")+1);
                    i1 = text.indexOf("</description>", i0);

                    String commitMsg = text.substring(i0 + 13 , i1);

                    i0 = text.indexOf("<author>");
                    i1 = text.indexOf("</author>", i0);

                    String commitUser = text.substring(i0 + 8 , i1).replace("&lt;", "<").replace("&gt;", ">");
                    
                    nl.onCommit(commitMsg, newlastBuildDate, commitUser);
                }
                lastBuildDate = newlastBuildDate;
                
                nl.onUpdate(System.currentTimeMillis());
                
            } catch (IOException ex) {
                nl.onCommit(ex.getMessage(), "", "");
            } finally {
                client.getConnectionManager().shutdown();
            }
        }


    }
    
    private String getRSS(DefaultHttpClient client) throws IllegalStateException, IOException {
        client.getCredentialsProvider().clear();
        client.getCredentialsProvider().setCredentials(new AuthScope(host, port), new UsernamePasswordCredentials(user, pass));
        HttpGet get = new HttpGet("http://"+ host+ ":"+ port + "/gitweb/?p="+project+";a=rss");
        HttpResponse resp = client.execute(get);
        String text = consumeToString(resp);
        return text;
    }
    
    private String consumeToString(HttpResponse resp) throws IllegalStateException, IOException {
        StringBuilder sb = new StringBuilder();
        InputStream is = resp.getEntity().getContent();
        try (BufferedReader br = new  BufferedReader(new InputStreamReader(is, "UTF-8"))) {
            String line;
            while((line = br.readLine()) != null) sb.append(line).append("\n");
        }
        String text = sb.toString();
        return text;
    }
}
