package org.cyberpwn.technic;

import org.cyberpwn.technic.block.SpawnerBlock;
import org.cyberpwn.technic.block.VaccumHopperBlock;
import org.phantomapi.construct.Controllable;
import org.phantomapi.construct.Controller;

public class BlockController extends Controller
{
	private VaccumHopperBlock vaccumHopperBlock;
	private SpawnerBlock spawnerController;
	
	public BlockController(Controllable parentController)
	{
		super(parentController);
		
		vaccumHopperBlock = new VaccumHopperBlock(this);
		spawnerController = new SpawnerBlock(this);
		
		register(vaccumHopperBlock);
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
