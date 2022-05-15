package dev.turtywurty.bettersponges.events;

import java.util.ArrayList;
import java.util.List;

import dev.turtywurty.bettersponges.BetterSponges;
import dev.turtywurty.bettersponges.block.entity.PotionSpongeBlockEntity;
import dev.turtywurty.bettersponges.init.BlockInit;
import dev.turtywurty.bettersponges.init.ItemInit;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(modid = BetterSponges.MODID, bus = Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {
    @SubscribeEvent
    public static void blockColors(ColorHandlerEvent.Block event) {
        event.getBlockColors().register((pState, pLevel, pPos, pTintIndex) -> {
            final BlockEntity be = pLevel.getBlockEntity(pPos);
            if (be instanceof final PotionSpongeBlockEntity potionSponge)
                return potionSponge.getColor();
            
            return 0xFFFFFF;
        }, BlockInit.POTION_SPONGE.get());
    }
    
    @SubscribeEvent
    public static void itemColors(ColorHandlerEvent.Item event) {
        event.getItemColors().register((pStack, pTintIndex) -> {
            final CompoundTag nbt = pStack.getOrCreateTagElement(BetterSponges.MODID);
            if (!nbt.contains("Potion") || !nbt.contains("Effects"))
                return 0xFFFFFF;

            final List<MobEffectInstance> effects = new ArrayList<>();
            final ListTag effectsNbt = nbt.getList("Effects", Tag.TAG_COMPOUND);
            for (final Tag tag : effectsNbt) {
                effects.add(MobEffectInstance.load((CompoundTag) tag));
            }

            return PotionSpongeBlockEntity.getColor(effects);
        }, ItemInit.POTION_SPONGE.get());
    }
}
