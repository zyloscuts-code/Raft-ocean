
package com.example.raftocean;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Barrel;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public class RaftOceanPlugin extends JavaPlugin implements Listener {

    private final Random random = new Random();
    private World world;

    @Override
    public void onEnable() {
        world = Bukkit.getWorlds().get(0);
        Bukkit.getPluginManager().registerEvents(this, this);

        createRaft();

        Bukkit.getScheduler().runTaskTimer(
                this,
                this::generateBarrels,
                20L * 60 * 60 * 24,
                20L * 60 * 60 * 24
        );

        getLogger().info("Raft Ocean World enabled!");
    }

    private void createRaft() {
        Location spawn = world.getSpawnLocation();
        int x = spawn.getBlockX();
        int z = spawn.getBlockZ();
        int y = 100;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                world.getBlockAt(x + dx, y, z + dz)
                        .setType(Material.OAK_PLANKS);
            }
        }

        world.setSpawnLocation(x, y + 1, z);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (!player.hasPlayedBefore()) {
            Location spawn = world.getSpawnLocation();
            player.teleport(spawn);

            player.getInventory().addItem(
                    new ItemStack(Material.FISHING_ROD),
                    new ItemStack(Material.OAK_SAPLING)
            );

            generateBarrels();
        }
    }

    private void generateBarrels() {
        Location spawn = world.getSpawnLocation();
        int count = 4 + random.nextInt(4);

        for (int i = 0; i < count; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            int distance = 18 + random.nextInt(5);

            int x = spawn.getBlockX()
                    + (int) (Math.cos(angle) * distance);
            int z = spawn.getBlockZ()
                    + (int) (Math.sin(angle) * distance);
            int y = 100;

            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    world.getBlockAt(x + dx, y - 1, z + dz)
                            .setType(Material.OAK_PLANKS);
                }
            }

            world.getBlockAt(x, y, z).setType(Material.BARREL);

            Barrel barrel = (Barrel) world.getBlockAt(x, y, z)
                    .getState();

            addLoot(barrel);
            barrel.update();
        }
    }

    private void addLoot(Barrel barrel) {
        Material[] loot = {
                Material.COAL,
                Material.STRING,
                Material.OAK_LOG,
                Material.DIRT,
                Material.WHEAT_SEEDS,
                Material.STICK,
                Material.OAK_SAPLING,
                Material.IRON_NUGGET,
                Material.DIAMOND
        };

        for (int i = 0; i < 4; i++) {
            Material item = loot[random.nextInt(loot.length)];
            int amount = item == Material.DIAMOND ? 1 :
                    1 + random.nextInt(5);

            barrel.getInventory().addItem(
                    new ItemStack(item, amount)
            );
        }
    }
}
