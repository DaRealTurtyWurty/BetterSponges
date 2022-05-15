package dev.turtywurty.bettersponges;

import dev.turtywurty.bettersponges.init.BlockEntityInit;
import dev.turtywurty.bettersponges.init.BlockInit;
import dev.turtywurty.bettersponges.init.ItemInit;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(BetterSponges.MODID)
public class BetterSponges {
    public static final String MODID = "bettersponges";

    public BetterSponges() {
        final IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        BlockInit.BLOCKS.register(bus);
        ItemInit.ITEMS.register(bus);
        BlockEntityInit.BLOCK_ENTITIES.register(bus);
    }
}
