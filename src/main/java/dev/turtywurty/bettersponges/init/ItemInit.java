package dev.turtywurty.bettersponges.init;

import dev.turtywurty.bettersponges.BetterSponges;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ItemInit {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS,
        BetterSponges.MODID);
    
    public static final RegistryObject<BlockItem> DAMP_SPONGE = ITEMS.register("damp_sponge",
        () -> new BlockItem(BlockInit.DAMP_SPONGE.get(),
            new Item.Properties().tab(CreativeModeTab.TAB_BUILDING_BLOCKS)));

    public static final RegistryObject<BlockItem> POTION_SPONGE = ITEMS.register("potion_sponge",
        () -> new BlockItem(BlockInit.POTION_SPONGE.get(),
            new Item.Properties().tab(CreativeModeTab.TAB_BUILDING_BLOCKS)));

    public static final RegistryObject<BlockItem> LAVA_SPONGE = ITEMS.register("lava_sponge",
        () -> new BlockItem(BlockInit.LAVA_SPONGE.get(),
            new Item.Properties().tab(CreativeModeTab.TAB_BUILDING_BLOCKS)));
    
    public static final RegistryObject<BlockItem> BURNT_SPONGE = ITEMS.register("burnt_sponge",
        () -> new BlockItem(BlockInit.BURNT_SPONGE.get(),
            new Item.Properties().tab(CreativeModeTab.TAB_BUILDING_BLOCKS)));
    
    private ItemInit() {
        throw new IllegalStateException("Hey thats illegal!");
    }
}
