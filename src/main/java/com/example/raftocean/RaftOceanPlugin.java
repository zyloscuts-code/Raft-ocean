package com.example.raftocean;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.block.Barrel;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class RaftOceanPlugin extends JavaPlugin implements Listener {
    private static final String WORLD_NAME = "RaftOcean";
    private static final int SEABED_Y = 30;
    private static final int WATER_SURFACE_Y = 62;
    private static final int RAFT_Y = 63;

    private final Random random = new Random();
    private World ocean;
    private NamespacedKey starterKitKey;
    private long lastSupplyDay;

    @Override
    public void onEnable() {
        starterKitKey = new NamespacedKey(this, "starter_kit_received");
        ocean = Bukkit.getWorld(WORLD_NAME);
        if (ocean == null) {
            WorldCreator creator = new WorldCreator(WORLD_NAME);
            creator.environment(World.Environment.NORMAL);
            creator.type(WorldType.NORMAL);
            ocean = creator.createWorld();
        }
        if (ocean == null) {
            getLogger().severe("Could not create RaftOcean world.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        ocean.setSpawnLocation(0, RAFT_Y + 1, 0);
        getServer().getPluginManager().registerEvents(this, this);
        ocean.getChunkAt(0, 0);
        convertChunk(ocean.getChunkAt(0, 0));
        buildRaft();

        long currentDay = ocean.getFullTime() / 24000L;
        lastSupplyDay = getConfig().getLong("last-supply-day", -1L);
        if (lastSupplyDay < currentDay) {
            spawnDailySupplies(4 + random.nextInt(4));
            saveSupplyDay(currentDay);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                long day = ocean.getFullTime() / 24000L;
                if (day > lastSupplyDay) {
                    spawnDailySupplies(4 + random.nextInt(4));
                    saveSupplyDay(day);
                }
            }
        }.runTaskTimer(this, 40L, 1200L);

        getLogger().info("RaftOcean enabled. Players spawn on the 3x3 raft in the RaftOcean world.");
    }

    private void saveSupplyDay(long day) {
        lastSupplyDay = day;
        getConfig().set("last-supply-day", day);
        saveConfig();
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        if (!event.getWorld().equals(ocean) || !event.isNewChunk()) return;

        convertChunk(event.getChunk());
        if (event.getChunk().getX() == 0 && event.getChunk().getZ() == 0) buildRaft();
    }

    private void convertChunk(org.bukkit.Chunk chunk) {
        int max = ocean.getMaxHeight();
        // Keep vanilla-generated underground terrain, caves and ores below Y=30.
        // Flatten everything above it into a deep, continuous ocean so no land pokes
        // through the water surface, regardless of the original biome or terrain height.
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = SEABED_Y + 1; y < max; y++) {
                    chunk.getBlock(x, y, z).setType(Material.AIR, false);
                }
                chunk.getBlock(x, SEABED_Y, z).setType(Material.GRAVEL, false);
                for (int y = SEABED_Y + 1; y <= WATER_SURFACE_Y; y++) {
                    chunk.getBlock(x, y, z).setType(Material.WATER, false);
                }
            }
        }
    }

    private void buildRaft() {
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                ocean.getBlockAt(x, RAFT_Y, z).setType(Material.OAK_PLANKS, false);
            }
        }
        ocean.getBlockAt(0, RAFT_Y + 1, 0).setType(Material.AIR, false);
        ocean.setSpawnLocation(0, RAFT_Y + 1, 0);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.getWorld().equals(ocean)) {
            player.teleport(ocean.getSpawnLocation());
        }
        if (!player.getPersistentDataContainer().has(starterKitKey, PersistentDataType.BYTE)) {
            player.getInventory().addItem(new ItemStack(Material.FISHING_ROD, 1));
            player.getInventory().addItem(new ItemStack(Material.OAK_SAPLING, 1));
            player.getPersistentDataContainer().set(starterKitKey, PersistentDataType.BYTE, (byte) 1);
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        event.setRespawnLocation(ocean.getSpawnLocation());
    }

    private void spawnDailySupplies(int count) {
        for (int i = 0; i < count; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            int radius = 20 + random.nextInt(31);
            int x = (int) Math.round(Math.cos(angle) * radius);
            int z = (int) Math.round(Math.sin(angle) * radius);
            ocean.getChunkAt(x >> 4, z >> 4);

            if (ocean.getBlockAt(x, WATER_SURFACE_Y, z).getType() != Material.WATER) {
                boolean found = false;
                for (int r = 1; r <= 8 && !found; r++) {
                    for (int dx = -r; dx <= r && !found; dx++) {
                        for (int dz = -r; dz <= r && !found; dz++) {
                            if (ocean.getBlockAt(x + dx, WATER_SURFACE_Y, z + dz).getType() == Material.WATER
                                    && ocean.getBlockAt(x + dx, RAFT_Y, z + dz).getType() == Material.AIR) {
                                x += dx;
                                z += dz;
                                found = true;
                            }
                        }
                    }
                }
                if (!found) continue;
            }

            Block spot = ocean.getBlockAt(x, RAFT_Y, z);
            if (spot.getType() != Material.AIR) continue;
            spot.setType(Material.BARREL, false);
            if (spot.getState() instanceof Barrel barrel) {
                barrel.setCustomName("Ocean Supply Crate");
                fillBarrel(barrel);
                barrel.update(true, false);
            }

            if (random.nextBoolean()) {
                Item loose = ocean.dropItem(new Location(ocean, x + 0.5, RAFT_Y + 0.3, z + 0.5), randomLooseResource());
                loose.setVelocity(new Vector((random.nextDouble() - 0.5) * 0.06, 0.015,
                        (random.nextDouble() - 0.5) * 0.06));
                loose.setPickupDelay(20);
            }
        }
        getLogger().info("Spawned " + count + " ocean supply barrels for Minecraft day "
                + (ocean.getFullTime() / 24000L) + ".");
    }

    private void fillBarrel(Barrel barrel) {
        List<ItemStack> pool = new ArrayList<>();
        add(pool, Material.OAK_LOG, 1, 4);
        add(pool, Material.OAK_PLANKS, 2, 8);
        add(pool, Material.STICK, 2, 8);
        add(pool, Material.STRING, 1, 4);
        add(pool, Material.COAL, 1, 5);
        add(pool, Material.DIRT, 1, 8);
        add(pool, Material.WHEAT_SEEDS, 1, 4);
        add(pool, Material.BEETROOT_SEEDS, 1, 3);
        add(pool, Material.OAK_SAPLING, 1, 2);
        add(pool, Material.SPRUCE_SAPLING, 1, 1);
        add(pool, Material.IRON_NUGGET, 1, 4);
        add(pool, Material.IRON_INGOT, 1, 2);
        add(pool, Material.BONE, 1, 3);
        add(pool, Material.FEATHER, 1, 4);

        int slots = 3 + random.nextInt(4);
        for (int i = 0; i < slots; i++) {
            barrel.getInventory().addItem(pool.get(random.nextInt(pool.size())).clone());
        }
        if (random.nextInt(100) < 12) barrel.getInventory().addItem(new ItemStack(Material.IRON_INGOT, 1));
        if (random.nextInt(1000) < 8) barrel.getInventory().addItem(new ItemStack(Material.DIAMOND, 1));
    }

    private void add(List<ItemStack> pool, Material material, int min, int max) {
        pool.add(new ItemStack(material, min + random.nextInt(max - min + 1)));
    }

    private ItemStack randomLooseResource() {
        Material[] items = {
                Material.OAK_LOG, Material.OAK_PLANKS, Material.STICK, Material.STRING,
                Material.COAL, Material.DIRT, Material.WHEAT_SEEDS, Material.OAK_SAPLING,
                Material.IRON_NUGGET
        };
        return new ItemStack(items[random.nextInt(items.length)], 1 + random.nextInt(3));
    }
}
