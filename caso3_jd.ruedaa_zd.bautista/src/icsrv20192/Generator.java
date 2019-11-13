package icsrv20192;

import uniandes.gload.core.*;

public class Generator 
{

	private LoadGenerator generator;
	
	public Generator()
	{
		Task work = createTask(); 
		int numberOfTasks = 100; 
		int gapBetweenTasks = 1000;
		generator = new LoadGenerator("Prueba de Carga Cliente-Servidor", numberOfTasks, work, gapBetweenTasks);
		generator.generate();
	}
	
	public Task createTask()
	{
		return new ClientServerTask(); 
	}
	
	public static void main(String[] args) 
	{
		@SuppressWarnings("unused")
		Generator gen = new Generator();
	}

}
