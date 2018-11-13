package ru.inkin.inkincrm.videouploader;

import java.io.File;
import java.io.IOException;

public class SynchronizedFileWriter extends java.io.FileWriter
{
    public SynchronizedFileWriter(File file) throws IOException
    {
        super(file);
    }

    @Override
    public synchronized void write(String str) throws IOException
    {
        super.write(str);
    }
}
