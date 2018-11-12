package ru.inkin.inkincrm.videouploader;

abstract public class TaskGeneric<ProcessorClass extends TaskProcessor> extends Task
{
    private ProcessorClass  processor;

    public void setProcessor(ProcessorClass processor)
    {
        this.processor = processor;
    }

    protected ProcessorClass getProcessor()
    {
        return processor;
    }
}
