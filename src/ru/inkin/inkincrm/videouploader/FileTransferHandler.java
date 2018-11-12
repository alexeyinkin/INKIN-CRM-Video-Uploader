package ru.inkin.inkincrm.videouploader;

import java.util.List;
import java.io.File;
import java.awt.datatransfer.*;
import javax.swing.*;

/**
 *
 * @author Alexey
 */
public class FileTransferHandler extends TransferHandler
{
    MainWindow mainWindow;

    public void setMainWindow(MainWindow mainWindow)
    {
        this.mainWindow = mainWindow;
    }

    public boolean canImport(TransferSupport support)
    {
        //return true;
        try
        {
            Transferable transferable = support.getTransferable();

            for (DataFlavor flavor : transferable.getTransferDataFlavors())
            {
                if (flavor.equals(DataFlavor.javaFileListFlavor)) return true;
            }
        }
        catch (Exception e)
        {
        }

        return false;
    }

    public boolean importData(TransferSupport support)
    {
        try
        {
            Transferable transferable = support.getTransferable();
            List<File> droppedFiles = (List<File>)transferable.getTransferData(DataFlavor.javaFileListFlavor);

            for (File file : droppedFiles)
            {
                FileTask task = new FileTask();
                task.setFile(file);
                InkinCrmVideoUploader.addFileTask(task);
            }
        }
        catch (Exception e)
        {
            return false;
        }

        return true;
    }
}
