package ru.inkin.inkincrm.videouploader;

public class QueueFinalizerTask extends JoiningTaskGeneric<QueueFinalizerTaskProcessor>
{
    /**
     * Called when the last video has been finalized.
     */
    @Override
    public void processFinalizing()
    {
        getProcessor().onComplete();
    }
}
