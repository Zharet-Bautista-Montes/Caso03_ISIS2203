package icsrv20192;

import java.net.InetAddress;
import java.net.UnknownHostException;

import uniandes.gload.core.*;

public class ClientServerTask extends Task
{
	static int ip; 

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
		Cliente client = new Cliente();
		try 
		{ client.run(InetAddress.getLocalHost().getHostName(), ip); } 
		catch (UnknownHostException e) { e.printStackTrace(); }
	}
}