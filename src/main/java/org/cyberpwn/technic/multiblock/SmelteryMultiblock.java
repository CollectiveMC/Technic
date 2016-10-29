package org.cyberpwn.technic.multiblock;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.util.Vector;
import org.cyberpwn.technic.MultiblockHost;
import org.cyberpwn.technic.ST;
import org.phantomapi.async.A;
import org.phantomapi.clust.Comment;
import org.phantomapi.clust.ConfigurableController;
import org.phantomapi.clust.Keyed;
import org.phantomapi.construct.Controllable;
import org.phantomapi.construct.Ticked;
import org.phantomapi.inventory.PhantomInventory;
import org.phantomapi.lang.GList;
import org.phantomapi.lang.GMap;
import org.phantomapi.lang.GSound;
import org.phantomapi.multiblock.MB;
import org.phantomapi.multiblock.Multiblock;
import org.phantomapi.multiblock.MultiblockStructure;
import org.phantomapi.sync.S;
import org.phantomapi.util.T;
import org.phantomapi.vfx.ParticleEffect;
import org.phantomapi.world.MaterialBlock;
import net.minecraft.server.v1_8_R3.RecipesFurnace;
import net.minecraft.server.v1_8_R3.TileEntityFurnace;

@Ticked(0)
public class SmelteryMultiblock extends ConfigurableController implements MultiblockHost
{
	private MultiblockStructure structure;
	private int delay;
	private double demand;
	
	@Comment("Should this structure be enabled?")
	@Keyed("enable")
	public boolean enabled = true;
	
	@Comment("The interval in ticks to screech")
	@Keyed("options.interval")
	public int interval = 15;
	
	public SmelteryMultiblock(Controllable parentController)
	{
		super(parentController, "smeltery");
		
		loadCluster(this, "multiblock");
		
		if(!enabled)
		{
			return;
		}
		
		delay = interval;
		demand = 0;
		structure = new MultiblockStructure("smeltery");
		ST.makePlateXZ(new Vector(0, 3, 0), structure, 2, new MaterialBlock(Material.IRON_BLOCK));
		ST.makePlateXZ(new Vector(0, 0, 0), structure, 2, new MaterialBlock(Material.IRON_BLOCK));
		ST.makePlateXZ(new Vector(0, 1, 0), structure, 2, ST.wildcard(Material.STAINED_GLASS_PANE, 16));
		ST.clearPlateXZ(new Vector(0, 1, 0), structure, 1);
		ST.makePlateXZ(new Vector(0, 2, 0), structure, 2, ST.wildcard(Material.STAINED_GLASS_PANE, 16));
		ST.clearPlateXZ(new Vector(0, 2, 0), structure, 1);
		ST.makePlateXZ(new Vector(0, 1, 0), structure, 1, ST.wildcard(Material.FURNACE, 6));
		ST.clear(new Vector(0, 1, 0), structure);
		
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
			T t = new T()
			{
				@Override
				public void onStop(long nsTime, double msTime)
				{
					demand = msTime;
				}
			};
			
			for(Multiblock i : MB.getInstances("smeltery"))
			{
				new A()
				{
					@Override
					public void async()
					{
						try
						{
							tryCycle(i);
						}
						
						catch(Exception e)
						{
							e.printStackTrace();
						}
					}
				};
			}
			
			t.stop();
			
			delay = interval;
		}
	}
	
	public void tryCycle(Multiblock mb)
	{
		if(hasOutputSpace(mb) && !getFuelInventories(mb).isEmpty() && !getCookableInventories(mb).isEmpty())
		{
			int needed = getMinimumRequiredItems(mb);
			int has = 0;
			
			if(needed > 0)
			{
				GMap<Block, Inventory> map = getCookableInventories(mb);
				GMap<Inventory, GList<ItemStack>> take = new GMap<Inventory, GList<ItemStack>>();
				
				for(Block i : map.k())
				{
					Inventory inv = map.get(i);
					
					for(ItemStack j : inv.getContents())
					{
						if(j != null && !j.getType().equals(Material.AIR) && isCookable(j))
						{
							if(!take.contains(inv))
							{
								take.put(inv, new GList<ItemStack>());
							}
							
							ItemStack is = j.clone();
							is.setAmount(1);
							
							for(int k = 0; k < j.getAmount(); k++)
							{
								take.get(inv).add(is);
							}
							
							has += j.getAmount();
						}
					}
				}
				
				if(has >= needed)
				{
					int taken = 0;
					
					while(taken < needed)
					{
						Inventory ix = take.k().pickRandom();
						ItemStack is = take.get(ix).pickRandom();
						ItemStack iv = is.clone();
						iv.setAmount(1);
						ItemStack result = getResult(iv);
						
						if(hasOutputSpace(mb))
						{
							ix.removeItem(iv);
							taken++;
							dispose(result, mb);
						}
						
						else
						{
							break;
						}
					}
					
					takeFuel(mb, needed);
					new GSound(Sound.BLAZE_BREATH, 2f, 0.121f).play(mb.getLocations().pickRandom());
					new GSound(Sound.FIRE_IGNITE, 2f, 0.121f).play(mb.getLocations().pickRandom());
					new GSound(Sound.HORSE_JUMP, 2f, 0.121f).play(mb.getLocations().pickRandom());
					playAnimation(mb);
				}
			}
		}
	}
	
	public void takeFuel(Multiblock mb, int taken)
	{
		new S()
		{
			@Override
			public void sync()
			{
				GMap<Block, Inventory> map = getFuelInventories(mb);
				
				for(Block i : map.k())
				{
					for(ItemStack j : map.get(i).getContents())
					{
						if(j != null && !j.getType().equals(Material.AIR))
						{
							if(isFuel(j))
							{
								int time = getFuelTime(j);
								
								if(time == taken * 10)
								{
									ItemStack c = j.clone();
									c.setAmount(1);
									map.get(i).removeItem(c);
									return;
								}
							}
						}
					}
				}
			}
		};
	}
	
	public int getMinimumRequiredItems(Multiblock mb)
	{
		GMap<Block, Inventory> map = getFuelInventories(mb);
		int lowest = Integer.MAX_VALUE;
		boolean found = false;
		
		for(Block i : map.k())
		{
			for(ItemStack j : map.get(i).getContents())
			{
				if(j != null && !j.getType().equals(Material.AIR))
				{
					if(isFuel(j))
					{
						int time = getFuelTime(j);
						
						if(time < lowest)
						{
							lowest = time;
							found = true;
						}
					}
				}
			}
		}
		
		if(found)
		{
			return lowest / 10;
		}
		
		return -1;
	}
	
	public void dispose(ItemStack is, Multiblock mb)
	{
		new S()
		{
			@Override
			public void sync()
			{
				GMap<Block, Inventory> map = getOutputInventories(mb);
				GList<Block> m = map.k().shuffleCopy();
				
				for(Block i : m)
				{
					if(new PhantomInventory(map.get(i)).hasSpace())
					{
						map.get(i).addItem(is);
						return;
					}
				}
			}
		};
	}
	
	public boolean hasOutputSpace(Multiblock mb)
	{
		GMap<Block, Inventory> map = getOutputInventories(mb);
		
		for(Block i : map.k())
		{
			if(new PhantomInventory(map.get(i)).hasSpace())
			{
				return true;
			}
		}
		
		return false;
	}
	
	public GMap<Block, Inventory> getFuelInventories(Multiblock mb)
	{
		GMap<Block, Inventory> map = getInventories(mb);
		
		for(Block i : map.k())
		{
			for(ItemStack j : map.get(i).getContents())
			{
				if(j != null && !j.getType().equals(Material.AIR))
				{
					if(isFuel(j))
					{
						map.put(i, map.get(i));
					}
				}
			}
		}
		
		return map;
	}
	
	public GMap<Block, Inventory> getCookableInventories(Multiblock mb)
	{
		GMap<Block, Inventory> map = getInventories(mb);
		
		for(Block i : map.k())
		{
			for(ItemStack j : map.get(i).getContents())
			{
				if(j != null && !j.getType().equals(Material.AIR))
				{
					if(isCookable(j))
					{
						map.put(i, map.get(i));
					}
				}
			}
		}
		
		return map;
	}
	
	public boolean isFuel(ItemStack item)
	{
		net.minecraft.server.v1_8_R3.ItemStack nmss = CraftItemStack.asNMSCopy(item);
		
		return TileEntityFurnace.isFuel(nmss);
	}
	
	public int getFuelTime(ItemStack item)
	{
		ItemStack ms = item.clone();
		ms.setAmount(1);
		net.minecraft.server.v1_8_R3.ItemStack nmss = CraftItemStack.asNMSCopy(ms);
		
		return TileEntityFurnace.fuelTime(nmss) / 20;
	}
	
	public boolean isCookable(ItemStack item)
	{
		net.minecraft.server.v1_8_R3.ItemStack nmss = CraftItemStack.asNMSCopy(item);
		
		return RecipesFurnace.getInstance().getResult(nmss) != null;
	}
	
	public void playAnimation(Multiblock mb)
	{
		GList<Location> gli = getAnimatingInternalLocations(mb);
		GList<Location> gle = getAnimatingExaustLocations(mb);
		
		for(int i = 0; i < 18; i++)
		{
			ParticleEffect.LAVA.display(0.1f, 4, gli.pickRandom(), 32.0);
			ParticleEffect.FLAME.display(0.1f, 2, gli.pickRandom(), 32.0);
			ParticleEffect.SMOKE_LARGE.display(0.1f, 1, gle.pickRandom(), 32.0);
			ParticleEffect.FLAME.display(0.1f, 2, gli.pickRandom(), 32.0);
		}
	}
	
	public GList<Location> getAnimatingInternalLocations(Multiblock mb)
	{
		GList<Location> gl = new GList<Location>();
		
		gl.add(mb.getMapping().get(new Vector(1, 1, 0)));
		gl.add(mb.getMapping().get(new Vector(-1, 1, 0)));
		gl.add(mb.getMapping().get(new Vector(0, 1, 1)));
		gl.add(mb.getMapping().get(new Vector(0, 1, -1)));
		gl.add(mb.getMapping().get(new Vector(1, 1, 1)));
		gl.add(mb.getMapping().get(new Vector(-1, 1, -1)));
		gl.add(mb.getMapping().get(new Vector(1, 1, 1)));
		gl.add(mb.getMapping().get(new Vector(-1, 1, -1)));
		
		for(Location i : gl)
		{
			i.add(0.5, 1.5, 0.5);
		}
		
		return gl;
	}
	
	public GList<Location> getAnimatingExaustLocations(Multiblock mb)
	{
		GList<Location> gl = new GList<Location>();
		
		gl.add(mb.getMapping().get(new Vector(0, 3, 0)));
		gl.add(mb.getMapping().get(new Vector(1, 3, 0)));
		gl.add(mb.getMapping().get(new Vector(-1, 3, 0)));
		gl.add(mb.getMapping().get(new Vector(0, 3, 1)));
		gl.add(mb.getMapping().get(new Vector(0, 3, -1)));
		gl.add(mb.getMapping().get(new Vector(1, 3, 1)));
		gl.add(mb.getMapping().get(new Vector(-1, 3, -1)));
		gl.add(mb.getMapping().get(new Vector(1, 3, 1)));
		gl.add(mb.getMapping().get(new Vector(-1, 3, -1)));
		
		for(Location i : gl)
		{
			i.add(0.5, 1.1, 0.5);
		}
		
		return gl;
	}
	
	public GMap<Block, Inventory> getInventories(Multiblock mb)
	{
		GMap<Block, Inventory> map = new GMap<Block, Inventory>();
		
		for(int i = -2; i < 3; i++)
		{
			for(int j = -2; j < 3; j++)
			{
				Block b = mb.getMapping().get(new Vector(i, 3, j)).clone().add(0, 1, 0).getBlock();
				
				if(b.getType().equals(Material.CHEST) || b.getType().equals(Material.TRAPPED_CHEST) || b.getType().equals(Material.HOPPER))
				{
					Inventory inv = ((InventoryHolder) b.getState()).getInventory();
					map.put(b, inv);
				}
			}
		}
		
		return map;
	}
	
	public GMap<Block, Inventory> getOutputInventories(Multiblock mb)
	{
		GMap<Block, Inventory> map = new GMap<Block, Inventory>();
		
		for(int i = -2; i < 3; i++)
		{
			for(int j = -2; j < 3; j++)
			{
				Block b = mb.getMapping().get(new Vector(i, 0, j)).clone().add(0, -1, 0).getBlock();
				
				if(b.getType().equals(Material.CHEST) || b.getType().equals(Material.TRAPPED_CHEST) || b.getType().equals(Material.HOPPER))
				{
					Inventory inv = ((InventoryHolder) b.getState()).getInventory();
					map.put(b, inv);
				}
			}
		}
		
		return map;
	}
	
	public ItemStack getResult(ItemStack item)
	{
		for(Recipe i : new GList<Recipe>(Bukkit.getServer().recipeIterator()))
		{
			if(i instanceof FurnaceRecipe)
			{
				FurnaceRecipe f = (FurnaceRecipe) i;
				
				if(f.getInput().getType().equals(item.getType()))
				{
					return f.getResult();
				}
			}
		}
		
		return null;
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
	public MultiblockStructure getStructure()
	{
		return structure;
	}
	
	@Override
	public double getDemand()
	{
		return demand;
	}
}
