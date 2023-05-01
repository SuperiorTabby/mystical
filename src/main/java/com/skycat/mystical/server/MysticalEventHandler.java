package com.skycat.mystical.server;

import com.skycat.mystical.Mystical;
import com.skycat.mystical.common.util.Utils;
import lombok.Getter;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;

public class MysticalEventHandler implements ServerLifecycleEvents.ServerStarted, ServerLifecycleEvents.ServerStopping {
    @Getter private MinecraftServer server;
    private MinecraftServerTimerAccess timerAccess;

    @Override
    public void onServerStarted(MinecraftServer server) {
        this.server = server;
        timerAccess = ((MinecraftServerTimerAccess) server);
        setNightTimer();
        Utils.log(Utils.translateString("text.mystical.eventHandler.timeOfDay", server.getOverworld().getTimeOfDay()), Mystical.CONFIG.timeOfDayAtStartupLogLevel());
    }

    public void doNighttimeEvents() {
        // TODO: Logging
        Mystical.SPELL_HANDLER.removeCuredSpells();
        if (Mystical.SPELL_HANDLER.getActiveSpells().size() < 2) { // TODO: Config
            Mystical.SPELL_HANDLER.activateNewSpell();
        }

        try {
            setNightTimer();
        } catch (NullPointerException e) {
            Utils.log(Utils.translateString("text.mystical.eventHandler.setNightTimerFailed", e.getMessage()), Mystical.CONFIG.failedToSetNightTimerLogLevel());
            // TODO Try again later?
            // WARN: If this fails after the timer expires, nighttime events will keep happening, which is not good.
        }
    }

    @Override
    public void onServerStopping(MinecraftServer server) {
        Mystical.CONFIG.save();
        Mystical.SPELL_HANDLER.save();
        Mystical.HAVEN_MANAGER.save();
    }

    /**
     * Sets the nighttime timer to the next night
     * @return Number of ticks until night, or -1 on failure
     */
    public long setNightTimer() throws NullPointerException {
        // CREDIT: Daomephsta#0044 for help on fabric discord
        if (server == null) {
            throw new NullPointerException(Utils.translateString("text.mystical.eventHandler.setNightTimerFailed", "server was null."));
        }
        if (timerAccess == null) {
            throw new NullPointerException(Utils.translateString("text.mystical.eventHandler.setNightTimerFailed", "timerAccess was null."));
        }
        long timerLength;
        long currentTime = server.getOverworld().getTimeOfDay() % 24000;
        if (currentTime > 18000) { // If we've passed midnight
            timerLength = (24000 - currentTime) + 18000; // (time left in this day) + (time from morning til midnight) = time until tomorrow's midnight
        } else { // It's before midnight
             timerLength = 18000 - currentTime; // midnight - (current time) = time until midnight
        }

        if (timerLength == 0) { // So that we don't get repeating events if this is fired twice in a tick.
            timerLength = 18000;
        }
        timerAccess.mystical_setTimer(timerLength);
        return timerLength;
    }
}
