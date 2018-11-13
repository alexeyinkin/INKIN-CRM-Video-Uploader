package ru.inkin.inkincrm.videouploader;

import java.util.List;
import java.util.ArrayList;

public abstract class LineProcessor
{
    private byte status = IDLE;
    private final List<LineProcessorListener>   listeners   = new ArrayList<>();

    public static final byte IDLE           = 0;
    public static final byte IN_PROGRESS    = 1;

    public abstract void start();

    /**
     * Called by QueueFinalizerTask when the queue is complete.
     */
    public void onComplete()
    {
        setStatus(IDLE);
    }

    public synchronized void stop()
    {
        if (status == IN_PROGRESS)
        {
            //  TODO: Abort all tasks in the queue.
            setStatus(IDLE);
        }
    }

    public void setStatus(byte status)
    {
        this.status = status;

        for (LineProcessorListener listener : listeners)
        {
            listener.onStatusChange(status);
        }
    }

    public byte getStatus()
    {
        return status;
    }

    public boolean isInProgress()
    {
        return status == IN_PROGRESS;
    }

    public void addListener(LineProcessorListener listener)
    {
        listeners.add(listener);
    }
}
