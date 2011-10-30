package ar.edu.itba.it.pdc;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import ar.edu.itba.it.pdc.proxy.IsecuServer;

public class Isecu {
	
	public static Logger log = Logger.getRootLogger();
	
	public static void main(String[] args) throws IOException{
		ApplicationContext context = new ClassPathXmlApplicationContext("context.xml");
		BeanFactory factory = context;
		IsecuServer is = (IsecuServer) factory.getBean("isecuServer");
				
		new Isecu(is);
	}
	
	public Isecu(IsecuServer isecuServer) {
		try {
			isecuServer.start();
		} catch (IOException e) {
			System.out.println("Error en el server!");
			e.printStackTrace();
		}
	}
	
}
