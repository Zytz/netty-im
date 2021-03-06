package com.wenwei.netty;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) throws IOException{
		NioServer nioServer = new NioServer();

		SpringApplication.run(DemoApplication.class, args);
	}
}
