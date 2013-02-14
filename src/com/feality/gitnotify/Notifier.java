package com.feality.gitnotify;

/**
 *
 * @author Filip
 */
public interface Notifier {
    public void onCommit(String commitMessage, String commitDate, String commitUser);
    public void onUpdate(long checkDate);
}