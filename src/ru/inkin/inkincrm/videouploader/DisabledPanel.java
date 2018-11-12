package ru.inkin.inkincrm.videouploader;

import java.awt.Component;
import javax.swing.JPanel;
import java.util.Map;
import java.util.HashMap;

public class DisabledPanel extends JPanel
{
    private final Map<Component, Boolean> oldEnabled = new HashMap<>();

    @Override
    public void setEnabled(boolean enabled)
    {
        if (isEnabled() != enabled)
        {
            if (enabled)
            {
                setEnabledTrue();
            }
            else
            {
                setEnabledFalse();
            }
        }

        super.setEnabled(enabled);
    }

    private void setEnabledFalse()
    {
        oldEnabled.clear();

        for (Component component : getComponents())
        {
            oldEnabled.put(component, component.isEnabled());
            component.setEnabled(false);
        }
    }

    private void setEnabledTrue()
    {
        for (Component component : getComponents())
        {
            component.setEnabled(oldEnabled.get(component));
        }
    }
}
