package org.cyberpwn.technic;

import org.phantomapi.construct.Controllable;
import org.phantomapi.construct.Controller;

public class TechnicController extends Controller
{
	private SpawnerController spawnerController;
	
	public TechnicController(Controllable parentController)
	{
		super(parentController);
		
		this.spawnerController = new SpawnerController(this);
		
		register(spawnerController);
	}

	@Override
	public void onStart()
	{
		
	}

	@Override
	public void onStop()
	{
		
	}
}
