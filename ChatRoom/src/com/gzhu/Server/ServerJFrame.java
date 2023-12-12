package com.gzhu.Server;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Iterator;

class ServerJFrame extends JFrame {
    //组件
    JTextArea serverTestArea = new JTextArea();
    JPanel jPanel = new JPanel();
    JButton startBtn = new JButton("启动");
    JButton stopBtn = new JButton("关闭");

    //端口
    private static final int PORT = 0001;

    //ServerSocket,在指定端口监听客户端的连接请求
    private ServerSocket serverSocket = null;

    //与客户端建立的Socket连接
    private Socket socket = null;

    //从客户端接收数据的输入流
    private DataInputStream dataInputStream = null;

    //存储多个客户端连接
    private ArrayList<ClientCoon> ccList = new ArrayList<ClientCoon>();

    //服务器启动的标志
    private boolean isStart = false;

    public void init() throws Exception {
        //初始化界面
        this.setTitle("欢迎使用软件221何骐光32206300076聊天室应用-服务器");
        this.add(serverTestArea, BorderLayout.CENTER);
        jPanel.add(startBtn);
        jPanel.add(stopBtn);
        this.add(jPanel, BorderLayout.SOUTH);
        this.setBounds(0, 0, 500, 500);

        //判断服务器是否已经开启
        if (isStart) {
            System.out.println("The server has already started\n");
        } else {
            System.out.println("The server has not started, please click start server!\n");
        }

        //按钮监听监听服务器开启，置开始位false
        startBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (serverSocket == null) {
                        serverSocket = new ServerSocket(PORT);
                    }
                    isStart = true;
                    serverTestArea.append("The server has already started！ \n");
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        //终止按钮监听停止服务器，置开始位true
        stopBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (serverSocket != null) {
                        serverSocket.close();
                        isStart = false;
                    }
                    System.exit(0);
                    serverTestArea.append("The server is closed！！\n");
                    System.out.println("The server is closed！\n");

                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        this.setVisible(true);
        startServer();
    }

    //服务器启动方法
    public void startServer() throws Exception {
        try {
            //尝试创建服务器套接字并启动服务器
            serverSocket = new ServerSocket(PORT);
            isStart = true; //设置服务器启动标志为true

            //在while循环中接收每一个客户端连接
            while (isStart) {
                //等待客户端连接
                socket = serverSocket.accept();

                //创建新的客户端连接对象，并将其加入连接列表
                ClientCoon clientCoon = new ClientCoon(socket);
                ccList.add(clientCoon);

                //等待用户名接收完毕
                Thread.sleep(100);

                // 接收客户端的用户名
                String clientUsername = clientCoon.receiveUsername();
                System.out.println("Client's username: " + clientUsername);

                //打印客户端连接信息
                System.out.println("\n" + "A client connects to the server：" + clientUsername + " " + socket.getInetAddress() + "/" + socket.getPort());

                //服务器追加客户端连接信息
                serverTestArea.append("\n" + "A client connects to the server：" + clientUsername + " " + socket.getInetAddress() + "/" + socket.getPort());
            }
        } catch (SocketException e) {
            System.out.println("Server interrupt!");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //在关闭服务器时，确保关闭服务器套接字
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        }
    }

    //客户端连接类，实现了Runnable接口
    class ClientCoon implements Runnable {
        Socket socket = null;
        String username;

        //初始化客户端连接对象，并启动线程
        public ClientCoon(Socket socket) {
            this.socket = socket;
            (new Thread(this)).start(); // 启动线程
        }

        //接收客户端用户名
        public String receiveUsername() {
            try {
                //只有在用户名为空时才接收新的用户名
                if (username == null || username.isEmpty()) {
                    DataInputStream usernameStream = new DataInputStream(socket.getInputStream());
                    //接收用户名
                    username = usernameStream.readUTF();
                }
                return username;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        //接受客户端信息的方法，用于多线程运行
        @Override
        public void run() {
            try {
                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                //先接收用户名
                username = receiveUsername();
                while (isStart) {
                    String str = dataInputStream.readUTF();
                   serverTestArea.append(username + " (" + socket.getInetAddress() + "|" + socket.getPort() + ")： "+"\n" + str + "\n");

                    //在所有客户端上展示
                    String strSend = (username + " ："+"\n"  + str + "\n");
                    Iterator<ClientCoon> iterator = ccList.iterator();
                    while (iterator.hasNext()) {
                        ClientCoon clientCoon = iterator.next();
                        clientCoon.send(strSend);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //服务器向每个连接对象发送数据
        public void send(String str) {
            try {
                DataOutputStream dataOutputStream = new DataOutputStream(this.socket.getOutputStream());
                dataOutputStream.writeUTF(str); //发送数据给客户端
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}