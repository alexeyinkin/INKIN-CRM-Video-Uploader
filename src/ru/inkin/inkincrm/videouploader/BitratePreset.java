package ru.inkin.inkincrm.videouploader;

import java.util.*;

public class BitratePreset
{
    private String                      title;
    private HashMap<String, Integer>    resolutionToBitrate = new HashMap<String, Integer>();

    public BitratePreset(String title)
    {
        this.title = title;
    }

    public void addBitrate(String resolution, int kbitsPerSecond)
    {
        resolutionToBitrate.put(resolution, kbitsPerSecond);
    }

    public String   getTitle()    { return title; }

    public int getBitrate(String resolution)
    {
        return resolutionToBitrate.get(resolution);
    }

    @Override
    public String toString()
    {
        return title;
    }
}
