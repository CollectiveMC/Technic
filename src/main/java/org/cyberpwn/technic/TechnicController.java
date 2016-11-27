package org.cyberpwn.technic;

import org.phantomapi.command.CommandController;
import org.phantomapi.command.PhantomCommand;
import org.phantomapi.command.PhantomCommandSender;
import org.phantomapi.command.PhantomSender;
import org.phantomapi.construct.Controllable;
import org.phantomapi.lang.GList;

public class TechnicController extends CommandController
{
	private MultiblockController multiblockController;
	private BlockController blockController;
	
	public TechnicController(Controllable parentController)
	{
		super(parentController, "breakspawner");
		
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
	
	@Override
	public boolean onCommand(PhantomCommandSender sender, PhantomCommand command)
	{
		if(sender.isPlayer())
		{
			blockController.getSpawnerController().onTryBreak((PhantomSender) sender, command);
		}
		
		return true;
	}
	
	@Override
	public GList<String> getCommandAliases()
	{
		return new GList<String>().qadd("bs");
	}
}
