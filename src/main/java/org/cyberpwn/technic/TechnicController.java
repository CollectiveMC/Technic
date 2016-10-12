package org.cyberpwn.technic;

import org.phantomapi.construct.Controllable;
import org.phantomapi.construct.Controller;

public class TechnicController extends Controller
{
	private SpawnerController spawnerController;
	private MultiblockController multiblockController;
	
	public TechnicController(Controllable parentController)
	{
		super(parentController);
		
		multiblockController = new MultiblockController(this);
		spawnerController = new SpawnerController(this);
		
		register(spawnerController);
		register(multiblockController);
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
