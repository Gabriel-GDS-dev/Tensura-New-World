package com.gabriel.tensuranewworld.gameplay;

import com.gabriel.tensuranewworld.data.TensuraPlayerData;
import net.minecraft.server.level.ServerPlayer;

public final class StoryManager {
    private StoryManager() {
    }

    public static void evaluate(ServerPlayer player, TensuraPlayerData data) {
        int oldChapter = data.getStoryChapter();
        if (data.isTrueDemonLord()) {
            data.setStoryChapter(5);
            data.setStoryObjective("Build a lasting alliance between nations");
        } else if (!data.getNationName().isBlank()) {
            data.setStoryChapter(4);
            data.setStoryObjective("Gather 10,000 human souls and awaken");
        } else if (data.getSubordinates().size() >= 5) {
            data.setStoryChapter(3);
            data.setStoryObjective("Found a nation with /tnw nation_found <name>");
        } else if (data.getBattlesWon() >= 10) {
            data.setStoryChapter(2);
            data.setStoryObjective("Name five loyal subordinates");
        } else if (data.getStoryChapter() == 0 && data.getBattlesWon() > 0) {
            data.setStoryChapter(1);
            data.setStoryObjective("Win ten battles and establish your strength");
        }
        if (data.getStoryChapter() > oldChapter) {
            VoiceOfTheWorld.notice(player, "Chapter " + data.getStoryChapter() + " unlocked: " + data.getStoryObjective());
        }
    }
}
