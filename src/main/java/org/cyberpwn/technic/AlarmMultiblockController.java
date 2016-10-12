package org.cyberpwn.technic;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.util.Vector;
import org.phantomapi.Phantom;
import org.phantomapi.construct.Controllable;
import org.phantomapi.construct.Controller;
import org.phantomapi.construct.Ticked;
import org.phantomapi.lang.GSound;
import org.phantomapi.multiblock.Multiblock;
import org.phantomapi.multiblock.MultiblockStructure;
import org.phantomapi.vfx.ParticleEffect;
import org.phantomapi.world.MaterialBlock;

@Ticked(4)
public class AlarmMultiblockController extends Controller
{
	private MultiblockStructure structure;
	
	public AlarmMultiblockController(Controllable parentController)
	{
		super(parentController);
		
		structure = new MultiblockStructure("alarm");
		structure.add(0, 0, 0, new MaterialBlock(Material.QUARTZ_BLOCK, (byte) 1));
		structure.add(0, 1, 0, new MaterialBlock(Material.QUARTZ_BLOCK, (byte) 2));
		
		for(int i = 0; i < 3; i++)
		{
			structure.add(0, 2 + i, 0, new MaterialBlock(Material.NOTE_BLOCK));
			structure.add(1, 2 + i, 0, new MaterialBlock(Material.NOTE_BLOCK));
			structure.add(-1, 2 + i, 0, new MaterialBlock(Material.NOTE_BLOCK));
			structure.add(0, 2 + i, 1, new MaterialBlock(Material.NOTE_BLOCK));
			structure.add(0, 2 + i, -1, new MaterialBlock(Material.NOTE_BLOCK));
			structure.add(1, 2 + i, 1, new MaterialBlock(Material.NOTE_BLOCK));
			structure.add(-1, 2 + i, -1, new MaterialBlock(Material.NOTE_BLOCK));
			structure.add(-1, 2 + i, 1, new MaterialBlock(Material.NOTE_BLOCK));
			structure.add(1, 2 + i, -1, new MaterialBlock(Material.NOTE_BLOCK));
		}
		
		structure.register();
	}
	
	@Override
	public void onTick()
	{
		for(Multiblock i : Phantom.instance().getMultiblockRegistryController().getMultiblocks("alarm"))
		{
			if(i.getMapping().get(new Vector(0, 0, 0)).getBlock().isBlockIndirectlyPowered())
			{
				Location l = i.getMapping().get(new Vector(0, 3, 0));
				new GSound(Sound.BAT_DEATH, 8f, 1.9f).play(l);
				
				for(Location j : i.getLocations())
				{
					ParticleEffect.SMOKE_NORMAL.display(0.2f, 3, j.clone().add(0.5, 0.5, 0.5), 64);
				}
			}
		}
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
