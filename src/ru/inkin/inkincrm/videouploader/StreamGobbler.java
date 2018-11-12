package ru.inkin.inkincrm.videouploader;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class StreamGobbler extends Thread
{
    InputStream is;
    
    public StreamGobbler(InputStream is)
    {
        this.is = is;
    }
    
    public void run()
    {
        try
        {
            BufferedReader r = new BufferedReader(new InputStreamReader(is));
            String line;

            while ((line = r.readLine()) != null)
            {
                processLine(line);
                //System.out.println("ERROR>" + line);
            }
        }
        catch (Exception e)
        {
        }
    }

    protected void processLine(String line)
    {
        System.out.println("OUTPUT>" + line);
    }
}
