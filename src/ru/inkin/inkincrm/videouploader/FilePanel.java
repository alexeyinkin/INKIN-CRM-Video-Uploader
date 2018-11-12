package ru.inkin.inkincrm.videouploader;

//import java.io.File;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class FilePanel extends JPanel
{
    private FileTask    fileTask;
    private JLabel      titleLabel;
    private JLabel      statLabel;
    private JLabel      thumbLabel;
    private JLabel      targetSizeLabel;
    private JButton     removeButton;

    public FilePanel()
    {
        super(new GridBagLayout());
    }

    public void setFileTask(FileTask fileTask)
    {
        this.fileTask = fileTask;
    }

    public void init()
    {
        setOpaque(true);
        setBackground(new Color(
                (float) 0, (float) 0.3, (float) 0, (float) .3));

        createThumbPanel();
        createTitle();
        createStat();
        createTargetSize();
        createRemoveButton();
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
        String      stat    = InkinCrmVideoUploader.getDisplayFileSize(fileTask.getFile().length());
        VideoInfo   info    = fileTask.getSourceInfo();

        if (info != null)
        {
            stat += ",   " + info.width + " × " + info.height;
            stat += ",   " + InkinCrmVideoUploader.getDisplayDuration(info.duration);
        }

        statLabel = new JLabel();
        statLabel.setText(stat);
        setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 1;
        c.weightx   = 1;
        c.weighty   = 1;
        c.anchor    = GridBagConstraints.FIRST_LINE_START;

        add(statLabel, c);
    }

    private void createTargetSize()
    {
        targetSizeLabel = new JLabel();
        targetSizeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 2;
        c.weightx   = 1;
        c.weighty   = 1;
        c.anchor    = GridBagConstraints.FIRST_LINE_START;

        add(targetSizeLabel, c);
    }

    private void createRemoveButton()
    {
        removeButton = new JButton();
        removeButton.setText("X");

        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                InkinCrmVideoUploader.removeFileTask(fileTask);
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

    private ImageIcon getThumb()
    {
        ImageIcon imageIcon = new ImageIcon(fileTask.getThumbPath()); // load the image to a imageIcon
        Image image = imageIcon.getImage(); // transform it 
        Image newimg = image.getScaledInstance(192, 108,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way  
        return new ImageIcon(newimg);  // transform it back
    }

    public void updateTargetSize(Map<String, Integer> selectedBitrates)
    {
        VideoInfo info = fileTask.getSourceInfo();
        if (info == null) return;

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

        targetSizeLabel.setText(InkinCrmVideoUploader.getDisplayFileSize(bytes));
    }
}
