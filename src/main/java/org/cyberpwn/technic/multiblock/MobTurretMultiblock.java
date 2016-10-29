package org.cyberpwn.technic.multiblock;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;
import org.cyberpwn.technic.MultiblockHost;
import org.cyberpwn.technic.ShockEffect;
import org.phantomapi.Phantom;
import org.phantomapi.async.A;
import org.phantomapi.clust.Comment;
import org.phantomapi.clust.ConfigurableController;
import org.phantomapi.clust.Keyed;
import org.phantomapi.construct.Controllable;
import org.phantomapi.construct.Ticked;
import org.phantomapi.lang.GSound;
import org.phantomapi.multiblock.Multiblock;
import org.phantomapi.multiblock.MultiblockStructure;
import org.phantomapi.physics.VectorMath;
import org.phantomapi.sync.TaskLater;
import org.phantomapi.util.T;
import org.phantomapi.vfx.ParticleEffect;
import org.phantomapi.world.Area;
import org.phantomapi.world.MaterialBlock;

@Ticked(0)
public class MobTurretMultiblock extends ConfigurableController implements MultiblockHost
{
	private MultiblockStructure structure;
	private int delay;
	private double demand;
	
	@Comment("The interval in ticks")
	@Keyed("interval")
	public int interval = 60;
	
	@Comment("The Range of the structure")
	@Keyed("range")
	public double range = 16.1;
	
	@Comment("Should this structure be enabled?")
	@Keyed("enabled")
	public boolean enabled = true;
	
	public MobTurretMultiblock(Controllable parentController)
	{
		super(parentController, "mob-turret");
		
		loadCluster(this, "multiblock");
		
		if(!enabled)
		{
			return;
		}
		
		demand = 0;
		delay = interval;
		structure = new MultiblockStructure("mob-turret");
		
		structure.add(1, 0, 0, new MaterialBlock(Material.EMERALD_BLOCK));
		structure.add(-1, 0, 0, new MaterialBlock(Material.EMERALD_BLOCK));
		structure.add(0, 0, 1, new MaterialBlock(Material.EMERALD_BLOCK));
		structure.add(0, 0, -1, new MaterialBlock(Material.EMERALD_BLOCK));
		
		structure.add(-1, 0, -1, new MaterialBlock(Material.SEA_LANTERN));
		structure.add(1, 0, 1, new MaterialBlock(Material.SEA_LANTERN));
		structure.add(-1, 0, 1, new MaterialBlock(Material.SEA_LANTERN));
		structure.add(1, 0, -1, new MaterialBlock(Material.SEA_LANTERN));
		
		structure.add(0, 0, 0, new MaterialBlock(Material.PRISMARINE, (byte) 2));
		structure.add(0, 1, 0, new MaterialBlock(Material.PRISMARINE, (byte) 2));
		structure.add(0, 2, 0, new MaterialBlock(Material.BEACON));
		
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
			delay = interval;
			
			T t = new T()
			{
				@Override
				public void onStop(long nsTime, double msTime)
				{
					demand = msTime;
				}
			};
			
			for(Multiblock i : Phantom.instance().getMultiblockRegistryController().getMultiblocks("mob-turret"))
			{
				new A()
				{
					@Override
					public void async()
					{
						Location l = i.getMapping().get(new Vector(0, 2, 0)).clone().add(0.5, 0.5, 0.5);
						Area a = new Area(l, range);
						ParticleEffect.ENCHANTMENT_TABLE.display(4.3f, 64, l, 32);
						
						for(Entity j : a.getNearbyEntities())
						{
							if(j instanceof LivingEntity && !j.getType().equals(EntityType.PLAYER))
							{
								new TaskLater((int) (5 * Math.random()))
								{
									@Override
									public void run()
									{
										try
										{
											LivingEntity e = (LivingEntity) j;
											e.damage(5.5);
											Vector push = VectorMath.direction(l, e.getLocation());
											e.setVelocity(push.clone().multiply(4));
											new GSound(Sound.AMBIENCE_THUNDER, 4f, 1.9f).play(l);
											new ShockEffect(0.5f).play(l, push);
										}
										
										catch(Exception e)
										{
											
										}
									}
								};
							}
						}
					}
				};
			}
			
			t.stop();
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
