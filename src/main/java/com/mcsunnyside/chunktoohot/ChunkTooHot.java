package com.mcsunnyside.chunktoohot;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.TimeUnit;

public final class ChunkTooHot extends JavaPlugin implements Listener {
    private Cache<Chunk, Integer> chunkHotMap;
    private int limit = Integer.MAX_VALUE;

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        chunkHotMap = CacheBuilder.newBuilder().expireAfterWrite(getConfig().getLong("hotexpire"), TimeUnit.MILLISECONDS).build();
        limit = getConfig().getInt("hotlimit");
        Bukkit.getPluginManager().registerEvents(this,this);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onMobSpawning(CreatureSpawnEvent event){
        if(chunkHotMap == null) {return;}
        if(event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.NATURAL){
            return;
        }
        Chunk chunk = event.getEntity().getLocation().getChunk();
        Integer chunkAmount = chunkHotMap.getIfPresent(chunk);
        if(chunkAmount == null){
            chunkAmount = 0;
        }
        if(chunkAmount > limit){
            event.setCancelled(true);
            return;
        }
        chunkAmount ++;
        chunkHotMap.put(chunk,chunkAmount);
    }
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onMobDeath(EntityDeathEvent event){
        if(chunkHotMap == null) {return;}
        Chunk chunk = event.getEntity().getLocation().getChunk();
        Integer chunkAmount = chunkHotMap.getIfPresent(chunk);
        if(chunkAmount == null){
            chunkAmount = 0;
        }
        if(chunkAmount > limit){
            event.getDrops().clear();
            event.setDroppedExp(0);
            return;
        }
        chunkAmount ++;
        chunkHotMap.put(chunk,chunkAmount);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        chunkHotMap = null;
    }
}
