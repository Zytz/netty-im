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

/**
 * author:zhou_wenwei
 * mail:zhou_wenwei@wuxiapptec.com
 * date:2018/10/9
 * description: server 
 */
public class NioServer {
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    
    
    public  NioServer()  throws IOException {

        serverSocketChannel = ServerSocketChannel.open();
        //必须设置成非阻塞的
        serverSocketChannel.configureBlocking(false);
        
        serverSocketChannel.socket().bind(new InetSocketAddress(8081));
        //创建selector
        selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("server start up OK ");
        handleKeys();
        
        
    }

    private void handleKeys() throws IOException{
        while (true){
            int selectNums = selector.select(30*1000L);
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
            handleAcceptkey(key);
        }
        if (key.isReadable()){
            handleReadKey(key);
        }
        if (key.isWritable()){
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
        responseQueue.clear();
        clientSocketChannel.register(selector,SelectionKey.OP_READ,responseQueue);
    }

    //channer 处理读的逻辑
    private void handleReadKey(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        ByteBuffer byteBuffer = CodeUtils.read(socketChannel);
        if(byteBuffer == null){
            System.out.println("处理结束");
            socketChannel.register(selector,0);
            return;
        }
        if(byteBuffer.position()>0){
          String content =  CodeUtils.newString(byteBuffer);
          System.out.println("读取数据：" + content);
          List<String> responseQueue = (List<String>) key.attachment();
          responseQueue.add("响应response :"+content);
          socketChannel.register(selector,SelectionKey.OP_WRITE,responseQueue);
        }
    }

    private void handleAcceptkey(SelectionKey key) throws IOException{
        SocketChannel serverSocketChannel = ((ServerSocketChannel)key.channel()).accept();
        serverSocketChannel.configureBlocking(false);
        //注册客户端的client
        serverSocketChannel.register(selector,SelectionKey.OP_READ,new ArrayList<String>());
    }

}
