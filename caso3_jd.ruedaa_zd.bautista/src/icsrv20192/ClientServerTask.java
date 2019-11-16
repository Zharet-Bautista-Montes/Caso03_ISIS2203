package icsrv20192;

import uniandes.gload.core.*;

public class ClientServerTask extends Task
{
	static int ip;
	
	static String hostname; 

	@Override
	public void fail() 
	{
		System.out.println(Task.MENSAJE_FAIL);
	}

	@Override
	public void success() 
	{
		System.out.println(Task.OK_MESSAGE);
	}

	@Override
	public void execute() 
	{
		int simalgo = (int) Math.random()*2;
		int hmacalgo = (int) Math.random()*4 + 3;
		Cliente client = new Cliente();
		client.run("localhost", 5386, simalgo, hmacalgo);
	}
}