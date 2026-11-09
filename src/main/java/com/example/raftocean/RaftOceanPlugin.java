
package com.example.raftocean;

import org.bukkit.*;
import org.bukkit.block.Barrel;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class RaftOceanPlugin extends JavaPlugin implements Listener {

    private World raftWorld;
    private final Random random = new Random();

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);

        raftWorld = Bukkit.getWorld("RaftOcean");

        if (raftWorld == null) {
            raftWorld = new WorldCreator("RaftOcean")
                    .environment(World.Environment.NORMAL)
                    .generator(new OceanGenerator())
                    .createWorld();

            if (raftWorld == null) {
                getLogger().severe("Could not create RaftOcean!");
                return;
            }

            createRaft();
            generateBarrels();
        }

        raftWorld.setSpawnLocation(0, 65, 0);

        getLogger().info("RaftOcean World is ready!");
    }

    private void createRaft() {
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                raftWorld.getBlockAt(x, 64, z)
                        .setType(Material.OAK_PLANKS);
            }
        }

        raftWorld.getBlockAt(0, 65, 0)
                .setType(Material.AIR);
    }

    private void generateBarrels() {
        int count = 7;

        for (int i = 0; i < count; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            int distance = 20 + random.nextInt(16);

            int x = (int) (Math.cos(angle) * distance);
            int z = (int) (Math.sin(angle) * distance);

            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    raftWorld.getBlockAt(x + dx, 64, z + dz)
                            .setType(Material.OAK_PLANKS);
                }
            }

            raftWorld.getBlockAt(x, 65, z)
                    .setType(Material.BARREL);

            Barrel barrel = (Barrel) raftWorld
                    .getBlockAt(x, 65, z).getState();

            Material[] loot = {
                    Material.OAK_LOG,
                    Material.COAL,
                    Material.IRON_INGOT,
                    Material.STRING,
                    Material.WHEAT_SEEDS,
                    Material.BREAD,
                    Material.DIAMOND,
                    Material.GOLD_INGOT
            };

            for (int j = 0; j < 5; j++) {
                Material item = loot[random.nextInt(loot.length)];
                barrel.getInventory().addItem(
                        new ItemStack(item, 1 + random.nextInt(3))
                );
            }

            barrel.update();
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        Bukkit.getScheduler().runTaskLater(this, () -> {
            player.teleport(raftWorld.getSpawnLocation());

            if (!player.hasPlayedBefore()) {
                player.getInventory().addItem(
                        new ItemStack(Material.FISHING_ROD),
                        new ItemStack(Material.OAK_SAPLING),
                        new ItemStack(Material.BREAD, 3)
                );
            }
        }, 20L);
    }

    public static class OceanGenerator extends ChunkGenerator {

        @Override
        public ChunkData generateChunkData(
                World world,
                Random random,
                int chunkX,
                int chunkZ,
                BiomeGrid biome
        ) {
            ChunkData data = createChunkData(world);

            data.setRegion(
                    0, 0, 0,
                    16, 1, 16,
                    Material.STONE
            );

            data.setRegion(
                    0, 1, 0,
                    16, 64, 16,
                    Material.WATER
            );

            return data;
        }
    }
}
