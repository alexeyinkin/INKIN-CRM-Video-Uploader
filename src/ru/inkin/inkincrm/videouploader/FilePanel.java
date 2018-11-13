package ru.inkin.inkincrm.videouploader;

//import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Image;
import java.awt.event.*;
import java.net.URI;
import javax.swing.*;

public class FilePanel extends JPanel
{
    private FileTask    fileTask;
    private JLabel      titleLabel;
    private JLabel      statLabel;
    private JLabel      thumbLabel;
    //private JLabel      targetSizeLabel;
    private JButton     removeButton;
    private JPanel      statusPanel;
    private JButton     editInBrowserButton;
    private final Map<String, JProgressBar> progressBars = new HashMap<>();

    public FilePanel()
    {
        super(new GridBagLayout());
    }

    public void setFileTask(FileTask fileTask)
    {
        this.fileTask = fileTask;
        fileTask.addListener(new PrivateFileTaskListener());
    }

    public void init()
    {
        setOpaque(true);
        setBackground(new Color(
                (float) .3, (float) .3, (float) 0, (float) .3));

        createThumbPanel();
        createTitle();
        createStat();
        //createTargetSize();
        createRemoveButton();
        createStatusPanel();
    }

    private void createThumbPanel()
    {
        thumbLabel = new JLabel();

        GridBagConstraints c = new GridBagConstraints();
        c.fill  = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx   = 1;
        c.weighty   = 1;
        c.gridheight    = 4;

        thumbLabel.setIcon(getThumb());
//        thumbLabel.setOpaque(true);
//        thumbLabel.setBackground(new Color(
//                (float) 1, (float) 0, (float) 0, (float) 1));

//        Dimension d = new Dimension(192, 108);
//        thumbLabel.setMinimumSize(d);
//        thumbLabel.setMaximumSize(d);

        add(thumbLabel, c);
    }

    private void createTitle()
    {
        titleLabel = new JLabel();
        titleLabel.setText(fileTask.getFile().getName());
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        //ImageIcon icon = new ImageIcon(fileTask.getThumbPath());
        //ImageIcon icon = new ImageIcon("D:\\ai\\work\\www\\cms_2016\\stuff\\source\\images\\approved-151676_640.png");
        //titleLabel.setIcon(getThumb());

        GridBagConstraints c = new GridBagConstraints();
        //c.fill  = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 0;
        c.weightx   = 1;
        c.weighty   = 1;
        c.anchor    = GridBagConstraints.FIRST_LINE_START;

//        titleLabel.setOpaque(true);
//        titleLabel.setBackground(new Color(
//                (float) 0, (float) 1, (float) 0, (float) 1));

        add(titleLabel, c);
    }

    private void createStat()
    {
//        String      stat    = InkinCrmVideoUploader.getDisplayFileSize(fileTask.getFile().length());
//        VideoInfo   info    = fileTask.getSourceInfo();
//
//        if (info != null)
//        {
//            stat += ",   " + info.width + " × " + info.height;
//            stat += ",   " + InkinCrmVideoUploader.getDisplayDuration(info.duration);
//        }
//
        statLabel = new JLabel();
//        statLabel.setText(stat);
        setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 1;
        c.weightx   = 1;
        c.weighty   = 1;
        c.anchor    = GridBagConstraints.FIRST_LINE_START;

        add(statLabel, c);
        //updateStat(InkinCrmVideoUploader.getSelectedBitrates());
    }

//    private void createTargetSize()
//    {
//        targetSizeLabel = new JLabel();
//        targetSizeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
//
//        GridBagConstraints c = new GridBagConstraints();
//        c.gridx = 1;
//        c.gridy = 2;
//        c.weightx   = 1;
//        c.weighty   = 1;
//        c.anchor    = GridBagConstraints.FIRST_LINE_START;
//
//        add(targetSizeLabel, c);
//    }

    private void createStatusPanel()
    {
        statusPanel = new JPanel(new GridBagLayout());
        statusPanel.setOpaque(false);
//        statusPanel.setBackground(new Color(
//                (float) 0, (float) 0, (float) .3, (float) .3));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx     = 1;
        c.gridy     = 2;
        c.weightx   = 1;
        c.weighty   = 1;
        c.anchor    = GridBagConstraints.FIRST_LINE_START;

        add(statusPanel, c);
    }

    private void createRemoveButton()
    {
        removeButton = new JButton();
        removeButton.setText("❌");

        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                onRemoveClick();
                //InkinCrmVideoUploader.removeFileTask(fileTask);
            }
        });

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 0;
        c.weightx   = 1;
        c.weighty   = 1;
        c.anchor    = GridBagConstraints.LAST_LINE_START;

        add(removeButton, c);
    }

    private void onRemoveClick()
    {
        LineProcessor lineProcessor = InkinCrmVideoUploader.getLineProcessor();

        synchronized (lineProcessor)
        {
            if (lineProcessor.isInProgress())
            {
                fileTask.setStatus(FileTask.ABORTED);
            }
            else
            {
                InkinCrmVideoUploader.removeFileTask(fileTask);
            }
        }
    }

    private ImageIcon getThumb()
    {
        ImageIcon imageIcon = new ImageIcon(fileTask.getThumbPath()); // load the image to a imageIcon
        Image image = imageIcon.getImage(); // transform it 
        Image newimg = image.getScaledInstance(192, 108,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way  
        return new ImageIcon(newimg);  // transform it back
    }

    public void updateStat()
    {
        //targetSizeLabel.setText(InkinCrmVideoUploader.getDisplayFileSize(bytes));
        statLabel.setText(getStatString());
    }

    private String getStatString()
    {
        List<String>    parts       = new ArrayList<>();
        String          fileSize    = InkinCrmVideoUploader.getDisplayFileSize(fileTask.getFile().length());
        VideoInfo       info        = fileTask.getSourceInfo();

        if (info != null)
        {
            parts.add(info.width + " × " + info.height);
            parts.add(InkinCrmVideoUploader.getDisplayDuration(info.duration));

            fileSize += " -> " + InkinCrmVideoUploader.getDisplayFileSize(
                    calculateProjectedFileSize(fileTask.getSelectedBitrates()));
        }

        parts.add(fileSize);

        return String.join(",   ", parts);
    }

    private long calculateProjectedFileSize(Map<String, Integer> selectedBitrates)
    {
        VideoInfo info = fileTask.getSourceInfo();
        if (info == null) return 0;

        long bytes = 0;

        for (String r : selectedBitrates.keySet())
        {
            short height = Short.parseShort(r);

            if (height > 0 && height <= info.height)
            {
                int bitsPerSecond = selectedBitrates.get(r);

                bytes += bitsPerSecond * info.duration / 8;
            }
        }

        return bytes;
    }

    private void showInProgress()
    {
        emptyStatusPanel();

        Map<String, Integer> bitrates = fileTask.getSelectedBitrates();
        int i = 0;

        for (String resolution : bitrates.keySet())
        {
            GridBagConstraints c;

            JLabel label = new JLabel(resolution + ": ");
            c = new GridBagConstraints();
            c.gridx     = 0;
            c.gridy     = i;
            c.weightx   = 1;
            c.weighty   = 1;
            c.anchor    = GridBagConstraints.FIRST_LINE_START;
            statusPanel.add(label, c);

            JProgressBar progressBar = new JProgressBar(0, 100);
            c = new GridBagConstraints();
            c.gridx     = 1;
            c.gridy     = i++;
            c.weightx   = 1;
            c.weighty   = 1;
            c.anchor    = GridBagConstraints.FIRST_LINE_START;
            statusPanel.add(progressBar, c);

            progressBars.put(resolution, progressBar);
        }

        packWindow();
    }

    private void packWindow()
    {
        JFrame mainWindow = (JFrame) SwingUtilities.getWindowAncestor(this);
        mainWindow.pack();
    }

    private void updateProgress(String resolution, float progress)
    {
        JProgressBar progressBar = progressBars.get(resolution);

        if (progressBar != null)    //  Could be deleted if aborted.
        {
            progressBar.setValue((int) Math.ceil(progress * 100));
        }
    }

    public void showAborted()
    {
        emptyStatusPanel();
        createViewLogButton();

        setBackground(new Color(
                (float) 0.3, (float) 0, (float) 0, (float) .3));

        packWindow();
    }

    private void showComplete()
    {
        emptyStatusPanel();
        createEditInBrowserButton();

        setBackground(new Color(
                (float) 0, (float) 0.3, (float) 0, (float) .3));

        packWindow();
    }

    private void emptyStatusPanel()
    {
        for (Component component : statusPanel.getComponents())
        {
            statusPanel.remove(component);
        }

        progressBars.clear();
    }

    private void createEditInBrowserButton()
    {
        editInBrowserButton = new JButton();
        editInBrowserButton.setText("Edit in Browser...");

        editInBrowserButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                editInBrowser();
            }
        });

        GridBagConstraints c = new GridBagConstraints();
        c.gridx     = 0;
        c.gridy     = 0;
        c.weightx   = 1;
        c.weighty   = 1;
        c.anchor    = GridBagConstraints.LAST_LINE_START;
        statusPanel.add(editInBrowserButton, c);
    }

    private void createViewLogButton()
    {
        editInBrowserButton = new JButton();
        editInBrowserButton.setText("View Log...");

        editInBrowserButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                viewLog();
            }
        });

        GridBagConstraints c = new GridBagConstraints();
        c.gridx     = 1;
        c.gridy     = 0;
        c.weightx   = 1;
        c.weighty   = 1;
        c.anchor    = GridBagConstraints.LAST_LINE_START;
        statusPanel.add(editInBrowserButton, c);
    }

    private void editInBrowser()
    {
        try
        {
            Desktop desktop = Desktop.getDesktop();
            desktop.browse(new URI(
                    InkinCrmVideoUploader.getServerUrl()
                    + "/admin/list/videos/" + fileTask.getVideoId()));
        }
        catch (Exception e) {}
    }

    private void viewLog()
    {
        try
        {
            Desktop.getDesktop().edit(fileTask.getLogFile());
        }
        catch (Exception e) {}
    }

    private class PrivateFileTaskListener implements FileTaskListener
    {
        @Override
        public void onStatusChange(byte status)
        {
            switch (status)
            {
                case FileTask.IN_PROGRESS:
                    showInProgress();
                    break;

                case FileTask.COMPLETE:
                    showComplete();
                    break;

                case FileTask.ABORTED:
                    showAborted();
                    break;
            }
        }

        @Override
        public void onProgress(String resolution, float progress)
        {
            updateProgress(resolution, progress);
        }
    }
}
