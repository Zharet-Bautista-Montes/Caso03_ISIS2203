package icsrv20192;

import uniandes.gload.core.*;

public class ClientServerTask extends Task
{

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
	}
}