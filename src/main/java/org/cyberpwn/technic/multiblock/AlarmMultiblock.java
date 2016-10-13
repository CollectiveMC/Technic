package org.cyberpwn.technic.multiblock;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.util.Vector;
import org.phantomapi.Phantom;
import org.phantomapi.clust.Comment;
import org.phantomapi.clust.ConfigurableController;
import org.phantomapi.clust.Keyed;
import org.phantomapi.construct.Controllable;
import org.phantomapi.construct.Ticked;
import org.phantomapi.lang.GSound;
import org.phantomapi.multiblock.Multiblock;
import org.phantomapi.multiblock.MultiblockStructure;
import org.phantomapi.vfx.ParticleEffect;
import org.phantomapi.world.MaterialBlock;

@Ticked(0)
public class AlarmMultiblock extends ConfigurableController
{
	private MultiblockStructure structure;
	private int delay;
	
	@Comment("Should this structure be enabled?")
	@Keyed("enable")
	public boolean enabled = true;
	
	@Comment("The pitch of the alarm sound")
	@Keyed("options.alarm-pitch")
	public double pitch = 2.1;
	
	@Comment("The volume (range) of the sound.")
	@Keyed("options.alarm-range")
	public double volume = 8;
	
	@Comment("The interval in ticks to screech")
	@Keyed("options.alarm-interval")
	public int interval = 30;
	
	@Comment("The sound to play")
	@Keyed("options.alarm-sound")
	public String sound = "WITHER_SPAWN";
	
	public AlarmMultiblock(Controllable parentController)
	{
		super(parentController, "alarm");
		
		loadCluster(this, "multiblock");
		
		if(!enabled)
		{
			return;
		}
		
		delay = interval;
		
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
		if(!enabled)
		{
			return;
		}
		
		delay--;
		
		if(delay <= 0)
		{
			for(Multiblock i : Phantom.instance().getMultiblockRegistryController().getMultiblocks("alarm"))
			{
				if(i.getMapping().get(new Vector(0, 0, 0)).getBlock().isBlockIndirectlyPowered())
				{
					Location l = i.getMapping().get(new Vector(0, 3, 0));
					new GSound(Sound.valueOf(sound), (float) volume, (float) pitch).play(l);
					
					for(Location j : i.getLocations())
					{
						ParticleEffect.SMOKE_NORMAL.display(0.2f, 3, j.clone().add(0.5, 0.5, 0.5), 64);
					}
				}
			}
			
			delay = interval;
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
