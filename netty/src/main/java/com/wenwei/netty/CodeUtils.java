package com.wenwei.netty;
/**
 * Created by wenweizww on 2018/10/10.
 */


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * author:zhou_wenwei
 * mail:zhou_wenwei@wuxiapptec.com
 * date:2018/10/10
 * description: 处理buffer逻辑
 */
public class CodeUtils {
    public static ByteBuffer read(SocketChannel channel){
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        try {
            int count  = channel.read(byteBuffer);
            if(count == -1){
                return null;
            }

        }catch (IOException e ){
            throw new RuntimeException(e);
        }
        return byteBuffer;
    }
    public static ByteBuffer write(SocketChannel channel,String context){
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        try {
            byteBuffer.put(context.getBytes("UTF-8"));

        }catch (UnsupportedEncodingException e ){
            throw new RuntimeException(e);
        }
        //切换读写方式，改成写的方式
        byteBuffer.flip();
        try {
            channel.write(byteBuffer);
        }catch (IOException e){
            throw new RuntimeException(e);
        }
        return byteBuffer;
    }
    public static String newString(ByteBuffer buffer) {
        buffer.flip();
        byte[] bytes = new byte[buffer.remaining()];
        System.arraycopy(buffer.array(), buffer.position(), bytes, 0, buffer.remaining());
        try {
            return new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

}
