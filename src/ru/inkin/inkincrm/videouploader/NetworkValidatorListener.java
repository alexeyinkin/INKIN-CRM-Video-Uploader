package ru.inkin.inkincrm.videouploader;

public interface NetworkValidatorListener<T>
{
    void resolvedValid(T obj);
    void resolvedInvalid(T obj);
    void enteredPending(T obj);
}
