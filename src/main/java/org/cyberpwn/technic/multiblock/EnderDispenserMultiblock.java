package org.cyberpwn.technic.multiblock;

import java.util.Iterator;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Dispenser;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.phantomapi.Phantom;
import org.phantomapi.clust.Comment;
import org.phantomapi.clust.ConfigurableController;
import org.phantomapi.clust.Keyed;
import org.phantomapi.construct.Controllable;
import org.phantomapi.construct.Ticked;
import org.phantomapi.inventory.PhantomInventory;
import org.phantomapi.lang.GList;
import org.phantomapi.lang.GSound;
import org.phantomapi.multiblock.Multiblock;
import org.phantomapi.multiblock.MultiblockStructure;
import org.phantomapi.sync.TaskLater;
import org.phantomapi.vfx.ParticleEffect;
import org.phantomapi.world.Cuboid;
import org.phantomapi.world.MaterialBlock;

@Ticked(100)
public class EnderDispenserMultiblock extends ConfigurableController
{
	private MultiblockStructure structure;
	
	@Comment("Should this structure be enabled?")
	@Keyed("enable")
	public boolean enabled = true;
	
	public EnderDispenserMultiblock(Controllable parentController)
	{
		super(parentController, "ender-dispenser");
		
		loadCluster(this, "multiblock");
		
		if(!enabled)
		{
			return;
		}
		
		structure = new MultiblockStructure("ender-dispenser");
		
		structure.add(0, 0, 0, new MaterialBlock(Material.CHEST, (byte) 5));
		structure.add(0, 0, 0, new MaterialBlock(Material.CHEST, (byte) 4));
		structure.add(0, 0, 0, new MaterialBlock(Material.CHEST, (byte) 3));
		structure.add(0, 0, 0, new MaterialBlock(Material.CHEST, (byte) 2));
		structure.add(1, 0, 0, new MaterialBlock(Material.OBSIDIAN));
		structure.add(-1, 0, 0, new MaterialBlock(Material.OBSIDIAN));
		structure.add(0, 0, 1, new MaterialBlock(Material.OBSIDIAN));
		structure.add(0, 0, -1, new MaterialBlock(Material.OBSIDIAN));
		structure.add(1, 0, 1, new MaterialBlock(Material.OBSIDIAN));
		structure.add(-1, 0, 1, new MaterialBlock(Material.OBSIDIAN));
		structure.add(1, 0, -1, new MaterialBlock(Material.OBSIDIAN));
		structure.add(-1, 0, -1, new MaterialBlock(Material.OBSIDIAN));
		structure.add(1, 1, 1, new MaterialBlock(Material.NETHER_FENCE));
		structure.add(-1, 1, 1, new MaterialBlock(Material.NETHER_FENCE));
		structure.add(1, 1, -1, new MaterialBlock(Material.NETHER_FENCE));
		structure.add(-1, 1, -1, new MaterialBlock(Material.NETHER_FENCE));
		
		structure.register();
	}
	
	@Override
	public void onTick()
	{
		if(!enabled)
		{
			return;
		}
		
		for(Multiblock i : Phantom.instance().getMultiblockRegistryController().getMultiblocks("ender-dispenser"))
		{
			Block b = i.getMapping().get(new Vector(0, 0, 0)).getBlock();
			
			try
			{
				GList<Block> dispensers = new GList<Block>();
				Chest c = (Chest) b.getState();
				Cuboid u = new Cuboid(c.getLocation().clone().add(20, 20, 20), c.getLocation().clone().add(-20, -20, -20));
				Iterator<Block> it = u.iterator();
				
				while(it.hasNext())
				{
					Block d = it.next();
					
					if(d.getType().equals(Material.DISPENSER))
					{
						dispensers.add(d);
					}
				}
				
				if(dispensers.isEmpty())
				{
					return;
				}
				
				for(int j = 0; j < c.getInventory().getSize(); j++)
				{
					ItemStack is = c.getInventory().getItem(j);
					
					if(is != null && !is.getType().equals(Material.AIR))
					{
						Dispenser di = (Dispenser) dispensers.pickRandom().getState();
						PhantomInventory pi = new PhantomInventory(di.getInventory());
						
						if(pi.hasSpace())
						{
							pi.addItem(is);
							c.getInventory().setItem(j, new ItemStack(Material.AIR));
							
							new TaskLater(j)
							{
								@Override
								public void run()
								{
									new GSound(Sound.ENDERMAN_TELEPORT, 1f, 1.5f).play(di.getLocation());
									ParticleEffect.PORTAL.display(0.5f, 32, di.getLocation().clone().add(0.5, 1, 0.5), 24);
									ParticleEffect.PORTAL.display(0.5f, 12, c.getLocation().clone().add(0.5, 1, 0.5), 24);
								}
							};
						}
					}
				}
			}
			
			catch(Exception e)
			{
				e.printStackTrace();
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
