package io.github.DKICooked.gameLogic;

import io.github.DKICooked.entities.LBScore;
import com.badlogic.gdx.utils.Array;

public class SaveData {
    // Ensure the array is initialized to prevent NullPointerExceptions
    public Array<LBScore> leaderBoard = new Array<>();

    public boolean isHighScore(int score) {
        if (score <= 0) return false; // Optional: don't count 0 scores
        if (leaderBoard.size < 10) return true;
        return score > leaderBoard.get(leaderBoard.size - 1).score;
    }

    public void addScore(String name, int score) {
        LBScore newEntry = new LBScore(name, score);

        // 1. Find the first index where the existing score is SMALLER than the new one
        int insertIndex = 0;
        while (insertIndex < leaderBoard.size && leaderBoard.get(insertIndex).score >= score) {
            insertIndex++;
        }

        // 2. Insert it (LibGDX Array.insert handles the "shifting" for you)
        leaderBoard.insert(insertIndex, newEntry);

        // 3. Keep it to a Top 10
        if (leaderBoard.size > 10) {
            leaderBoard.truncate(10);
        }
    }
}
