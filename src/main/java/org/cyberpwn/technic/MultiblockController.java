package org.cyberpwn.technic;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.cyberpwn.technic.multiblock.AlarmMultiblock;
import org.cyberpwn.technic.multiblock.EnderDispenserMultiblock;
import org.cyberpwn.technic.multiblock.MobTurretMultiblock;
import org.phantomapi.construct.Controllable;
import org.phantomapi.construct.Controller;
import org.phantomapi.event.MultiblockConstructEvent;
import org.phantomapi.event.MultiblockDestroyEvent;
import org.phantomapi.lang.GSound;
import org.phantomapi.multiblock.MB;
import org.phantomapi.multiblock.Multiblock;
import org.phantomapi.multiblock.MultiblockStructure;
import org.phantomapi.nms.NMSX;
import org.phantomapi.sync.TaskLater;
import org.phantomapi.world.MaterialBlock;

public class MultiblockController extends Controller
{
	private AlarmMultiblock alarmMultiblockStructure;
	private MobTurretMultiblock mobTurretMultiblockStructure;
	private EnderDispenserMultiblock enderDispenserMultiblockStructure;
	
	public MultiblockController(Controllable parentController)
	{
		super(parentController);
		
		alarmMultiblockStructure = new AlarmMultiblock(this);
		mobTurretMultiblockStructure = new MobTurretMultiblock(this);
		enderDispenserMultiblockStructure = new EnderDispenserMultiblock(this);
		
		register(alarmMultiblockStructure);
		register(mobTurretMultiblockStructure);
		register(enderDispenserMultiblockStructure);
	}
	
	@Override
	public void onStart()
	{
		
	}
	
	@Override
	public void onStop()
	{
		
	}
	
	@EventHandler
	public void on(MultiblockConstructEvent e)
	{
		created(e.getMultiblock());
	}
	
	@EventHandler
	public void on(MultiblockDestroyEvent e)
	{
		broken(e.getMultiblock());
	}
	
	public static void created(Multiblock mb)
	{
		new GSound(Sound.ANVIL_USE, 2f, 0.6f).play(mb.getMapping().get(new Vector(0, 0, 0)));
		
		int k = 0;
		
		for(Vector i : mb.getMapping().k())
		{
			k += 1;
			
			new TaskLater(k)
			{
				@Override
				public void run()
				{
					Material m = mb.getMapping().get(i).getBlock().getType();
					NMSX.breakParticles(mb.getMapping().get(i), m, 24);
					new GSound(Sound.ITEM_PICKUP, 0.5f, 0.6f).play(mb.getMapping().get(i));
				}
			};
		}
	}
	
	public static void broken(Multiblock mb)
	{
		new GSound(Sound.ZOMBIE_WOODBREAK, 2f, 0.6f).play(mb.getMapping().get(new Vector(0, 0, 0)));
		MultiblockStructure s = MB.getStructure(mb);
		
		int k = 0;
		
		for(Vector i : mb.getMapping().k())
		{
			k += 1;
			
			new TaskLater(k)
			{
				@SuppressWarnings("deprecation")
				@Override
				public void run()
				{
					Material m = mb.getMapping().get(i).getBlock().getType();
					byte data = mb.getMapping().get(i).getBlock().getData();
					
					if(!s.contains(new MaterialBlock(m, data)))
					{
						return;
					}
					
					if(m.equals(Material.AIR))
					{
						return;
					}
					
					if(m.equals(Material.CHEST))
					{
						Chest c = (Chest) mb.getMapping().get(i).getBlock().getState();
						
						for(ItemStack is : c.getInventory().getContents())
						{
							if(is == null || is.getType().equals(Material.AIR))
							{
								continue;
							}
							
							mb.getMapping().get(i).getBlock().getWorld().dropItemNaturally(c.getLocation(), is);
						}
						
						c.getInventory().clear();
					}
					
					NMSX.breakParticles(mb.getMapping().get(i), m, 24);
					new GSound(Sound.WOOD_CLICK, 0.5f, 0.6f).play(mb.getMapping().get(i));
					ItemStack is = new ItemStack(m, 1, mb.getMapping().get(i).getBlock().getData());
					mb.getMapping().get(i).getBlock().setType(Material.AIR);
					mb.getMapping().get(i).getBlock().getWorld().dropItemNaturally(mb.getMapping().get(i), is);
				}
			};
		}
	}
}
