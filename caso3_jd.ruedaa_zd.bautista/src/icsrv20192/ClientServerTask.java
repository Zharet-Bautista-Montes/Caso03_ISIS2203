package icsrv20192;

import uniandes.gload.core.*;

public class ClientServerTask extends Task
{
	private int ip = 0;	
	private String ha = ""; 
	private boolean gater; 
	
	public ClientServerTask(int socketip, String socketha, boolean gate)
	{ ip = socketip; ha = socketha; gater = gate; }

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
		if(gater)
		{ C client = new C();
		synchronized (C.class) {client.run(ha, ip, simalgo, hmacalgo);}; }
		else
		{ UnsafeClient client = new UnsafeClient();
		synchronized (UnsafeClient.class) {client.run(ha, ip, simalgo, hmacalgo);}; }
	}
}