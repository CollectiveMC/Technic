package org.cyberpwn.technic;

import org.phantomapi.construct.Ghost;

public class Technic extends Ghost
{
	private static Technic instance;
	private TechnicController controller;
	
	@Override
	public void preStart()
	{
		instance = this;
		controller = new TechnicController(this);
		
		register(controller);
	}
	
	@Override
	public void onStart()
	{
		
	}
	
	@Override
	public void onStop()
	{
		
	}
	
	@Override
	public void postStop()
	{
		
	}
	
	public static Technic instance()
	{
		return instance;
	}
	
	public static Technic getInstance()
	{
		return instance;
	}
	
	public TechnicController getTechnicController()
	{
		return controller;
	}
}
