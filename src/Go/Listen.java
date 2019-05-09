package Go;

import java.io.*;
import java.net.*;

public class Listen extends Thread
{
    Socket socket;
    MainWindow mainWindow;
    public Listen(Socket socket,MainWindow mainWindow)
    {
        this.socket=socket;
        this.mainWindow=mainWindow;
    }
    public void run()
    {
        try
        {
            this.activeListen(this.socket);
        }catch(IOException ioe){this.mainWindow.panelGo.showError("意外中断");}
    }
    void activeListen(Socket socket) throws IOException
    {
        BufferedReader reader=new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String message;
        while(true)
        {
            message=reader.readLine();
            this.mainWindow.doMessage(message);
        }
    }
}
