package io.github.DKICooked.gameLogic;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import io.github.DKICooked.entities.Platform;
import io.github.DKICooked.gameLogic.generationLogic.LevelGenerator;

public class WorldManager {
    public static final float CHUNK_HEIGHT = 600f;

    private final LevelGenerator generator = new LevelGenerator();
    private final IntMap<Chunk> chunks = new IntMap<>();
    private final Array<Platform> activePlatforms = new Array<>();

    private int currentChunk = 0;

    public WorldManager() {
        generateNearbyChunks(0);
        refreshActivePlatforms();
    }

    public void update(float playerY) {
        int playerChunk = (int) (playerY / CHUNK_HEIGHT);

        if (playerChunk != currentChunk) {
            currentChunk = playerChunk;
            generateNearbyChunks(currentChunk);
            refreshActivePlatforms();
        }
    }

    public void generateNearbyChunks(int index) {
        getOrCreateChunk(index - 1); // For falling safety
        getOrCreateChunk(index);
        getOrCreateChunk(index + 1);
    }

    private void getOrCreateChunk(int index) {
        if (index < 0 || chunks.containsKey(index)) return;
        Chunk chunk = new Chunk(index, index * CHUNK_HEIGHT);
        chunk.platforms.addAll(generator.generateChunk(chunk.yStart, CHUNK_HEIGHT));
        chunks.put(index, chunk);
    }

    public void refreshActivePlatforms() {
        activePlatforms.clear();
        // Check -1, 0, and 1
        for (int i = currentChunk - 1; i <= currentChunk + 1; i++) {
            if (chunks.containsKey(i)) {
                activePlatforms.addAll(chunks.get(i).platforms);
            }
        }
    }

    public Array<Platform> getActivePlatforms() { return activePlatforms; }
    public int getCurrentChunk() { return currentChunk; }

    private static class Chunk {
        int index;
        float yStart;
        Array<Platform> platforms = new Array<>();
        Chunk(int index, float yStart) { this.index = index; this.yStart = yStart; }
    }
}
