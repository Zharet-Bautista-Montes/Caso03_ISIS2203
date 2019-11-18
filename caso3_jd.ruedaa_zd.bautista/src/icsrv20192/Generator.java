package icsrv20192;

import java.util.Scanner;

import uniandes.gload.core.*;

public class Generator 
{
	private LoadGenerator generator;
	
	public static int portip;	
	public static String hostaddress;
	public static boolean swap; 
	public Scanner generatorconfig;
	
	public Generator()
	{
		generatorconfig = new Scanner(System.in);
		System.out.println("Ingrese la dirección IP en donde se ubica el servidor");
		hostaddress = generatorconfig.next();
		System.out.println("Ingrese el puerto para conectarse al servidor");
		portip = generatorconfig.nextInt();
		System.out.println("Indique si el servidor está en modo seguro (true) o inseguro (false)");
		swap = new Boolean(generatorconfig.next());
		Task work = createTask(); 
		int numberOfTasks = 80; 
		int gapBetweenTasks = 100;
		generator = new LoadGenerator("Prueba de Carga Cliente-Servidor", numberOfTasks, work, gapBetweenTasks);
		generator.generate();
	}
	
	public Task createTask()
	{
		return new ClientServerTask(portip, hostaddress, swap); 
	}
	
	public static void main(String[] args) 
	{
		@SuppressWarnings("unused")
		Generator gen = new Generator();
	}

}
