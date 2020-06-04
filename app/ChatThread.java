package app;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import util.conf;
import util.FileOpe;
import vo.customer;
import vo.message;

public class ChatThread extends Thread{
    private Socket socket=null;
    private ObjectInputStream ois=null;
    private ObjectOutputStream oos=null;
    private customer customer1=null;

    private Server server;
    private boolean canRun=true;
    public ChatThread(Socket socket,Server server) throws Exception{
        this.socket=socket;
        this.server=server;
        oos=new ObjectOutputStream(socket.getOutputStream());
        ois=new ObjectInputStream(socket.getInputStream());
    }
    public void run()
    {
        try{
            while(canRun){
                message msg=(message)ois.readObject();
                String type=msg.getType();
                if(type.equals(conf.LOGIN)){
                    this.handleLogin(msg);
                }else if(type.equals(conf.REGISTER)){
                    this.handleRegister(msg);
                }else if(type.equals(conf.MESSAGE)){
                    this.handleMessage(msg);
                }
            }
        }catch (Exception ex){
            this.handleLogout();
        }
    }
    public void handleLogin(message msg)throws Exception{
        customer loginCustomer =(customer)msg.getContent();
        String account=loginCustomer.getAccount();
        String password=loginCustomer.getPassword();
        customer cus=FileOpe.getCustomerByAccount(account);
        message newMsg=new message();
        if(cus==null||!cus.getPassword().equals(password)){
            newMsg.setType(conf.LOGINFALL);
            oos.writeObject(newMsg);
            canRun=false;
            socket.close();
            return;
        }
        this.customer1=cus;
        server.getClients().add(this);
        server.getUserList().add(this.customer1);
        newMsg.setType(conf.USERLIST);
        newMsg.setContent(server.getUserList().clone());
        this.sendMessage(newMsg,conf.ALL);
        server.setTitle("当前在线:"+server.getClients().size()+"人");
    }
    public void handleRegister(message msg) throws Exception{
        customer registerCustomer=(customer)msg.getContent();
        String account=registerCustomer.getAccount();
        customer cus=FileOpe.getCustomerByAccount(account);
        message newMsg=new message();
        if(cus!=null){
            newMsg.setType(conf.REGISTERFALL);
    } else{
        String password=registerCustomer.getPassword();
        String name=registerCustomer.getName();
        FileOpe.insertCustomer(account,password,name);
        newMsg.setType(conf.REGISTERSUCCESS);
        oos.writeObject(newMsg);
    }
        oos.writeObject(newMsg);
        canRun=false;
        socket.close();
}

public void handleMessage(message msg) throws Exception{
        String to=msg.getTo();
        sendMessage(msg,to);
    }
    public void handleLogout()
    {
        message logoutMessage=new message();
        logoutMessage.setType(conf.LOGOUT);
        logoutMessage.setContent(this.customer1);
        server.getClients().remove(this);
        server.getUserList().remove(this.customer1);
        try {
            sendMessage(logoutMessage,conf.ALL);
            canRun=false;
            socket.close();
        }catch (Exception ex){
            ex.printStackTrace();
        }
        server.setTitle("当前在线："+server.getClients().size()+"人");
    }
    public void sendMessage(message msg,String to)throws Exception{
        for (ChatThread ct:server.getClients()){
            if(ct.customer1.getAccount().equals(to)||to.equals(conf.ALL)){
                ct.oos.writeObject(msg);
            }
        }
    }
}
