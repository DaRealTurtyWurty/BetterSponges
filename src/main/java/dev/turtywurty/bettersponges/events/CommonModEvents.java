package dev.turtywurty.bettersponges.events;

import dev.turtywurty.bettersponges.BetterSponges;
import dev.turtywurty.bettersponges.dispenser.BlockDispenseItemBehaviour;
import dev.turtywurty.bettersponges.dispenser.GlassBottleDispenserBehavior;
import dev.turtywurty.bettersponges.dispenser.ShearsDispenserBehavior;
import dev.turtywurty.bettersponges.init.BlockInit;
import dev.turtywurty.bettersponges.init.ItemInit;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(modid = BetterSponges.MODID, bus = Bus.MOD)
public class CommonModEvents {
    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(CommonModEvents::registerDispensers);
    }

    private static DispenseItemBehavior getVanilla(Item item) {
        return DispenserBlock.DISPENSER_REGISTRY.get(item);
    }

    private static void registerDispensers() {
        DispenserBlock.registerBehavior(Blocks.SPONGE,
            new BlockDispenseItemBehaviour(getVanilla(Items.SPONGE), Blocks.SPONGE.defaultBlockState(),
                BlockDispenseItemBehaviour.EMPTY_BLOCK.or(BlockDispenseItemBehaviour.isFluid(Fluids.WATER))));
        
        DispenserBlock.registerBehavior(Blocks.WET_SPONGE,
            new BlockDispenseItemBehaviour(getVanilla(Items.WET_SPONGE), Blocks.WET_SPONGE.defaultBlockState(),
                BlockDispenseItemBehaviour.EMPTY_BLOCK.or(BlockDispenseItemBehaviour.isFluid(Fluids.WATER))));
        
        DispenserBlock.registerBehavior(BlockInit.DAMP_SPONGE.get(),
            new BlockDispenseItemBehaviour(getVanilla(ItemInit.DAMP_SPONGE.get()),
                BlockInit.DAMP_SPONGE.get().defaultBlockState(),
                BlockDispenseItemBehaviour.EMPTY_BLOCK.or(BlockDispenseItemBehaviour.isFluid(Fluids.WATER))));
        
        DispenserBlock.registerBehavior(BlockInit.LAVA_SPONGE.get(),
            new BlockDispenseItemBehaviour(getVanilla(ItemInit.LAVA_SPONGE.get()),
                BlockInit.LAVA_SPONGE.get().defaultBlockState(),
                BlockDispenseItemBehaviour.EMPTY_BLOCK.or(BlockDispenseItemBehaviour.isFluid(Fluids.LAVA))));
        
        DispenserBlock.registerBehavior(Items.GLASS_BOTTLE,
            new GlassBottleDispenserBehavior(getVanilla(Items.GLASS_BOTTLE)));
        DispenserBlock.registerBehavior(Items.SHEARS, new ShearsDispenserBehavior(getVanilla(Items.SHEARS)));
    }
}
