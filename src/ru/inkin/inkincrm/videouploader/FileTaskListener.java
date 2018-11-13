package ru.inkin.inkincrm.videouploader;

public interface FileTaskListener
{
    void onStatusChange(byte status);
    void onProgress(String resolution, float progress);
}
