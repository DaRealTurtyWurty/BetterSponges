package dev.turtywurty.bettersponges.events;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import org.jetbrains.annotations.Nullable;

import dev.turtywurty.bettersponges.BetterSponges;
import dev.turtywurty.bettersponges.block.entity.PotionSpongeBlockEntity;
import dev.turtywurty.bettersponges.init.BlockInit;
import dev.turtywurty.bettersponges.init.ItemInit;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.items.ItemHandlerHelper;

@Mod.EventBusSubscriber(modid = BetterSponges.MODID, bus = Bus.FORGE)
public class CommonForgeEvents {
    @SubscribeEvent
    public static void bottleToSponge(RightClickBlock event) {
        if (event.getWorld().isClientSide())
            return;

        if (event.getItemStack().getItem() == Items.GLASS_BOTTLE) {
            final BlockState state = event.getWorld().getBlockState(event.getPos());
            final Block block = state.getBlock();
            final Player player = event.getPlayer();

            if (block == Blocks.WET_SPONGE) {
                final ItemStack result = filledPotion(player, Potions.WATER);
                giveItemToPlayer(event, player, result);
                event.getWorld().setBlock(event.getPos(), BlockInit.DAMP_SPONGE.get().defaultBlockState(), 2);
            } else if (block == BlockInit.DAMP_SPONGE.get()) {
                final ItemStack result = filledPotion(player, Potions.WATER);
                giveItemToPlayer(event, player, result);
                event.getWorld().setBlock(event.getPos(), Blocks.SPONGE.defaultBlockState(), 2);
            } else if (block == BlockInit.POTION_SPONGE.get()) {
                final BlockEntity be = event.getWorld().getBlockEntity(event.getPos());
                if (be instanceof final PotionSpongeBlockEntity potionSponge) {
                    final List<MobEffectInstance> effects = potionSponge.getEffects();
                    final ItemStack potion = PotionUtils.setCustomEffects(Items.POTION.getDefaultInstance(), effects);
                    giveItemToPlayer(event, player, potion);
                    event.getWorld().setBlock(event.getPos(), Blocks.SPONGE.defaultBlockState(), 2);
                }
            }
        }
    }
    
    @SubscribeEvent
    public static void fuelBurnTime(FurnaceFuelBurnTimeEvent event) {
        if (event.getItemStack().getItem() == ItemInit.BURNT_SPONGE.get()) {
            event.setBurnTime(20000);
        }
    }

    @SubscribeEvent
    public static void onLightningStrike(EntityStruckByLightningEvent event) {
        if (event.getEntity() instanceof Guardian) {
            event.getEntity().remove(RemovalReason.DISCARDED);
            final var elderGuardian = new ElderGuardian(EntityType.ELDER_GUARDIAN, event.getEntity().getLevel());
            elderGuardian.setPos(event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ());
            event.getEntity().getLevel().addFreshEntity(elderGuardian);
        }
    }
    
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        final Entity entity = event.getEntityLiving();
        if (entity instanceof final Guardian guardian) {
            final Entity attacker = event.getSource().getDirectEntity();
            if (!(attacker instanceof final Axolotl axolotl))
                return;
            
            final Player player = getPlayer(axolotl);
            if (player == null)
                return;
            
            final int level = EnchantmentHelper.getMobLooting(player);
            
            final int percentage = level > 0 ? level * 3 : 3;
            int count = percentage / 100;
            final int leftover = percentage % 100;
            if (ThreadLocalRandom.current().nextInt(100) < leftover) {
                count++;
            }
            
            final var stack = new ItemStack(Items.WET_SPONGE, count);
            final var item = new ItemEntity(guardian.getLevel(), guardian.getX(), guardian.getY(), guardian.getZ(),
                stack);
            guardian.getLevel().addFreshEntity(item);
        }
    }
    
    private static ItemStack filledPotion(Player player, Potion potion) {
        return ItemUtils.createFilledResult(Items.POTION.getDefaultInstance(), player,
            PotionUtils.setPotion(new ItemStack(Items.POTION), potion));
    }

    @Nullable
    private static Player getPlayer(Axolotl axolotl) {
        final Optional<LivingEntity> attackTarget = axolotl.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET);
        if (!attackTarget.isPresent())
            return null;
        
        final Level level = axolotl.getLevel();
        final LivingEntity livingEntity = attackTarget.get();
        final DamageSource damageSource = livingEntity.getLastDamageSource();
        if (damageSource == null)
            return null;
        
        final Entity entity = damageSource.getEntity();
        if (entity != null) {
            final List<Player> nearbyPlayers = level.getEntitiesOfClass(Player.class,
                axolotl.getBoundingBox().inflate(20.0D));
            return nearbyPlayers.isEmpty() ? null : nearbyPlayers.get(0);
        }
        
        return null;
    }
    
    private static void giveItemToPlayer(RightClickBlock event, Player player, ItemStack item) {
        ItemHandlerHelper.giveItemToPlayer(player, item, player.getInventory().selected);
        player.awardStat(Stats.ITEM_USED.get(item.getItem()));
        event.setCancellationResult(InteractionResult.CONSUME);
    }
}
