package com.wenwei.netty;
/**
 * Created by wenweizww on 2018/10/9.
 */



import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * author:zhou_wenwei
 * mail:zhou_wenwei@wuxiapptec.com
 * date:2018/10/9
 * description: server 
 */
public class NioServer {
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    
    
    public void NioServer() throws IOException{
        serverSocketChannel = ServerSocketChannel.open();
        //必须设置成非阻塞的
        serverSocketChannel.configureBlocking(false);
        
        serverSocketChannel.socket().bind(new InetSocketAddress(8080));
        //创建selector
        selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("链接成功");
        handleKeys();
        
        
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
                handleKey(key)
            }
            
        }
    }

    private void handleKey(SelectionKey key) throws IOException{
        if(key.isAcceptable()){
            handleAcceptkey(key);
        }else if (key.isReadable()){
            handleReadKey(key);
        }else if (key.isWritable()){
            handleWriteKey(key);
        }
    }

    private void handleReadKey(SelectionKey key) {
    }

    private void handleAcceptkey(SelectionKey key) throws IOException{
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        //注册客户端的client
        serverSocketChannel.register(selector,SelectionKey.OP_READ,new ArrayList<>());
    }

}
