package dev.turtywurty.bettersponges.init;

import dev.turtywurty.bettersponges.BetterSponges;
import dev.turtywurty.bettersponges.block.entity.PotionSpongeBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class BlockEntityInit {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister
        .create(ForgeRegistries.BLOCK_ENTITIES, BetterSponges.MODID);
    
    public static final RegistryObject<BlockEntityType<PotionSpongeBlockEntity>> POTION_SPONGE = BLOCK_ENTITIES
        .register("potion_sponge",
            () -> BlockEntityType.Builder.of(PotionSpongeBlockEntity::new, BlockInit.POTION_SPONGE.get()).build(null));

    private BlockEntityInit() {
        throw new IllegalStateException("Hey thats illegal!");
    }
}
