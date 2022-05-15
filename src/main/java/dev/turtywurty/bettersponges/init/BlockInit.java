package dev.turtywurty.bettersponges.init;

import dev.turtywurty.bettersponges.BetterSponges;
import dev.turtywurty.bettersponges.block.DampSpongeBlock;
import dev.turtywurty.bettersponges.block.LavaSpongeBlock;
import dev.turtywurty.bettersponges.block.PotionSpongeBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class BlockInit {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS,
        BetterSponges.MODID);
    
    public static final RegistryObject<DampSpongeBlock> DAMP_SPONGE = BLOCKS.register("damp_sponge",
        () -> new DampSpongeBlock(BlockBehaviour.Properties.copy(Blocks.WET_SPONGE)));
    
    public static final RegistryObject<PotionSpongeBlock> POTION_SPONGE = BLOCKS.register("potion_sponge",
        () -> new PotionSpongeBlock(BlockBehaviour.Properties.copy(Blocks.WET_SPONGE)));
    
    public static final RegistryObject<LavaSpongeBlock> LAVA_SPONGE = BLOCKS.register("lava_sponge",
        () -> new LavaSpongeBlock(BlockBehaviour.Properties.copy(Blocks.SPONGE)));

    public static final RegistryObject<Block> BURNT_SPONGE = BLOCKS.register("burnt_sponge",
        () -> new Block(BlockBehaviour.Properties.copy(Blocks.COAL_BLOCK)));
    
    private BlockInit() {
        throw new IllegalStateException("Hey thats illegal!");
    }
}
