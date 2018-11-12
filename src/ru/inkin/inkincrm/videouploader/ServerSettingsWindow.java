package ru.inkin.inkincrm.videouploader;

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class ServerSettingsWindow extends JFrame
{
    private JPanel      fieldsPanel;
    private JTextField  hostField;
    private JLabel      hostStatusLabel;
    private JTextField  tokenField;
    private JLabel      tokenStatusLabel;
    private JButton     manageTokensButton;

    private JPanel      buttonsPanel;
    private JButton     okButton;
    private JButton     cancelButton;

    private Icon        validIcon;
    private Icon        invalidIcon;
    private Icon        pendingIcon;

    private boolean     hostChanged;

    private NetworkValidatorListener<String>    tokenValidatorListener;
    private TokenValidator  tempHostTokenValidator;

    public void init()
    {
        initWindow();
        initFields();
        initButtons();
        initValidationIcons();
        initHostValidator();
        initTokenValidator();

        pack();
        probeHostAndUpdateStatus();
    }

    private void initWindow()
    {
        setTitle("INKIN CRM Server Settings");
        setIconImage(InkinCrmVideoUploader.getAppIcon());
    }

    private void initFields()
    {
        fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(fieldsPanel, BorderLayout.CENTER);
        
        JLabel label;
        GridBagConstraints c;

        label = new JLabel("Host:");
        c = new GridBagConstraints();
        c.gridx     = 0;
        c.gridy     = 0;
        c.anchor    = GridBagConstraints.FIRST_LINE_END;
        c.insets    = new Insets(10, 0, 10, 10);
        fieldsPanel.add(label, c);

        hostField = new JTextField();
        hostField.setPreferredSize(new Dimension(300, 25));
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 0;
        fieldsPanel.add(hostField, c);

        hostField.getDocument().addDocumentListener(new DocumentListener() {
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
                hostChanged = true;
            }
        });

        hostField.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {}
            public void focusLost(FocusEvent e)
            {
                probeHostAndUpdateStatus();
            }
        });

        hostStatusLabel = new JLabel();
        hostStatusLabel.setPreferredSize(new Dimension(32, 32));
        c = new GridBagConstraints();
        c.gridx     = 2;
        c.gridy     = 0;
        c.anchor    = GridBagConstraints.FIRST_LINE_START;
        fieldsPanel.add(hostStatusLabel, c);


        label = new JLabel("API Token:");
        c = new GridBagConstraints();
        c.gridx     = 0;
        c.gridy     = 1;
        c.anchor    = GridBagConstraints.FIRST_LINE_END;
        c.insets    = new Insets(10, 0, 10, 10);
        fieldsPanel.add(label, c);

        tokenField = new JTextField();
        tokenField.setPreferredSize(new Dimension(300, 25));
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 1;
        fieldsPanel.add(tokenField, c);

        tokenField.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {}
            public void focusLost(FocusEvent e)
            {
                validateTokenAndUpdateStatus();
            }
        });

        tokenStatusLabel = new JLabel();
        tokenStatusLabel.setPreferredSize(new Dimension(32, 32));
        c = new GridBagConstraints();
        c.gridx     = 2;
        c.gridy     = 1;
        c.anchor    = GridBagConstraints.FIRST_LINE_START;
        fieldsPanel.add(tokenStatusLabel, c);

        if (Desktop.isDesktopSupported())
        {
            manageTokensButton = new JButton("Manage Tokens...");
            c = new GridBagConstraints();
            c.gridx = 3;
            c.gridy = 1;
            fieldsPanel.add(manageTokensButton, c);

            manageTokensButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    onManageTokensClick();
                }
            });
        }
    }

    private void initButtons()
    {
        buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(buttonsPanel, BorderLayout.PAGE_END);
        buttonsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                onOk();
            }
        });

        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                setVisible(false);
            }
        });

        buttonsPanel.add(okButton);
        buttonsPanel.add(cancelButton);
    }

    private void initHostValidator()
    {
        HostValidator validator = InkinCrmVideoUploader.getHostValidator();
        validator.addListener(new NetworkValidatorListener<String>() {
            public void resolvedValid(String url)
            {
                showHostValid();
            }
            public void resolvedInvalid(String url)
            {
                showHostInvalid();
            }
            public void enteredPending(String url)
            {
                showHostPending();
            }
        });
    }

    private void showHostValid()
    {
        hostStatusLabel.setIcon(validIcon);
        validateTokenAndUpdateStatus();
    }

    private void showHostInvalid()
    {
        hostStatusLabel.setIcon(invalidIcon);
    }

    private void showHostPending()
    {
        hostStatusLabel.setIcon(pendingIcon);
    }

    private void initTokenValidator()
    {
        tokenValidatorListener = new NetworkValidatorListener<String>() {
            public void resolvedValid(String token)
            {
                showTokenValid();
            }
            public void resolvedInvalid(String token)
            {
                showTokenInvalid();
            }
            public void enteredPending(String token)
            {
                showTokenPending();
            }
        };

        TokenValidator validator = InkinCrmVideoUploader.getTokenValidator();
        validator.addListener(tokenValidatorListener);
    }

    private void showTokenValid()
    {
        tokenStatusLabel.setIcon(validIcon);
    }

    private void showTokenInvalid()
    {
        tokenStatusLabel.setIcon(invalidIcon);
    }

    private void showTokenPending()
    {
        tokenStatusLabel.setIcon(pendingIcon);
    }

    private void initValidationIcons()
    {
        validIcon   = new ImageIcon(InkinCrmVideoUploader.class.getResource("/resources/valid.png"));
        invalidIcon = new ImageIcon(InkinCrmVideoUploader.class.getResource("/resources/invalid.png"));
        pendingIcon = new ImageIcon(InkinCrmVideoUploader.class.getResource("/resources/loading.gif"));
    }

    private void onManageTokensClick()
    {
        navigateToCreateToken();

        if (isHostValidSync())
        {
            navigateToCreateToken();
        }
        else
        {
            hostField.requestFocus();
        }
    }

    private void navigateToCreateToken()
    {
        try
        {
            Desktop desktop = Desktop.getDesktop();
            desktop.browse(new URI(
                    InkinCrmVideoUploader.getServerUrl()
                    + "/admin/list/operation-tokens"));
        }
        catch (Exception e) {}
    }

    private boolean isHostValidSync()
    {
        return InkinCrmVideoUploader.getHostValidator().validateSync(hostField.getText());
    }

    private void probeHostAndUpdateStatus()
    {
        InkinCrmVideoUploader.getHostValidator().validate(hostField.getText());
    }

    private void validateTokenAndUpdateStatus()
    {
        TokenValidator validator = hostChanged
                ? getTempHostTokenValidator()
                : InkinCrmVideoUploader.getTokenValidator();

        validator.validate(tokenField.getText());
        //InkinCrmVideoUploader.getTokenValidator().validate(tokenField.getText());
    }

    private TokenValidator getTempHostTokenValidator()
    {
        if (tempHostTokenValidator == null)
        {
            tempHostTokenValidator = new TokenValidator(
                    () -> InkinCrmVideoUploader.fixServerUrl(hostField.getText()));

            tempHostTokenValidator.addListener(tokenValidatorListener);
        }

        return tempHostTokenValidator;
    }

    public void loadConfig()
    {
        hostField.setText(InkinCrmVideoUploader.getServerUrl());
        tokenField.setText(InkinCrmVideoUploader.getApiToken());

        probeHostAndUpdateStatus();
        validateTokenAndUpdateStatus();
    }

    private void onOk()
    {
        InkinCrmVideoUploader.setServerUrl(hostField.getText());
        InkinCrmVideoUploader.setApiToken(tokenField.getText());
        InkinCrmVideoUploader.saveConfig();
        setVisible(false);
    }
}
