package com.example.examplemod;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraft.commands.Commands;

import java.util.Random;

@Mod(VillageGeneratorMod.MODID)
public class VillageGeneratorMod {
    public static final String MODID = "examplemod";
    private final Random random = new Random();

    public VillageGeneratorMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::setup);
        
        // Register this class to the common/server event bus for ServerStartingEvent
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLClientSetupEvent event) {}

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        event.getServer().getCommands().getDispatcher().register(
                Commands.literal("generate_village")
                        .then(Commands.argument("size", IntegerArgumentType.integer(10, 50))
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    ServerLevel world = player.serverLevel();
                                    int size = IntegerArgumentType.getInteger(context, "size");
                                    generateVillage(world, player.blockPosition(), size);
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
        );
    }

    private void generateVillage(ServerLevel world, BlockPos center, int size) {
        int roadWidth = 2;
        int blockSize = 5; // 各ブロックの大きさ（家・畑など）

        for (int x = -size; x <= size; x += blockSize) {
            for (int z = -size; z <= size; z += blockSize) {
                BlockPos pos = center.offset(x, 0, z);
                double choice = random.nextDouble();

                if (choice < 0.4) {
                    generateHouse(world, pos); // 家を生成
                } else if (choice < 0.6) {
                    generateFarm(world, pos);  // 畑を生成
                } else {
                    generatePath(world, pos, roadWidth); // 道を生成
                }
            }
        }
    }

    private void generatePath(ServerLevel world, BlockPos pos, int width) {
        for (int x = -width; x <= width; x++) {
            for (int z = -width; z <= width; z++) {
                BlockPos pathPos = pos.offset(x, 0, z);
                world.setBlock(pathPos, Blocks.DIRT_PATH.defaultBlockState(), 3);
            }
        }
    }

    private void generateHouse(ServerLevel world, BlockPos pos) {
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 3; x++) {
                for (int z = 0; z < 3; z++) {
                    if (x == 0 || x == 2 || z == 0 || z == 2 || y == 3) {
                        world.setBlock(pos.offset(x, y, z), Blocks.OAK_PLANKS.defaultBlockState(), 3);
                    } else {
                        world.setBlock(pos.offset(x, y, z), Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        }
    }

    private void generateFarm(ServerLevel world, BlockPos pos) {
        for (int x = 0; x < 4; x++) {
            for (int z = 0; z < 4; z++) {
                world.setBlock(pos.offset(x, 0, z), Blocks.FARMLAND.defaultBlockState(), 3);
                if (random.nextDouble() > 0.3) {
                    world.setBlock(pos.offset(x, 1, z), Blocks.WHEAT.defaultBlockState(), 3);
                }
            }
        }
    }
}
