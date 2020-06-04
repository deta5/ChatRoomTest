package app;

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.Vector;
import javax.swing.*;
import vo.customer;

public class Server extends JFrame implements Runnable{
    private Socket socket=null;
    private ServerSocket serverSocket=null;
    private Vector<ChatThread>clients=new Vector<ChatThread>();
    private Vector<customer>userList=new Vector<customer>();
    private JButton jbt=new JButton("关闭服务器");
    private Boolean canRun=true;
    public Server()throws Exception{
        this.setTitle("服务器端");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.add(jbt,BorderLayout.NORTH);
        jbt.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e){
                System.exit(0);
            }
        });
        this.setBackground(Color.yellow);
        this.setSize(300,100);
        this.setVisible(true);
        serverSocket = new ServerSocket(9999);
        new Thread(this).start();
    }

    public void run()
    {
        try{
            while(canRun){
                socket=serverSocket.accept();
                ChatThread ct=new ChatThread(socket,this);
                ct.start();
            }
        }catch(Exception ex){
            canRun=false;
            try{
                serverSocket.close();
            }catch (Exception e){}
        }
    }

    public Vector<ChatThread>getClients()
    {
        return clients;
    }

    public Vector<customer> getUserList() {
        return userList;
    }
}
