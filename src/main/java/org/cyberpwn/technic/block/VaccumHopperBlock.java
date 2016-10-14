package org.cyberpwn.technic.block;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.phantomapi.clust.Comment;
import org.phantomapi.clust.ConfigurableController;
import org.phantomapi.clust.Keyed;
import org.phantomapi.construct.Controllable;
import org.phantomapi.construct.Ticked;
import org.phantomapi.event.NestChunkLoadEvent;
import org.phantomapi.event.NestChunkUnloadEvent;
import org.phantomapi.inventory.PhantomInventory;
import org.phantomapi.lang.GList;
import org.phantomapi.lang.GSound;
import org.phantomapi.nest.Nest;
import org.phantomapi.nest.NestedBlock;
import org.phantomapi.nest.NestedChunk;
import org.phantomapi.sync.ExecutiveRunnable;
import org.phantomapi.util.C;
import org.phantomapi.util.Chunks;
import org.phantomapi.vfx.ParticleEffect;
import org.phantomapi.world.Area;

@Ticked(0)
public class VaccumHopperBlock extends ConfigurableController
{
	private GList<Block> vaccums;
	private GList<Entity> marked;
	private int delay;
	
	@Comment("The interval a hopper sucks up nearby items")
	@Keyed("interval")
	public int interval = 20;
	
	@Comment("The Range of the vaccum")
	@Keyed("range")
	public double range = 4.3;
	
	@Comment("Should vaccum hoppers exist?")
	@Keyed("enabled")
	public boolean enable = true;
	
	public VaccumHopperBlock(Controllable parentController)
	{
		super(parentController, "vaccum-hopper");
		
		marked = new GList<Entity>();
		vaccums = new GList<Block>();
		
		loadCluster(this, "block");
		
		if(!enable)
		{
			return;
		}
		
		delay = interval;
		
		ShapedRecipe r = new ShapedRecipe(getItem());
		r.shape("***", "B%B", "***");
		r.setIngredient('*', Material.EYE_OF_ENDER);
		r.setIngredient('%', Material.HOPPER);
		r.setIngredient('B', Material.ENDER_CHEST);
		getPlugin().getServer().addRecipe(r);
	}
	
	@Override
	public void onTick()
	{
		if(!enable)
		{
			return;
		}
		
		delay--;
		
		if(delay <= 0)
		{
			delay = interval;
			
			vaccums.schedule(new ExecutiveRunnable<Block>()
			{
				@Override
				public void run()
				{
					Block next = next();
					
					if(!next.getType().equals(Material.HOPPER))
					{
						return;
					}
					
					ParticleEffect.PORTAL.display(0.4f, 4, next.getLocation().add(0.5, 0.5, 0.5), 32);
					Hopper h = (Hopper) next.getState();
					Inventory inv = h.getInventory();
					
					try
					{
						if(new PhantomInventory(inv).hasSpace())
						{
							Area a = new Area(next.getLocation().add(0.5, 0.5, 0.5), range);
							
							for(Entity i : a.getNearbyEntities())
							{
								if(i.getType().equals(EntityType.DROPPED_ITEM))
								{
									if(marked.contains(i))
									{
										continue;
									}
									
									marked.add(i);
									Item it = (Item) i;
									ItemStack is = it.getItemStack().clone();
									inv.addItem(is);
									new GSound(Sound.ENDERMAN_TELEPORT, 1f, 1.5f).play(i.getLocation());
									i.remove();
								}
							}
						}
					}
					
					catch(Exception ex)
					{
						
					}
				}
			});
		}
	}
	
	@Override
	public void onStart()
	{
		if(!enable)
		{
			return;
		}
		
		for(Chunk i : Chunks.getLoadedChunks())
		{
			NestedChunk c = Nest.getChunk(i);
			
			if(c != null)
			{
				for(NestedBlock j : c.getBlocks().v())
				{
					if(j.contains("ttype") && j.getString("ttype").equals("vaccum-hopper") && j.getLocation().toLocation().getBlock().getType().equals(Material.HOPPER))
					{
						vaccums.add(j.getLocation().toLocation().getBlock());
					}
				}
			}
		}
	}
	
	@Override
	public void onStop()
	{
		
	}
	
	@EventHandler
	public void on(NestChunkLoadEvent e)
	{
		if(!enable)
		{
			return;
		}
		
		for(NestedBlock i : e.getNestedChunk().getBlocks().v())
		{
			if(i.contains("ttype") && i.getString("ttype").equals("vaccum-hopper") && i.getLocation().toLocation().getBlock().getType().equals(Material.HOPPER))
			{
				vaccums.add(i.getLocation().toLocation().getBlock());
			}
		}
	}
	
	@EventHandler
	public void on(NestChunkUnloadEvent e)
	{
		if(!enable)
		{
			return;
		}
		
		for(NestedBlock i : e.getNestedChunk().getBlocks().v())
		{
			vaccums.remove(i.getLocation().toLocation().getBlock());
		}
	}
	
	@EventHandler
	public void on(BlockPlaceEvent e)
	{
		if(!enable)
		{
			return;
		}
		
		if(e.getItemInHand().getType().equals(Material.HOPPER))
		{
			try
			{
				ItemStack is = e.getItemInHand();
				ItemMeta im = is.getItemMeta();
				
				if(im.getDisplayName().equals(C.LIGHT_PURPLE + "Vaccum Hopper") && im.getLore().get(0).equals(C.DARK_PURPLE + "Sucks up nearby items"))
				{
					Nest.getBlock(e.getBlock()).set("ttype", "vaccum-hopper");
					vaccums.add(e.getBlock());
				}
			}
			
			catch(Exception ex)
			{
				
			}
		}
	}
	
	@EventHandler
	public void on(BlockBreakEvent e)
	{
		if(!enable)
		{
			return;
		}
		
		if(Nest.getBlock(e.getBlock()).contains("ttype") && Nest.getBlock(e.getBlock()).getString("ttype").equals("vaccum-hopper"))
		{
			e.setCancelled(true);
			Nest.getBlock(e.getBlock()).remove("ttype");
			vaccums.remove(e.getBlock());
			e.getBlock().setType(Material.AIR);
			e.getBlock().getWorld().dropItem(e.getBlock().getLocation(), getItem());
		}
	}
	
	public ItemStack getItem()
	{
		ItemStack is = new ItemStack(Material.HOPPER);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(C.LIGHT_PURPLE + "Vaccum Hopper");
		im.setLore(new GList<String>().qadd(C.DARK_PURPLE + "Sucks up nearby items"));
		is.setItemMeta(im);
		
		return is;
	}
}
