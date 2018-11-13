package ru.inkin.inkincrm.videouploader;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;

/**
 *
 * @author Alexey
 */
public class MainWindow extends JFrame
{
    private BoxLayout       layout;

    private JPanel          actionsPanel;
    private JPanel          resolutionsPanel;

    private JPanel          statusPanel;
    private JLabel          dropHereLabel;

    private ButtonGroup     actionRadios;
    private JRadioButton    actionRadioNew;
    private JRadioButton    actionRadioReplace;
    private JTextField      replaceIdField;
    private JPanel          replacePanel;

    private JButton         startButton;
    private JButton         stopButton;
    private JButton         settingsButton;

    private JComboBox<BitratePreset>       bitratePresetCombo;
    private String[]        resolutions;
    private HashMap<String, JToggleButton>  resolutionToggles       = new HashMap<>();
    private HashMap<String, JTextField>     resolutionBitrateFields = new HashMap<>();
    private HashMap<String, FilePanel>      filePanels              = new HashMap<>();
    private SortedMap<String, BitratePreset>bitratePresets;

    private ServerSettingsWindow    serverSettingsWindow;

    public void setResolutions(String[] resolutions)
    {
        this.resolutions = resolutions;
    }

    public void setBitratePresets(SortedMap<String, BitratePreset> bitratePresets)
    {
        this.bitratePresets = bitratePresets;
    }

    public void initAndShow()
    {
        initWindow();

        createActionRadios();
        createResolutionToggles();

        add(actionsPanel);
        add(new JSeparator(SwingConstants.HORIZONTAL));
        add(resolutionsPanel);
        add(new JSeparator(SwingConstants.HORIZONTAL));
        add(createStatusPanel());
        add(new JSeparator(SwingConstants.HORIZONTAL));
        add(createActionButtons());
        pack();

        replacePanel.setMaximumSize(replacePanel.getSize());

        Dimension d = replaceIdField.getSize();
        replaceIdField.setMaximumSize(new Dimension(50, (int) d.getHeight()));
        replaceIdField.setSize(new Dimension(50, (int) d.getHeight()));

        pack();
    }

    private void initWindow()
    {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        layout = new BoxLayout(getContentPane(), BoxLayout.Y_AXIS);
        setLayout(layout);
        setTitle("INKIN CRM Video Uploader");
        setIconImage(InkinCrmVideoUploader.getAppIcon());
    }

    private JComponent createStatusPanel()
    {
        statusPanel = new JPanel(new GridBagLayout());
        statusPanel.setOpaque(true);

        statusPanel.setBackground(new Color(
                (float) 0, (float) 0, (float) 0, (float) .3));

        //statusPanel.setMaximumSize(new Dimension(600, 100));
        statusPanel.setMinimumSize(new Dimension(600, 100));
        //statusPanel.setText("Drop Video Here");
        statusPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        FileTransferHandler handler = new FileTransferHandler();
        handler.setMainWindow(this);
        statusPanel.setTransferHandler(handler);

        dropHereLabel = new JLabel();
        dropHereLabel.setText("Drop Files Here");
        dropHereLabel.setMinimumSize(new Dimension(600, 100));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.ipady = 40;
        //dropHereLabel.setMargin(new Insets(30, 30, 30, 30));
        statusPanel.add(dropHereLabel, c);

        return statusPanel;
    }

    private void createActionRadios()
    {
        actionsPanel = new DisabledPanel();
        actionsPanel.setLayout(new BoxLayout(actionsPanel, BoxLayout.Y_AXIS));
        actionsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        replacePanel = new DisabledPanel();
        replacePanel.setLayout(new BoxLayout(replacePanel, BoxLayout.X_AXIS));

        actionRadioNew = new JRadioButton("Upload New");
        actionRadioNew.setActionCommand("uploadNew");
        actionRadioNew.setSelected(true);
        actionRadioNew.setAlignmentX(Component.LEFT_ALIGNMENT);

        actionRadioReplace = new JRadioButton("Replace Video with ID:");
        actionRadioReplace.setActionCommand("uploadReplace");

        actionsPanel.add(actionRadioNew);
        actionsPanel.add(replacePanel);
        actionsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        replaceIdField = new JTextField(5);
        replacePanel.add(actionRadioReplace);
        //replacePanel.add(Box.createHorizontalGlue());
        replacePanel.add(replaceIdField);
        //replacePanel.add(Box.createRigidArea(new Dimension(30, 30)));
        //replacePanel.add(Box.createHorizontalGlue());
        replacePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        //replacePanel.setMaximumSize(replacePanel.getSize());

        actionRadios = new ButtonGroup();
        actionRadios.add(actionRadioNew);
        actionRadios.add(actionRadioReplace);

        replaceIdField.addFocusListener(new FocusListener(){
                @Override
                public void focusGained(FocusEvent e){
                    actionRadioReplace.setSelected(true);
                }
                public void focusLost(FocusEvent e) {
                    
                }
            });
    }

    private JComponent createResolutionToggles()
    {
        short               i;
        JLabel              label;
        GridBagConstraints  c;

        resolutionsPanel = new DisabledPanel();
        resolutionsPanel.setLayout(new GridBagLayout());
        resolutionsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        resolutionsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        label = new JLabel();
        label.setText("Optimize For:");
        c = new GridBagConstraints();
        c.gridx     = 0;
        c.gridy     = 0;
        c.weightx   = 1;
        c.weighty   = 1;
        c.anchor    = GridBagConstraints.LINE_END;
        c.insets    = new Insets(10, 0, 10, 10);    // Top, Left, Bottom, Right.
        resolutionsPanel.add(label, c);

        bitratePresetCombo = new JComboBox<>();
        for (String key : bitratePresets.keySet())
        {
            BitratePreset preset = bitratePresets.get(key);
            bitratePresetCombo.addItem(preset);
        }

        bitratePresetCombo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e)
            {
                onBitratePresetChange();
            }
        });

        c = new GridBagConstraints();
        c.gridx     = 1;
        c.gridy     = 0;
        c.weightx   = 1;
        c.weighty   = 1;
        c.gridwidth = 7;
        c.anchor    = GridBagConstraints.LINE_START;
        //c.insets    = new Insets(10, 10, 10, 10);
        resolutionsPanel.add(bitratePresetCombo, c);

        label = new JLabel();
        label.setText("Resolutions:");
        c = new GridBagConstraints();
        c.gridx     = 0;
        c.gridy     = 1;
        c.weightx   = 1;
        c.weighty   = 1;
        c.anchor    = GridBagConstraints.LINE_END;
        c.insets    = new Insets(10, 0, 10, 10);
        resolutionsPanel.add(label, c);

        label = new JLabel();
        label.setText("bits/sec:");
        c = new GridBagConstraints();
        c.gridx     = 0;
        c.gridy     = 2;
        c.weightx   = 1;
        c.weighty   = 1;
        c.anchor    = GridBagConstraints.LINE_END;
        c.insets    = new Insets(10, 0, 10, 10);
        resolutionsPanel.add(label, c);

        ActionListener toggleActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                updateFilePanels();
            }
        };

        for (i = 0; i < resolutions.length; i++)
        {
            String          r       = resolutions[i];

            JToggleButton   toggle  = new JToggleButton(r);
            resolutionToggles.put(r, toggle);

            toggle.setSelected(true);
            toggle.setPreferredSize(new Dimension(70, 25));
            toggle.setMargin(new Insets(0, 0, 0, 0));
            toggle.addActionListener(toggleActionListener);

            c = new GridBagConstraints();
            c.gridx     = i + 1;
            c.gridy     = 1;
            c.weightx   = 1;
            c.weighty   = 1;
            resolutionsPanel.add(toggle, c);

            if (!r.equals(InkinCrmVideoUploader.audioOnlyString))
            {
                //JTextField field = new JFormattedTextField(dFormat);
                JTextField field = new JTextField();
                field.setHorizontalAlignment(JTextField.CENTER);
                field.setPreferredSize(new Dimension(70, 25));
                resolutionBitrateFields.put(r, field);
                c = new GridBagConstraints();
                c.gridx     = i + 1;
                c.gridy     = 2;
                c.weightx   = 1;
                c.weighty   = 1;
                resolutionsPanel.add(field, c);

                field.getDocument().addDocumentListener(new DocumentListener() {
                    public void changedUpdate(DocumentEvent e)
                    {
                        change();
                    }
                    public void removeUpdate(DocumentEvent e)
                    {
                        change();
                    }
                    public void insertUpdate(DocumentEvent e)
                    {
                        change();
                    }

                    public void change()
                    {
                        updateFilePanels();
                    }
                });
            }
        }

        onBitratePresetChange();        //  Fill the default bitrates.

        return resolutionsPanel;
    }

    private JPanel createActionButtons()
    {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        //panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        settingsButton = new JButton("Server Settings...");
        settingsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                showServerSettings();
            }
        });

        startButton = new JButton("Start");
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                onStartClick();
            }
        });

        stopButton = new JButton("Stop");
        stopButton.setVisible(false);
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                onStopClick();
            }
        });

        panel.add(settingsButton);
        panel.add(startButton);
        panel.add(stopButton);

        return panel;
    }

    public void addFileTask(FileTask fileTask)
    {
        dropHereLabel.setVisible(false);
        
        FilePanel filePanel = new FilePanel();
        filePanel.setFileTask(fileTask);
        filePanel.init();

        GridBagConstraints c = new GridBagConstraints();
        c.gridx     = 0;
        c.gridy     = fileTask.getIndex() + 1;
        c.weightx   = 1;
        c.weighty   = 1;
        c.fill      = GridBagConstraints.HORIZONTAL;
        c.anchor    = GridBagConstraints.PAGE_START;
        //c.gridwidth = GridBagConstraints.REMAINDER;

        statusPanel.add(filePanel, c);
        filePanels.put(fileTask.getFile().getAbsolutePath(), filePanel);
        updateFilePanel(filePanel); //, null);
        pack();
    }

    /**
     * Removes the visuals. The task is already stopped if was running.
     */
    public void removeFileTask(FileTask fileTask)
    {
        String      key         = fileTask.getFile().getAbsolutePath();
        FilePanel   filePanel   = filePanels.get(key);

        if (filePanel != null)
        {
            filePanels.remove(key);
            statusPanel.remove(filePanel);
        }

        updateDropTarget();
        pack();
    }

    private void onBitratePresetChange()
    {
        fillBitratesFromPreset((BitratePreset) bitratePresetCombo.getSelectedItem());
    }

    private void fillBitratesFromPreset(BitratePreset preset)
    {
        for (String r : resolutions)
        {
            JTextField field = resolutionBitrateFields.get(r);

            if (field != null)
            {
                field.setText(String.valueOf(preset.getBitrate(r)));
            }
        }
    }

    /**
     * Called:
     * -- When bitrate changes.
     * -- When a resolution is turned on or off.
     */
    private void updateFilePanels()
    {
        //Map<String, Integer> selectedBitrates = getSelectedBitrates();

        for (FilePanel panel : filePanels.values())
        {
            updateFilePanel(panel); //, selectedBitrates);
        }
    }

    private void updateFilePanel(FilePanel panel) //, Map<String, Integer> selectedBitrates)
    {
        panel.updateStat();
//        panel.updateStat(selectedBitrates == null
//                ? getSelectedBitrates()
//                : selectedBitrates);
    }

    private void updateDropTarget()
    {
        dropHereLabel.setVisible(filePanels.isEmpty());
    }

    public Map<String, Integer> getSelectedBitrates()
    {
        Map<String, Integer>    selectedBitrates = new HashMap<>();

        for (String r : resolutionToggles.keySet())
        {
            if (resolutionToggles.get(r).isSelected())
            {
                JTextField field = resolutionBitrateFields.get(r);

                if (field != null)
                {
                    int bitsPerSecond = 0;

                    try
                    {
                        bitsPerSecond = Integer.parseInt(field.getText());
                    }
                    catch (Exception e) {}

                    selectedBitrates.put(r, bitsPerSecond);
                }
            }
        }

        return selectedBitrates;
    }

    public void createServerSettingsWindow()
    {
        serverSettingsWindow = new ServerSettingsWindow();
        serverSettingsWindow.init();
    }

    public void showServerSettings()
    {
        if (serverSettingsWindow == null)
        {
            createServerSettingsWindow();
        }

        serverSettingsWindow.loadConfig();
        serverSettingsWindow.setVisible(true);
    }

    public void onStartClick()
    {
        if (InkinCrmVideoUploader.isConfigOk())
        {
            InkinCrmVideoUploader.startQueue();
        }
        else
        {
            showServerSettings();
        }
    }

    public void onStopClick()
    {
        InkinCrmVideoUploader.stopQueue();
    }

    public byte getAction()
    {
        if (actionRadioNew.isSelected())    return FileTask.UPLOAD_NEW;

        return FileTask.REPLACE;
    }

    public long getReplaceVideoId()
    {
        //  TODO: Allow to set for each video, then remove this method.
        return Long.parseLong(replaceIdField.getText());
    }

    private void setInProgressMode()
    {
        actionsPanel.setEnabled(false);
        resolutionsPanel.setEnabled(false);

        startButton.setVisible(false);
        stopButton.setVisible(true);

        settingsButton.setEnabled(false);
    }

    private void setIdleMode()
    {
        actionsPanel.setEnabled(true);
        resolutionsPanel.setEnabled(true);

        stopButton.setVisible(false);
        startButton.setVisible(true);

        settingsButton.setEnabled(true);
    }

    public void setLineProcessor(LineProcessor lineProcessor)
    {
        lineProcessor.addListener(new PrivateLineProcessorListener());
    }

    private class PrivateLineProcessorListener implements LineProcessorListener
    {
        @Override
        public void onStatusChange(byte status)
        {
            switch (status)
            {
                case InkinCrmVideoUploaderLineProcessor.IDLE:
                    setIdleMode();
                    break;

                case InkinCrmVideoUploaderLineProcessor.IN_PROGRESS:
                    setInProgressMode();
                    break;
            }
        }
    }
}
