package org.cyberpwn.technic;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.phantomapi.clust.ConfigurableController;
import org.phantomapi.clust.Keyed;
import org.phantomapi.construct.Controllable;
import org.phantomapi.construct.Ticked;
import org.phantomapi.core.SyncStart;
import org.phantomapi.event.NestChunkLoadEvent;
import org.phantomapi.event.NestChunkUnloadEvent;
import org.phantomapi.gui.Click;
import org.phantomapi.gui.Element;
import org.phantomapi.gui.PhantomElement;
import org.phantomapi.gui.PhantomWindow;
import org.phantomapi.gui.Slot;
import org.phantomapi.gui.Window;
import org.phantomapi.lang.GList;
import org.phantomapi.lang.GListAdapter;
import org.phantomapi.lang.GLocation;
import org.phantomapi.lang.GSound;
import org.phantomapi.nest.Nest;
import org.phantomapi.sfx.Audio;
import org.phantomapi.sync.TaskLater;
import org.phantomapi.util.C;
import org.phantomapi.util.F;
import org.phantomapi.util.M;
import org.phantomapi.vfx.ParticleEffect;
import de.dustplanet.util.SilkUtil;

@Ticked(0)
@SyncStart
public class SpawnerController extends ConfigurableController
{
	@Keyed("spawner.levels")
	public int levels = 5;
	
	@Keyed("spawner.interval")
	public double interval = 0.25;
	
	private SilkUtil s;
	private GList<Block> mapped;
	
	public SpawnerController(Controllable parentController)
	{
		super(parentController, "spawners");
		
		s = SilkUtil.hookIntoSilkSpanwers();
		mapped = new GList<Block>();
	}
	
	@Override
	public void onStart()
	{
		loadCluster(this);
	}
	
	@Override
	public void onStop()
	{
		
	}
	
	@Override
	public void onTick()
	{
		for(Block i : mapped.copy())
		{
			if(Nest.getBlock(i) == null)
			{
				mapped.remove(i);
				continue;
			}
			
			if(Nest.getBlock(i).contains("t.s.v") && i.getType().equals(Material.MOB_SPAWNER))
			{
				CreatureSpawner cs = (CreatureSpawner) i.getState();
				
				if(M.r(getSpeed(i) / (levels * interval)))
				{
					try
					{
						cs.setDelay((int) (cs.getDelay() - (1 + getSpeed(i))));
						
						if(M.r(0.3))
						{
							ParticleEffect.FLAME.display((float) getSpeed(i) / 35, (int) (8 + getSpeed(i)), i.getLocation().add(0.5, 0.5, 0.5), 12);
						}
					}
					
					catch(Exception e)
					{
						mapped.remove(i);
					}
				}
			}
			
			else
			{
				mapped.remove(i);
			}
		}
	}
	
	public double getSpeed(Block block)
	{
		if(block.getType().equals(Material.MOB_SPAWNER) && Nest.getBlock(block).contains("t.s.v"))
		{
			return Nest.getBlock(block).getDouble("t.s.v");
		}
		
		return -1;
	}
	
	public void setSpeed(Block block, double value)
	{
		if(block.getType().equals(Material.MOB_SPAWNER))
		{
			Nest.getBlock(block).set("t.s.v", value);
		}
	}
	
	@EventHandler
	public void on(NestChunkLoadEvent e)
	{
		mapped.add(new GListAdapter<GLocation, Block>()
		{
			@Override
			public Block onAdapt(GLocation from)
			{
				if(!from.toLocation().getBlock().getType().equals(Material.MOB_SPAWNER))
				{
					return null;
				}
				
				return from.toLocation().getBlock();
			}
		}.adapt(e.getNestedChunk().getBlocks().k()));
	}
	
	@EventHandler
	public void on(NestChunkUnloadEvent e)
	{
		for(GLocation i : e.getNestedChunk().getBlocks().k())
		{
			mapped.remove(i.toLocation().getBlock());
		}
	}
	
	@EventHandler
	public void on(BlockPlaceEvent e)
	{
		if(e.getBlock().getType().equals(Material.MOB_SPAWNER))
		{
			Nest.getBlock(e.getBlock()).set("t.s.v", 0.0);
			mapped.add(e.getBlock());
			ItemStack is = e.getItemInHand();
			
			try
			{
				ItemMeta im = is.getItemMeta();
				
				for(String i : im.getLore())
				{
					String d = C.stripColor(i);
					
					if(d.startsWith("Boost: ") && d.endsWith("%"))
					{
						try
						{
							d = d.replaceAll("%", "").replaceAll("Boost: ", "");
							Integer v = Integer.valueOf(d);
							Double vv = ((double) v / 100.0);
							Nest.getBlock(e.getBlock()).set("t.s.v", vv);
							break;
						}
						
						catch(Exception ex)
						{
							
						}
					}
				}
			}
			
			catch(Exception exx)
			{
				
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void on(BlockBreakEvent e)
	{
		if(e.getBlock().getType().equals(Material.MOB_SPAWNER))
		{
			if(Nest.getBlock(e.getBlock()).contains("t.s.v"))
			{
				double v = getSpeed(e.getBlock());
				
				if(v > 0)
				{
					short id = s.getSpawnerEntityID(e.getBlock());
					String name = s.getCreatureName(id);
					mapped.remove(e.getBlock());
					Nest.getBlock(e.getBlock()).remove("t.s.v");
					e.setCancelled(true);
					String title = C.RED + "Overclocked " + C.YELLOW + name + " " + C.WHITE + "Spawner";
					String lore = C.RED + "Boost: " + C.YELLOW + F.pc(v);
					ItemStack is = s.newSpawnerItem(id, name);
					ItemMeta im = is.getItemMeta();
					im.setDisplayName(title);
					im.setLore(new GList<String>().qadd(lore));
					is.setItemMeta(im);
					e.getBlock().setType(Material.AIR);
					
					if(e.getPlayer().getGameMode().equals(GameMode.SURVIVAL) && s.isValidItemAndHasSilkTouch(e.getPlayer().getItemInHand()))
					{
						new TaskLater()
						{
							@Override
							public void run()
							{
								e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), is);
							}
						};
					}
				}
			}
			
			mapped.remove(e.getBlock());
			Nest.getBlock(e.getBlock()).remove("t.s.v");
		}
	}
	
	public void okm(PlayerInteractEvent e)
	{
		double rate = getSpeed(e.getClickedBlock());
		
		if(rate >= 0.0)
		{
			short id = s.getSpawnerEntityID(e.getClickedBlock());
			String name = s.getCreatureName(id);
			Window w = new PhantomWindow(C.DARK_RED + name + " Accelerator", e.getPlayer());
			
			for(int i = 0; i < levels; i++)
			{
				double speed = (i + 1) * interval;
				
				if(rate < speed)
				{
					Element element = new PhantomElement(new ItemStack(Material.MOB_SPAWNER), new Slot(0, 2))
					{
						@Override
						public void onClick(Player p, Click c, Window w)
						{
							setSpeed(e.getClickedBlock(), speed);
							okm(e);
							
							Audio a = new Audio();
							a.add(new GSound(Sound.CLICK, 1f, (float) ((speed / (interval * levels)) * 1.8)));
							a.play(p);
							p.sendMessage(C.RED + "Spawner is now " + F.pc(speed) + " faster");
						}
					};
					
					element.setTitle(C.GOLD + "+ " + F.pc(speed) + " overclock");
					w.addElement(element);
					break;
				}
			}
			
			w.setViewport(3);
			w.open();
		}
	}
	
	@EventHandler
	public void on(PlayerInteractEvent e)
	{
		try
		{
			if(e.getClickedBlock().getType().equals(Material.MOB_SPAWNER) && e.getAction().equals(Action.RIGHT_CLICK_BLOCK))
			{
				if(e.getPlayer().isSneaking())
				{
					return;
				}
				
				e.setCancelled(true);
				
				okm(e);
			}
		}
		
		catch(Exception ex)
		{
			
		}
	}
}
