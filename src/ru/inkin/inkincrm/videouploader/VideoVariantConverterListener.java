package ru.inkin.inkincrm.videouploader;

public interface VideoVariantConverterListener
{
    void onSegmentFileReady(VideoSegmentInfo info);
    void onProgress(float progress);
}
