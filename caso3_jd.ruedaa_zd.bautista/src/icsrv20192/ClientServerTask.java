package icsrv20192;

import uniandes.gload.core.*;

public class ClientServerTask extends Task
{
	private int ip = 0;	
	private String ha = ""; 
	
	public ClientServerTask(int socketip, String socketha)
	{ ip = socketip; ha = socketha; }

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
		int simalgo = (int) (Math.random()*2);
		int hmacalgo = (int) (Math.random()*4 + 3);
		Cliente client = new Cliente();
		synchronized (Cliente.class) {client.run(ha, ip, simalgo, hmacalgo);};
	}
}