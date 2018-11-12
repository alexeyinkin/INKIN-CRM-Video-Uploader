package ru.inkin.inkincrm.videouploader;

public class VideoVariantSettings
{
    private long    id;
    private short   width;
    private short   height;
    private long    bitsPerSecond;
    private String  resolutionString;

    public void setWidth(short width)   { this.width = width; }
    public short getWidth()             { return width; }

    public void setHeight(short height) { this.height = height; }
    public short getHeight()            { return height; }

    public void setBitsPerSecond(long bitsPerSecond)    { this.bitsPerSecond = bitsPerSecond; }
    public long getBitsPerSecond()                      { return bitsPerSecond; }

    public void setVideoVariantId(long id)      { this.id = id; }
    public long getVideoVariantId()             { return id; }

    public void setResolutionString(String str) { resolutionString = str; }
    public String getResolutionString()         { return resolutionString; }
}
