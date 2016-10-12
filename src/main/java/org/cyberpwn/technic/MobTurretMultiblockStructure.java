package org.cyberpwn.technic;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;
import org.phantomapi.Phantom;
import org.phantomapi.construct.Controllable;
import org.phantomapi.construct.Controller;
import org.phantomapi.construct.Ticked;
import org.phantomapi.lang.GSound;
import org.phantomapi.multiblock.Multiblock;
import org.phantomapi.multiblock.MultiblockStructure;
import org.phantomapi.physics.VectorMath;
import org.phantomapi.vfx.ParticleEffect;
import org.phantomapi.world.Area;
import org.phantomapi.world.MaterialBlock;

@Ticked(60)
public class MobTurretMultiblockStructure extends Controller
{
	private MultiblockStructure structure;
	
	public MobTurretMultiblockStructure(Controllable parentController)
	{
		super(parentController);
		
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
		for(Multiblock i : Phantom.instance().getMultiblockRegistryController().getMultiblocks("mob-turret"))
		{
			Location l = i.getMapping().get(new Vector(0, 2, 0)).clone().add(0.5, 0.5, 0.5);
			Area a = new Area(l, 16);
			ParticleEffect.ENCHANTMENT_TABLE.display(4.3f, 64, l, 32);
			
			for(Entity j : a.getNearbyEntities())
			{
				if(j instanceof LivingEntity && !j.getType().equals(EntityType.PLAYER))
				{
					LivingEntity e = (LivingEntity) j;
					e.damage(5.5);
					Vector push = VectorMath.direction(l, e.getLocation());
					e.setVelocity(push.clone().multiply(4));
					new GSound(Sound.AMBIENCE_THUNDER, 4f, 1.9f).play(l);
					new ShockEffect(5.5f).play(l, push);
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
