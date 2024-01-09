package com.skycat.mystical.common.spell;

import com.mojang.serialization.Codec;
import com.skycat.mystical.Mystical;
import com.skycat.mystical.common.spell.consequence.ChangeArmorHurtConsequence;
import com.skycat.mystical.common.spell.consequence.ConsequenceFactory;
import com.skycat.mystical.common.spell.consequence.SpellConsequence;
import com.skycat.mystical.common.spell.cure.SpellCure;
import com.skycat.mystical.common.spell.cure.StatBackedSpellCure;
import com.skycat.mystical.common.util.Utils;
import com.skycat.mystical.event.CatEntityEvents;
import lombok.Getter;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stat;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Scanner;

import static com.skycat.mystical.Mystical.CONFIG;
import static com.skycat.mystical.Mystical.GSON;

public class SpellHandler implements EntitySleepEvents.StartSleeping,
        EntitySleepEvents.StopSleeping,
        PlayerBlockBreakEvents.Before,
        PlayerBlockBreakEvents.After,
        ServerPlayerEvents.AfterRespawn,
        ServerEntityCombatEvents.AfterKilledOtherEntity,
        AttackBlockCallback,
        CatEntityEvents.Eat,
        ServerEntityEvents.EquipmentChange
{
    /**
     * @implNote Saving/loading does not ensure that the order of spells will be retained.
     */
    // This saves the active spells by taking the spell codec, turning it into a list codec, then maps List<Spell> and SpellHandler
    public static final Codec<SpellHandler> CODEC = Spell.CODEC.listOf().xmap(spellList -> new SpellHandler(spellList), SpellHandler::getActiveSpells); // Using SpellHandler::new just feels wrong since there's multiple

    @Getter private static final File SAVE_FILE = new File("config/spellHandler.json");
    @Getter private final ArrayList<Spell> activeSpells;

    public SpellHandler() {
        activeSpells = new ArrayList<>();
    }

    public SpellHandler(ArrayList<Spell> activeSpells) {
        this.activeSpells = activeSpells;
    }

    public SpellHandler(List<Spell> activeSpells) {
        this.activeSpells = new ArrayList<>(activeSpells);
    }

    public static SpellHandler loadOrNew() {
        try (Scanner scanner = new Scanner(SAVE_FILE)) {
            return GSON.fromJson(scanner.nextLine(), SpellHandler.class);
        } catch (FileNotFoundException e) {
            Utils.log(Utils.translateString("text.mystical.logging.failedToLoadSpellHandler"), Mystical.CONFIG.failedToLoadSpellHandlerLogLevel());
            return new SpellHandler();
        }
    }

    public void decaySpells() {
        double amount = CONFIG.spellDecay() / 100;
        for (Spell spell : activeSpells) {
            SpellCure cure = spell.getCure();
            // If linear, base decay on the goal. Otherwise, base it on how much is left.
            cure.contribute(null, (int) Math.ceil((CONFIG.spellDecayLinear() ? cure.getContributionGoal() : cure.getContributionsLeft()) * amount));
        }
    }

    @Override
    public ActionResult interact(PlayerEntity player, World world, Hand hand, BlockPos pos, Direction direction) {
        boolean fail = false;
        for (Spell spell : spellsOfHandler(AttackBlockCallback.class)) {
            if (((AttackBlockCallback) spell.getConsequence()).interact(player, world, hand, pos, direction) == ActionResult.FAIL) {
                fail = true;
            }
        }
        return fail ? ActionResult.FAIL : ActionResult.PASS;
    }

    public boolean isConsequenceActive(Class<? extends SpellConsequence> consequence) {
        return !spellsOfConsequenceType(consequence).isEmpty();
    }


    /**
     * Used for finding active spells with a particular consequence type.
     * This is not the same as a handler.
     * @param clazz The class to check for
     * @return An ArrayList of matching spells
     * @see SpellHandler#spellsOfHandler
     */
    public ArrayList<Spell> spellsOfConsequenceType(Class<? extends SpellConsequence> clazz) {
        ArrayList<Spell> results = new ArrayList<>();
        for (Spell spell : activeSpells) {
            if (clazz.isAssignableFrom(spell.getConsequence().getClass())) {
                results.add(spell);
            }
        }
        return results;
    }

    public void activateNewSpell() {
        activeSpells.add(SpellGenerator.get());
        Mystical.saveUpdated();
    }

    public void activateNewSpellWithConsequence(ConsequenceFactory<?> consequenceFactory) {
        activeSpells.add(SpellGenerator.getWithConsequence(consequenceFactory));
        Mystical.saveUpdated();
    }

    @Override
    public void afterBlockBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        for (Spell spell : spellsOfHandler(PlayerBlockBreakEvents.After.class)) {
            ((PlayerBlockBreakEvents.After) spell.getConsequence()).afterBlockBreak(world, player, pos, state, blockEntity);
        }
    }

    @Override
    public void onEat(CatEntity cat, PlayerEntity player, Hand hand, ItemStack stack) {
        for (Spell spell : spellsOfHandler(CatEntityEvents.Eat.class)) {
            ((CatEntityEvents.Eat) spell.getConsequence()).onEat(cat, player, hand, stack);
        }
    }

    @Override
    public void afterKilledOtherEntity(ServerWorld world, Entity entity, LivingEntity killedEntity) {
        for (Spell spell : spellsOfHandler(ServerEntityCombatEvents.AfterKilledOtherEntity.class)) {
            ((ServerEntityCombatEvents.AfterKilledOtherEntity) spell.getConsequence()).afterKilledOtherEntity(world, entity, killedEntity);
        }
    }

    /**
     * Applies spells of type {@link ServerPlayerEvents.AfterRespawn}
     * @param oldPlayer the old player
     * @param newPlayer the new player
     * @param alive whether the old player is still alive
     */
    @Override
    public void afterRespawn(ServerPlayerEntity oldPlayer, ServerPlayerEntity newPlayer, boolean alive) {
        for (Spell spell : spellsOfHandler(ServerPlayerEvents.AfterRespawn.class)) {
            ((ServerPlayerEvents.AfterRespawn) spell.getConsequence()).afterRespawn(oldPlayer, newPlayer, alive);
        }
    }

    /**
     * Applies spells of type {@link PlayerBlockBreakEvents.Before}
     * @param world the world in which the block is broken
     * @param player the player breaking the block
     * @param pos the position at which the block is broken
     * @param state the block state <strong>before</strong> the block is broken
     * @param blockEntity the block entity <strong>before</strong> the block is broken, can be {@code null}
     * @return {@code false} to cancel the breaking, {@code true} to leave it alone.
     * @implNote All spells will be run, even if it is cancelled in one of them.
     */
    @Override
    public boolean beforeBlockBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        // TODO Make options for when there are collisions
        boolean returnValue = true;
        for (Spell spell : spellsOfHandler(PlayerBlockBreakEvents.Before.class)) {
            // Keep these in order. This way, the consequence triggers, even if returnValue is false. Otherwise, it gets short-circuited.
            returnValue = ((PlayerBlockBreakEvents.Before) spell.getConsequence()).beforeBlockBreak(world, player, pos, state, blockEntity) && returnValue  ;
        }
        return returnValue;
    }

    @Override
    public void onStartSleeping(LivingEntity entity, BlockPos sleepingPos) {
        for (Spell spell : spellsOfHandler(EntitySleepEvents.StartSleeping.class)) {
            ((EntitySleepEvents.StartSleeping) spell.getConsequence()).onStartSleeping(entity, sleepingPos);
        }
    }

    public <T> void onStatIncreased(PlayerEntity player, Stat<T> stat, int amount) {
        for (Spell spell : spellsOfStatCure(stat)) {
            spell.getCure().contribute(player.getUuid(), amount);
        }
    }

    @Override
    public void onStopSleeping(LivingEntity entity, BlockPos sleepingPos) {
        for (Spell spell : spellsOfHandler(EntitySleepEvents.StopSleeping.class)) {
            ((EntitySleepEvents.StopSleeping) spell.getConsequence()).onStopSleeping(entity, sleepingPos);
        }
    }


    public void removeAllSpells() {
        activeSpells.clear();
        Mystical.saveUpdated();
    }

    /**
     * Save the spellHandler to file. Deprecated in favor of {@link com.skycat.mystical.server.SaveState}
     */
    @Deprecated
    public void save() {
        try (PrintWriter pw = new PrintWriter(SAVE_FILE)) {
            pw.println(GSON.toJson(this));
        } catch (IOException e) {
            Utils.log(Utils.translateString("text.mystical.logging.failedToSaveSpellHandler"), Mystical.CONFIG.failedToSaveSpellHandlerLogLevel());
        }
    }

    /**
     * Get all active spells that are using a specified handler
     *
     * @param clazz The event handler to search for
     * @param <T>   The type of handlers to return
     * @return An ArrayList of spells that have matching consequences
     */
    public <T> ArrayList<Spell> spellsOfHandler(Class<T> clazz) {
        ArrayList<Spell> results = new ArrayList<>();
        for (Spell spell : activeSpells) {
            Class<?> callbackType = spell.getConsequence().getCallbackType();
            if (callbackType != null && callbackType.equals(clazz)) {
                results.add(spell);
            }
        }
        return results;
    }

    public <T> ArrayList<Spell> spellsOfStatCure(Stat<T> stat) {
        ArrayList<Spell> results = new ArrayList<>();
        for (Spell spell : activeSpells) {
            if (spell.getCure() instanceof StatBackedSpellCure backedSpellCure) {
                if (backedSpellCure.getStatType().equals(stat.getType()) && backedSpellCure.getStat().getValue().equals(stat.getValue())) {
                    results.add(spell);
                }
            }
        }
        return results;
    }

    /**
     * Removes all spells that have met their cure condition
     * @return The number of spells cured
     */
    public int removeCuredSpells() {
        ListIterator<Spell> li = activeSpells.listIterator();
        int removed = 0;
        while (li.hasNext()) {
            SpellCure cure = li.next().getCure();
            if (cure.isSatisfied()) {
                cure.awardPower(200, 100); // TODO: modify based on cure difficulty
                li.remove();
                removed ++;
            }
        }
        Mystical.saveUpdated();
        return removed;
    }


    @Override
    public void onChange(LivingEntity livingEntity, EquipmentSlot equipmentSlot, ItemStack previousStack, ItemStack currentStack) {
        for (Spell spell : spellsOfHandler(ServerEntityEvents.EquipmentChange.class)) {
            ((ServerEntityEvents.EquipmentChange) spell.getConsequence()).onChange(livingEntity, equipmentSlot, previousStack, currentStack);
        }
    }
}
