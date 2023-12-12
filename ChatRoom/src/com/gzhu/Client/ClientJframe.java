package com.gzhu.Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

class ClientJframe extends JFrame {

    private JTextArea showHistory = new JTextArea(10, 20);
    private JTextField inputArea = new JTextField(20);
    private JButton sendButton = new JButton("发送");

    private static final String CONNSTR = "127.0.0.1";
    private static final int CONNPORT = 0001;

    private Socket socket = null;
    private DataOutputStream dataOutputStream = null;
    private boolean isConn = false;
    private String username;

    public ClientJframe() throws HeadlessException {
        super();
    }

    //初始化用户名、获取用户名
    private void getUsername() {
        this.username = JOptionPane.showInputDialog("请输入用户名:");
    }

    //发送用户名到服务器
    private void sendUsernameToServer() {
        try {
            //创建一个新的DataOutputStream用于发送用户名
            DataOutputStream usernameStream = new DataOutputStream(socket.getOutputStream());
            //发送用户名到服务器
            usernameStream.writeUTF(username);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void init() {
        this.setTitle("欢迎使用软件221何骐光32206300076聊天室应用");
        getUsername();
        if (username == null) {
            System.exit(0);
        }

        this.setLayout(new BorderLayout());

        JScrollPane scrollPane = new JScrollPane(showHistory);
        this.add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(inputArea, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        this.add(inputPanel, BorderLayout.SOUTH);

        this.setSize(500, 400);

        inputArea.addActionListener(e -> sendMessage());
        sendButton.addActionListener(e -> sendMessage());

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        showHistory.setEditable(false);
        inputArea.requestFocus();

        try {
            socket = new Socket(CONNSTR, CONNPORT);
            isConn = true;
            //在连接建立后，将用户名发送到服务器
            sendUsernameToServer();

        } catch (IOException e) {
            e.printStackTrace();
        }

        new Thread(new Receive()).start();
        this.setVisible(true);
    }

    //输入后，清空输入框
    private void sendMessage() {
        String input = inputArea.getText().trim();
        if (!input.isEmpty()) {
            send(input);
            inputArea.setText("");
        }
    }

    //发送到服务器
    public void send(String str) {
        try {
            if (dataOutputStream == null) {
                dataOutputStream = new DataOutputStream(socket.getOutputStream());
            }
            dataOutputStream.writeUTF(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class Receive implements Runnable {
        @Override
        public void run() {
            try {
                while (isConn) {
                    DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                    String str = dataInputStream.readUTF();
                    showHistory.append(str + "\n");
                }
            } catch (SocketException e) {
                System.out.println("The server was unexpectedly aborted!");
                showHistory.append("The server was unexpectedly aborted!\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}