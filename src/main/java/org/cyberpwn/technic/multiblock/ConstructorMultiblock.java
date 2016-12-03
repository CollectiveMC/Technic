package org.cyberpwn.technic.multiblock;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.util.Vector;
import org.cyberpwn.technic.MultiblockHost;
import org.cyberpwn.technic.ST;
import org.phantomapi.Phantom;
import org.phantomapi.clust.Comment;
import org.phantomapi.clust.ConfigurableController;
import org.phantomapi.clust.Keyed;
import org.phantomapi.construct.Controllable;
import org.phantomapi.construct.Ticked;
import org.phantomapi.lang.GList;
import org.phantomapi.multiblock.Multiblock;
import org.phantomapi.multiblock.MultiblockStructure;
import org.phantomapi.nest.Nest;
import org.phantomapi.util.T;
import org.phantomapi.world.MaterialBlock;
import org.phantomapi.world.W;

@Ticked(0)
public class ConstructorMultiblock extends ConfigurableController implements MultiblockHost
{
	private MultiblockStructure structure;
	private int delay;
	private double demand;
	
	@Keyed("deny-recipies")
	public GList<String> deny = new GList<String>().qadd("HOPPER");
	
	@Comment("The interval in ticks")
	@Keyed("interval")
	public int interval = 60;
	
	@Comment("Should this structure be enabled?")
	@Keyed("enabled")
	public boolean enabled = true;
	
	public ConstructorMultiblock(Controllable parentController)
	{
		super(parentController, "constructor");
		
		loadCluster(this, "multiblock");
		
		if(!enabled)
		{
			return;
		}
		
		demand = 0;
		delay = interval;
		structure = new MultiblockStructure("constructor");
		
		ST.makePlateXZ(new Vector(0, 0, 0), structure, 1, new MaterialBlock(Material.GOLD_BLOCK));
		ST.makePlateXZ(new Vector(0, 1, 0), structure, 1, ST.wildcard(Material.STAINED_GLASS_PANE, 16));
		ST.makePlateXZ(new Vector(0, 2, 0), structure, 1, ST.wildcard(Material.STAINED_GLASS_PANE, 16));
		ST.makePlateXZ(new Vector(0, 3, 0), structure, 1, new MaterialBlock(Material.GOLD_BLOCK));
		
		ST.clear(new Vector(0, 0, 0), structure);
		ST.clear(new Vector(0, 1, 0), structure);
		ST.clear(new Vector(0, 2, 0), structure);
		ST.clear(new Vector(0, 3, 0), structure);
		
		structure.add(0, 0, 0, new MaterialBlock(Material.HOPPER));
		structure.add(new Vector(0, 1, 0), new MaterialBlock(Material.WORKBENCH));
		structure.add(new Vector(0, 2, 0), new MaterialBlock(Material.WORKBENCH));
		structure.add(0, 3, 0, ST.wildcard(Material.CHEST, 6));
		
		structure.register();
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
	
	@Override
	public void onStart()
	{
		
	}
	
	@Override
	public void onStop()
	{
		
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
			delay = interval;
			
			T t = new T()
			{
				@Override
				public void onStop(long nsTime, double msTime)
				{
					demand = msTime;
				}
			};
			
			for(Multiblock i : Phantom.instance().getMultiblockRegistryController().getMultiblocks("constructor"))
			{
				if(hasTarget(i))
				{
					
				}
			}
			
			t.stop();
		}
	}
	
	public boolean hasTarget(Multiblock m)
	{
		return getTarget(m) != null;
	}
	
	public void setTarget(Multiblock m, MaterialBlock mb)
	{
		Nest.getBlock(m.getMapping().get(new Vector(0, 0, 0)).getBlock()).set("const.target", mb.toString());
	}
	
	public MaterialBlock getTarget(Multiblock m)
	{
		String targ = Nest.getBlock(m.getMapping().get(new Vector(0, 0, 0)).getBlock()).getString("const.target");
		
		if(targ != null)
		{
			return W.getMaterialBlock(targ);
		}
		
		return null;
	}
	
	@SuppressWarnings("deprecation")
	public ItemStack getItemFor(MaterialBlock mb)
	{
		return new ItemStack(mb.getMaterial(), 1, (short) 0, mb.getData());
	}
	
	public Recipe getRecipe(MaterialBlock mb)
	{
		ItemStack is = getItemFor(mb);
		
		if(isRecipe(mb))
		{
			GList<Recipe> r = new GList<Recipe>(Bukkit.getServer().getRecipesFor(is));
			
			if(!r.isEmpty())
			{
				for(Recipe i : r)
				{
					if(i instanceof ShapedRecipe || i instanceof ShapelessRecipe)
					{
						return i;
					}
				}
			}
		}
		
		return null;
	}
	
	public boolean isRecipe(MaterialBlock mb)
	{
		ItemStack is = getItemFor(mb);
		GList<Recipe> r = new GList<Recipe>(Bukkit.getServer().getRecipesFor(is));
		
		for(String i : deny)
		{
			if(W.getMaterialBlock(i).equals(mb))
			{
				return false;
			}
		}
		
		if(!r.isEmpty())
		{
			for(Recipe i : r)
			{
				if(i instanceof ShapedRecipe || i instanceof ShapelessRecipe)
				{
					return true;
				}
			}
		}
		
		return false;
	}
}
