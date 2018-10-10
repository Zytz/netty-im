package com.wenwei.netty;
/**
 * Created by wenweizww on 2018/10/9.
 */



import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * author:zhou_wenwei
 * mail:zhou_wenwei@wuxiapptec.com
 * date:2018/10/9
 * description: server 
 */
public class NioClient {
    private SocketChannel socketChannel;
    private Selector selector;
    private CountDownLatch connected = new CountDownLatch(1);

    private List<String> responseQueue = new ArrayList<>();
    
    
    public  NioClient() throws IOException,InterruptedException{

        socketChannel = SocketChannel.open();
        //必须设置成非阻塞的
        socketChannel.configureBlocking(false);

//        socketChannel.socket().bind(new InetSocketAddress(8080));
        //创建selector
        selector = Selector.open();
        socketChannel.register(selector, SelectionKey.OP_CONNECT);

        socketChannel.connect(new InetSocketAddress(8081));
        System.out.println("链接成功");
//        handleKeys();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    handleKeys();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        if (connected.getCount() != 0) {
            connected.await();
        }
        System.out.println("Client 启动完成");
        
        
    }

    private void handleKeys() throws IOException{
        while (true){
            int selectNums = selector.select(3000L);
            if (selectNums == 0){
                continue;
            }
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()){
                SelectionKey key = iterator.next();
                //先移除key
                iterator.remove();
                if(!key.isValid()) continue;
                handleKey(key);
            }
            
        }
    }

    private void handleKey(SelectionKey key) throws IOException{
        if(key.isAcceptable()){
            handleConnectableKey(key);
        }else if (key.isReadable()){
            handleReadKey(key);
        }else if (key.isWritable()){
            handleWriteKey(key);
        }
    }

    private void handleWriteKey(SelectionKey key) throws ClosedChannelException{
        SocketChannel clientSocketChannel = (SocketChannel) key.channel();
        List<String> responseQueue = (List<String>) key.attachment();
        for(String s : responseQueue){
            System.out.println("响应数据 ："+s);
            CodeUtils.write(clientSocketChannel,s);
        }
        clientSocketChannel.register(selector,SelectionKey.OP_READ,responseQueue);
    }

    //channer 处理读的逻辑
    private void handleReadKey(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        ByteBuffer byteBuffer = CodeUtils.read(socketChannel);
//        if(byteBuffer == null){
//            System.out.println("处理结束");
//            socketChannel.register(selector,0);
//            return;
//        }
        if(byteBuffer.position()>0){
          String content =  CodeUtils.newString(byteBuffer);
          System.out.println("读取数据：" + content);
//          List<String> responseQueue = (List<String>) key.attachment();
//          responseQueue.add("响应response :"+content);
//          socketChannel.register(selector,SelectionKey.OP_WRITE,responseQueue);
        }
    }

    private void handleConnectableKey(SelectionKey key) throws IOException{

        // 完成连接
        if (!socketChannel.isConnectionPending()) {
            return;
        }
        socketChannel.finishConnect();
        // log
        System.out.println("接受新的 Channel");
        // 注册 Client Socket Channel 到 Selector
        socketChannel.register(selector, SelectionKey.OP_READ, responseQueue);
        // 标记为已连接
        connected.countDown();
    }
    public synchronized void send(String content) throws ClosedChannelException {
        // 添加到响应队列
        responseQueue.add(content);
        // 打印数据
        System.out.println("写入数据：" + content);
        // 注册 Client Socket Channel 到 Selector
        socketChannel.register(selector, SelectionKey.OP_WRITE, responseQueue);
        selector.wakeup();
    }
    public static void main(String[] args) throws IOException, InterruptedException {
        NioClient client = new NioClient();
        for (int i = 0; i < 30; i++) {
            client.send("nihaoTTTT: " + i);
            Thread.sleep(1000L);
        }
    }

}
