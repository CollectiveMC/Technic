package org.cyberpwn.technic;

import java.util.Set;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;
import org.cyberpwn.technic.multiblock.AlarmMultiblock;
import org.cyberpwn.technic.multiblock.MobTurretMultiblock;
import org.cyberpwn.technic.multiblock.SmelteryMultiblock;
import org.phantomapi.Phantom;
import org.phantomapi.command.Command;
import org.phantomapi.command.CommandFilter;
import org.phantomapi.command.PhantomCommand;
import org.phantomapi.command.PhantomSender;
import org.phantomapi.construct.Controllable;
import org.phantomapi.construct.Controller;
import org.phantomapi.event.MultiblockConstructEvent;
import org.phantomapi.event.MultiblockDestroyEvent;
import org.phantomapi.lang.GSound;
import org.phantomapi.multiblock.Multiblock;
import org.phantomapi.nest.Nest;
import org.phantomapi.nms.NMSX;
import org.phantomapi.statistics.Monitorable;
import org.phantomapi.sync.TaskLater;
import org.phantomapi.util.C;
import org.phantomapi.util.F;
import org.phantomapi.world.Blocks;

public class MultiblockController extends Controller implements Monitorable
{
	private MBS mbs;
	private AlarmMultiblock alarmMultiblock;
	private MobTurretMultiblock mobTurretMultiblock;
	private SmelteryMultiblock smelteryMultiblock;
	
	public MultiblockController(Controllable parentController)
	{
		super(parentController);
		
		mbs = new MBS();
		alarmMultiblock = new AlarmMultiblock(this);
		mobTurretMultiblock = new MobTurretMultiblock(this);
		smelteryMultiblock = new SmelteryMultiblock(this);
		
		register(alarmMultiblock);
		register(mobTurretMultiblock);
		register(smelteryMultiblock);
	}
	
	@Override
	public void onStart()
	{
		Phantom.instance().getNestController().registerScrub(mbs);
	}
	
	@Override
	public void onStop()
	{
		Phantom.instance().getNestController().unregisterScrub(mbs);
	}
	
	@CommandFilter.PlayerOnly()
	@Command("mbh")
	public void onHallucinate(PhantomSender sender, PhantomCommand cmd)
	{
		if(cmd.getArgs().length == 1)
		{
			for(Controllable i : getControllers())
			{
				if(i instanceof MultiblockHost)
				{
					MultiblockHost h = (MultiblockHost) i;
					
					if(h.getStructure().getType().equalsIgnoreCase(cmd.getArgs()[0]))
					{
						Block b = sender.getPlayer().getTargetBlock((Set<Material>) null, 48);
						h.getStructure().hallucinate(b.getLocation(), sender.getPlayer());
					}
				}
			}
		}
	}
	
	@EventHandler
	public void on(MultiblockConstructEvent e)
	{
		try
		{
			if(!Blocks.canModify(e.getPlayer(), e.getBlock()))
			{
				return;
			}
		}
		
		catch(Exception ex)
		{
			
		}
		
		created(e.getMultiblock());
		
		for(Chunk i : e.getMultiblock().getChunks())
		{
			Phantom.instance().getNestController().scrub(Nest.getChunk(i));
		}
	}
	
	@EventHandler
	public void on(MultiblockDestroyEvent e)
	{
		try
		{
			if(!Blocks.canModify(e.getPlayer(), e.getBlock()))
			{
				return;
			}
		}
		
		catch(Exception ex)
		{
			
		}
		
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
	}
	
	@Override
	public String getMonitorableData()
	{
		double ms = 0;
		
		for(Controllable i : getControllers())
		{
			if(i instanceof MultiblockHost)
			{
				MultiblockHost h = (MultiblockHost) i;
				ms += h.getDemand();
			}
		}
		
		return C.LIGHT_PURPLE.toString() + F.f(ms, 4) + " ms";
	}
}
