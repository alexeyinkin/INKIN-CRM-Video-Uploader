package ru.inkin.inkincrm.videouploader;

import java.util.SortedMap;

public class QueueStarterTask extends TaskGeneric<QueueStarterTaskProcessor>
{
    private SortedMap<String, FileTask> fileTasks;

    public void setFileTasks(SortedMap<String, FileTask> fileTasks)
    {
        this.fileTasks = fileTasks;
    }

    @Override
    public void process()
    {
        int                         left        = fileTasks.size();
        QueueStarterTaskProcessor   processor   = getProcessor();

        for (String filePath : fileTasks.keySet())
        {
            VideoCreatorTask task = processor.createNextTask(this);
                    //this, VideoCreatorTask.class);

            task.setFileTask(fileTasks.get(filePath));
            processor.sendTaskToNext(task); //, --left == 0);
        }

        processor.sendNoMoreSubTasks(this); //, VideoCreatorTask.class);
    }
}
