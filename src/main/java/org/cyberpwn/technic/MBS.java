package org.cyberpwn.technic;

import org.phantomapi.clust.DataCluster;
import org.phantomapi.multiblock.MB;
import org.phantomapi.multiblock.Multiblock;
import org.phantomapi.nest.Nest;
import org.phantomapi.nest.NestScrub;
import org.phantomapi.nest.NestedBlock;
import org.phantomapi.nest.NestedChunk;
import org.phantomapi.util.D;

public class MBS implements NestScrub
{
	@Override
	public void onScan(NestedBlock b)
	{
		NestedChunk nc = Nest.getChunk(b.getLocation().toLocation().getChunk());
		
		try
		{
			DataCluster cc = nc.crop("mb");
			
			for(String i : cc.keys())
			{
				int id = Integer.valueOf(i.split("-")[1]);
				
				Multiblock mb = MB.getInstance(id);
				
				if(!(mb != null && mb.getChunks().contains(nc.getChunk().toChunk())))
				{
					nc.remove("mb." + i);
					new D("Multiblock Scrubber").f("Removing Invalid Instance " + id);
				}
			}
		}
		
		catch(Exception e)
		{
			
		}
	}
	
	@Override
	public void onScan(NestedChunk c)
	{
		
	}
}
