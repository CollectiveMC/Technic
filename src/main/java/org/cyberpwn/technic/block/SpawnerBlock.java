package org.cyberpwn.technic.block;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;
import org.phantomapi.clust.Comment;
import org.phantomapi.clust.ConfigurableController;
import org.phantomapi.clust.DataCluster;
import org.phantomapi.clust.Keyed;
import org.phantomapi.construct.Controllable;
import org.phantomapi.construct.ControllerMessage;
import org.phantomapi.construct.Ticked;
import org.phantomapi.core.SyncStart;
import org.phantomapi.currency.Transaction;
import org.phantomapi.currency.VaultCurrency;
import org.phantomapi.event.NestChunkLoadEvent;
import org.phantomapi.event.NestChunkUnloadEvent;
import org.phantomapi.gui.Click;
import org.phantomapi.gui.Element;
import org.phantomapi.gui.PhantomElement;
import org.phantomapi.gui.PhantomWindow;
import org.phantomapi.gui.Slot;
import org.phantomapi.gui.Window;
import org.phantomapi.lang.GList;
import org.phantomapi.lang.GLocation;
import org.phantomapi.lang.GMap;
import org.phantomapi.lang.GSound;
import org.phantomapi.lang.GTime;
import org.phantomapi.nest.Nest;
import org.phantomapi.sfx.Audio;
import org.phantomapi.sync.TaskLater;
import org.phantomapi.util.C;
import org.phantomapi.util.F;
import org.phantomapi.util.M;
import org.phantomapi.vfx.ParticleEffect;
import org.phantomapi.world.Cuboid;
import org.phantomapi.world.MaterialBlock;
import org.phantomapi.world.W;
import de.dustplanet.util.SilkUtil;

@Ticked(20)
@SyncStart
public class SpawnerBlock extends ConfigurableController
{
	@Comment("Max levels")
	@Keyed("spawner.levels")
	public int levels = 5;
	
	@Comment("Grace period after place to not be charged for mining")
	@Keyed("spawner.grace-seconds")
	public int graceSeconds = 120;
	
	@Comment("Level interval")
	@Keyed("spawner.interval")
	public double interval = 0.5;
	
	@Comment("Base spawner delay")
	@Keyed("spawner.base")
	public int delayx = 400;
	
	@Comment("Spawner price node for what shop")
	@Keyed("spawner.price-node")
	public String node = "shop";
	
	@Comment("This will be treaded as if this is in the shop")
	@Keyed("spawner.override-price")
	public GList<String> spawnerHacks = new GList<String>().qadd("MOB_SPAWNER:99;5000");
	
	@Comment("The Multiplier for the upgrade price")
	@Keyed("spawner.default-mult")
	public double defaultMultiplier = 8000;
	
	@Comment("Enable or disable spawner leveling")
	@Keyed("enabled")
	public boolean enabled = true;
	
	@Comment("if set to true, you will have to pay money to mine spawners")
	@Keyed("charge-on-mine")
	public boolean chargeMines = true;
	
	private SilkUtil s;
	private GList<Block> mapped;
	private GMap<Block, Integer> delay;
	private GMap<Block, Location> lastRealPosition;
	
	public SpawnerBlock(Controllable parentController)
	{
		super(parentController, "spawner");
		
		s = SilkUtil.hookIntoSilkSpanwers();
		mapped = new GList<Block>();
		delay = new GMap<Block, Integer>();
		lastRealPosition = new GMap<Block, Location>();
	}
	
	@Override
	public void onStart()
	{
		loadCluster(this, "block");
	}
	
	@Override
	public void onStop()
	{
		
	}
	
	@Override
	public void onTick()
	{
		for(Block i : delay.k())
		{
			delay.put(i, delay.get(i) - 20);
			
			if(delay.get(i) <= 0)
			{
				delay.remove(i);
			}
		}
		
		for(Block i : mapped.copy())
		{
			if(Nest.getChunk(i.getChunk()) == null)
			{
				mapped.remove(i);
				continue;
			}
			
			if(Nest.getBlock(i) == null)
			{
				mapped.remove(i);
				continue;
			}
			
			if(Nest.getBlock(i).contains("t.s.v") && i.getType().equals(Material.MOB_SPAWNER))
			{
				CreatureSpawner cs = (CreatureSpawner) i.getState();
				
				try
				{
					ParticleEffect.FLAME.display((float) getSpeed(i) / 35, (int) (8 + getSpeed(i)), i.getLocation().add(0.5, 0.5, 0.5), 12);
					
					if(!delay.containsKey(i))
					{
						double speed = getSpeed(i) + 1.0;
						
						if(speed > 1)
						{
							if(lastRealPosition.containsKey(i))
							{
								delay.put(i, (int) ((double) delayx / speed));
								EntityType et = cs.getSpawnedType();
								Location l = lastRealPosition.get(i);
								Entity e = i.getWorld().spawn(l, et.getEntityClass());
								SpawnerSpawnEvent sse = new SpawnerSpawnEvent(e, cs);
								callEvent(sse);
								ParticleEffect.FLAME.display((float) getSpeed(i) / 35, (int) (28 + getSpeed(i)), i.getLocation().add(0.5, 0.5, 0.5), 12);
								ParticleEffect.FLAME.display((float) getSpeed(i) / 35, (int) (28 + getSpeed(i)), l, 12);
							}
						}
					}
				}
				
				catch(Exception e)
				{
					
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
	
	public GTime getGrace(Block block)
	{
		if(!isGraced(block))
		{
			return null;
		}
		
		if(block.getType().equals(Material.MOB_SPAWNER) && Nest.getBlock(block).contains("t.s.m"))
		{
			long es = 1000 * graceSeconds;
			return new GTime(Nest.getBlock(block).getLong("t.s.m") + es - M.ms());
		}
		
		return null;
	}
	
	public boolean isGraced(Block block)
	{
		if(block.getType().equals(Material.MOB_SPAWNER) && Nest.getBlock(block).contains("t.s.m"))
		{
			long es = 1000 * graceSeconds;
			
			if(Nest.getBlock(block).getLong("t.s.m") + es - M.ms() < 0)
			{
				return false;
			}
			
			return Nest.getBlock(block).getLong("t.s.m") + es > M.ms();
		}
		
		return false;
	}
	
	@EventHandler
	public void on(SpawnerSpawnEvent e)
	{
		try
		{
			lastRealPosition.put(e.getSpawner().getBlock(), e.getLocation());
			
			if(!mapped.contains(e.getSpawner().getBlock()))
			{
				mapped.add(e.getSpawner().getBlock());
			}
		}
		
		catch(Exception ex)
		{
			
		}
	}
	
	public double getValue(Block block)
	{
		Double result = null;
		
		try
		{
			MaterialBlock mb = new MaterialBlock(Material.MOB_SPAWNER, (byte) s.getSpawnerEntityID(block));
			
			for(String i : spawnerHacks)
			{
				if(W.getMaterialBlock(i.split(";")[0]).getData() == mb.getData())
				{
					return Integer.valueOf(i.split(";")[1]);
				}
			}
			
			ControllerMessage message = new ControllerMessage(this);
			message.set("value", mb.getMaterial() + ":" + mb.getData());
			message.set("shop", node);
			
			ControllerMessage response = sendMessage("CurrencyShops", message);
			
			if(response.contains("result"))
			{
				result = response.getDouble("result");
			}
		}
		
		catch(Exception e)
		{
			
		}
		
		if(result == null)
		{
			f("Failed to find node in shop");
			result = defaultMultiplier;
		}
		
		return result;
	}
	
	@EventHandler
	public void on(NestChunkLoadEvent e)
	{
		for(GLocation i : e.getNestedChunk().getBlocks().k())
		{
			if(i.toLocation().getBlock().getType().equals(Material.MOB_SPAWNER))
			{
				if(!mapped.contains(i.toLocation().getBlock()))
				{
					mapped.add(i.toLocation().getBlock());
				}
			}
		}
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
			Nest.getBlock(e.getBlock()).set("t.s.m", M.ms());
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
							Double vv = (double) v / 100.0;
							Nest.getBlock(e.getBlock()).set("t.s.v", vv);
							Nest.getBlock(e.getBlock()).set("t.s.m", M.ms());
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
			if(chargeMines && Nest.getBlock(e.getBlock()).contains("t.s.v") && !isGraced(e.getBlock()))
			{
				boolean b = false;
				int xp = (int) new VaultCurrency().get(e.getPlayer());
				int cost = (int) (0.25 * getPrice(e.getBlock()));
				
				if(xp >= cost)
				{
					ItemStack is = e.getPlayer().getItemInHand();
					
					if(is != null && is.getEnchantments().containsKey(Enchantment.SILK_TOUCH))
					{
						new Transaction(new VaultCurrency()).from(e.getPlayer()).amount((double) cost).commit();
						b = true;
						e.getPlayer().sendMessage(C.RED + "Mined for " + F.f(cost) + "$");
					}
					
					else
					{
						return;
					}
				}
				
				else
				{
					DataCluster dc = Nest.getBlock(e.getBlock()).copy();
					e.getPlayer().sendMessage(C.RED + "You need " + F.f(cost) + "$ to mine this.");
					e.setCancelled(true);
					
					new TaskLater()
					{
						@Override
						public void run()
						{
							Nest.getBlock(e.getBlock()).setData(dc.getData());
							Nest.getBlock(e.getBlock()).set("s", true);
						}
					};
					
					return;
				}
				
				if(!b)
				{
					e.setCancelled(true);
					return;
				}
			}
			
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
								Item ent = e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), is);
								ent.teleport(e.getBlock().getLocation().add(0.5, 0, 0.5));
								ent.setVelocity(new Vector(0, 0, 0));
							}
						};
					}
				}
				
				else
				{
					short id = s.getSpawnerEntityID(e.getBlock());
					String name = s.getCreatureName(id);
					mapped.remove(e.getBlock());
					Nest.getBlock(e.getBlock()).remove("t.s.v");
					e.setCancelled(true);
					String title = C.YELLOW + name + " " + C.WHITE + "Spawner";
					ItemStack is = s.newSpawnerItem(id, name);
					ItemMeta im = is.getItemMeta();
					im.setDisplayName(title);
					is.setItemMeta(im);
					e.getBlock().setType(Material.AIR);
					
					if(e.getPlayer().getGameMode().equals(GameMode.SURVIVAL) && s.isValidItemAndHasSilkTouch(e.getPlayer().getItemInHand()))
					{
						new TaskLater()
						{
							@Override
							public void run()
							{
								Item ent = e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), is);
								ent.teleport(e.getBlock().getLocation().add(0.5, 0, 0.5));
								ent.setVelocity(new Vector(0, 0, 0));
							}
						};
					}
				}
			}
			
			mapped.remove(e.getBlock());
			Nest.getBlock(e.getBlock()).remove("t.s.v");
			Nest.getBlock(e.getBlock()).remove("t.s.m");
		}
	}
	
	public void okm(PlayerInteractEvent e)
	{
		if(Nest.getBlock(e.getClickedBlock()).contains("s") && !Nest.getBlock(e.getClickedBlock()).contains("t.s.v"))
		{
			Nest.getBlock(e.getClickedBlock()).set("t.s.v", 0.0);
			Nest.getBlock(e.getClickedBlock()).set("t.s.m", 0.0);
		}
		
		double rate = getSpeed(e.getClickedBlock());
		
		if(rate >= 0.0)
		{
			if(!mapped.contains(e.getClickedBlock()))
			{
				mapped.add(e.getClickedBlock());
			}
			
			Window w = new PhantomWindow(C.DARK_RED + "Accelerator" + C.DARK_GRAY + " (" + F.pc(rate + 1.0) + ")", e.getPlayer());
			
			for(int i = 0; i < levels; i++)
			{
				double speed = (i + 1) * interval;
				
				if(rate < speed)
				{
					int cost = (int) (getValue(e.getClickedBlock()) * speed);
					int xp = (int) new VaultCurrency().get(e.getPlayer());
					
					Element element = new PhantomElement(new ItemStack(Material.MOB_SPAWNER), new Slot(0, 2))
					{
						@Override
						public void onClick(Player p, Click c, Window w)
						{
							if(xp >= cost)
							{
								new Transaction(new VaultCurrency()).from(p).amount((double) cost).commit();
								setSpeed(e.getClickedBlock(), speed);
								Audio a = new Audio();
								a.add(new GSound(Sound.CLICK, 1f, (float) (speed / (interval * levels) * 1.8)));
								a.play(p);
								p.sendMessage(C.RED + "Spawner is now " + F.pc(speed) + " faster");
								w.close();
							}
							
							else
							{
								Audio a = new Audio();
								a.add(new GSound(Sound.WOOD_CLICK, 1f, (float) (speed / (interval * levels) * 1.8)));
								a.play(p);
							}
						}
					};
					
					element.setTitle(C.GOLD + "+ " + F.pc(speed) + " overclock");
					element.addText(C.GOLD + "This means this spawner will spawn " + (1.0 + speed) + "x speed");
					element.addText((xp >= cost ? C.GREEN : C.RED) + "Costs: $" + F.f(cost));
					
					if(xp < cost)
					{
						element.addText(C.RED + "Cannot afford");
					}
					
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
			if(e.getClickedBlock() == null)
			{
				return;
			}
			
			if(e.getClickedBlock().getType().equals(Material.MOB_SPAWNER) && e.getAction().equals(Action.RIGHT_CLICK_BLOCK))
			{
				if(e.getPlayer().isSneaking())
				{
					return;
				}
				
				e.setCancelled(true);
				
				okm(e);
			}
			
			else if(e.getClickedBlock().getType().equals(Material.MOB_SPAWNER) && e.getAction().equals(Action.LEFT_CLICK_BLOCK))
			{
				if(Nest.getBlock(e.getClickedBlock()).contains("s") && Nest.getBlock(e.getClickedBlock()).contains("t.s.v"))
				{
					if(Nest.getBlock(e.getClickedBlock()).contains("t.s.m"))
					{
						if(isGraced(e.getClickedBlock()))
						{
							e.getPlayer().sendMessage(C.GREEN + "Free to mine for " + getGrace(e.getClickedBlock()).to());
							return;
						}
					}
					
					int cost = (int) (0.25 * getPrice(e.getClickedBlock()));
					e.getPlayer().sendMessage(C.RED + "You will spend $" + F.f(cost) + " if you mine this.");
				}
			}
		}
		
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void on(EntityExplodeEvent e)
	{
		Cuboid c = new Cuboid(e.getEntity().getLocation().add(3, 3, 3), e.getEntity().getLocation().add(-3, -3, -3));
		
		new TaskLater(2)
		{
			@Override
			public void run()
			{
				for(Block i : new GList<Block>(c.iterator()))
				{
					Location l = i.getLocation();
					
					if(l.getBlock().getType().equals(Material.MOB_SPAWNER) && M.r(0.80))
					{
						double speed = getSpeed(l.getBlock());
						
						if(speed == 0)
						{
							mapped.remove(l.getBlock());
							Nest.getBlock(l.getBlock()).remove("t.s.v");
							Nest.getBlock(l.getBlock()).remove("t.s.m");
							short k = s.getSpawnerEntityID(l.getBlock());
							l.getBlock().setType(Material.AIR);
							
							new TaskLater(3)
							{
								@Override
								public void run()
								{
									l.getWorld().dropItem(l, s.newSpawnerItem(k, C.YELLOW.toString() + s.getCreatureName(k) + C.WHITE.toString() + " Spawner"));
								}
							};
						}
						
						else
						{
							short id = s.getSpawnerEntityID(l.getBlock());
							String name = s.getCreatureName(id);
							e.setCancelled(true);
							String title = C.RED + "Overclocked " + C.YELLOW + name + " " + C.WHITE + "Spawner";
							String lore = C.RED + "Boost: " + C.YELLOW + F.pc(speed);
							ItemStack is = s.newSpawnerItem(id, name);
							ItemMeta im = is.getItemMeta();
							im.setDisplayName(title);
							im.setLore(new GList<String>().qadd(lore));
							is.setItemMeta(im);
							mapped.remove(l.getBlock());
							Nest.getBlock(l.getBlock()).remove("t.s.v");
							Nest.getBlock(l.getBlock()).remove("t.s.m");
							l.getBlock().setType(Material.AIR);
							
							new TaskLater(3)
							{
								@Override
								public void run()
								{
									l.getWorld().dropItem(l, is);
								}
							};
						}
					}
				}
			}
		};
	}
	
	public int getPrice(Block b)
	{
		Double value = 0.0;
		Block bx = new Location(Bukkit.getWorld(b.getWorld().getName()), b.getX(), b.getY(), b.getZ()).getBlock();
		MaterialBlock mb = new MaterialBlock(Material.MOB_SPAWNER, (byte) s.getSpawnerEntityID(bx));
		
		for(String i : spawnerHacks)
		{
			if(W.getMaterialBlock(i.split(";")[0]).getData() == mb.getData())
			{
				return Integer.valueOf(i.split(";")[1]);
			}
		}
		
		try
		{
			ControllerMessage message = new ControllerMessage(this);
			message.set("value", mb.getMaterial() + ":" + mb.getData());
			message.set("shop", node);
			
			ControllerMessage response = sendMessage("CurrencyShops", message);
			
			if(response.contains("result"))
			{
				value = response.getDouble("result");
			}
		}
		
		catch(Exception e)
		{
			
		}
		
		return value.intValue();
	}
}
