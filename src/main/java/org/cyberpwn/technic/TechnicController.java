package org.cyberpwn.technic;

import org.phantomapi.construct.Controllable;
import org.phantomapi.construct.Controller;

public class TechnicController extends Controller
{
	private MultiblockController multiblockController;
	private BlockController blockController;
	
	public TechnicController(Controllable parentController)
	{
		super(parentController);
		
		blockController = new BlockController(this);
		multiblockController = new MultiblockController(this);
		
		register(blockController);
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
