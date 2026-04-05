package io.github.DKICooked.gameLogic.generationLogic;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import io.github.DKICooked.entities.Platform;
import io.github.DKICooked.entities.PowerUpActor;

public class LevelGenerator {

    // ── Screen / jump constants ───────────────────────────────────────────────
    private static final float SCREEN_WIDTH  = 800f;
    private static final float MARGIN        = 100f;   // min X from edge
    private static final float MAX_JUMP_H    = 200;
    private static final float MAX_JUMP_W    = 300f;
    private static final float TILE_SIZE     = 40f;
    private static final float PLATFORM_HALF = TILE_SIZE * 1.5f; // half-width of a platform (3 tiles)

    // ── Spacing guards ────────────────────────────────────────────────────────
    private static final float MIN_STEP_Y    = 120f;   // minimum vertical rise per hop
    private static final float H_CLEARANCE   = 50f;    // horizontal breathing room
    private static final float V_CLEARANCE   = 50f;    // vertical breathing room

    // ── Chunk shape ───────────────────────────────────────────────────────────
    private static final int   NODES_PER_CHUNK   = 5;
    private static final float CHUNK_TOP_MARGIN  = 150f; // stop seeding this close to top
    private static final int   CANDIDATE_TRIES   = 10;   // retries per node placement

    // ── State carried between chunks ──────────────────────────────────────────
    /** Landing nodes from the top of the previous chunk — seed for the next one. */
    private final Array<float[]> seedNodes = new Array<>(); // each entry: {x, y}

    // ─────────────────────────────────────────────────────────────────────────

    public Array<Platform> generateChunk(float chunkYStart, float chunkHeight) {
        Array<Platform> platforms = new Array<>();
        float chunkTop = chunkYStart + chunkHeight;

        // ── Ground chunk (first ever) ─────────────────────────────────────────
        if (chunkYStart == 0) {
            platforms.add(new Platform(0, 50, SCREEN_WIDTH, 50));
            seedNodes.clear();
            seedNodes.add(new float[]{250f, 80f});
            seedNodes.add(new float[]{550f, 80f});
        }

        // ── Pick a layout theme for this chunk ────────────────────────────────
        ChunkTheme theme = ChunkTheme.random();

        // ── Build node graph ──────────────────────────────────────────────────
        Array<float[]> allNodes = new Array<>();
        for (float[] s : seedNodes) allNodes.add(s);

        Array<float[]> newNodes = new Array<>();

        int target = NODES_PER_CHUNK + theme.extraNodes;
        for (int i = 0; i < target; i++) {
            float[] node = placeNode(allNodes, chunkYStart, chunkTop, theme);
            if (node != null) {
                allNodes.add(node);
                newNodes.add(node);
            }
        }

        // ── Turn nodes into Platform objects ──────────────────────────────────
        for (float[] node : newNodes) {
            float cx = node[0];
            float cy = node[1];
            Platform p = new Platform(cx - PLATFORM_HALF, cy, cx + PLATFORM_HALF, cy);
            p.thickness = MathUtils.randomBoolean(0.3f) ? TILE_SIZE * 1.5f : TILE_SIZE;

            if (MathUtils.randomBoolean(0.15f)) {
                p.powerUpType = PowerUpActor.Type.GHOST;
            }

            platforms.add(p);
        }

        // ── Wall placement (safe: only when alternate route exists) ───────────
        placeWalls(platforms, newNodes, allNodes);

        // ── Seed next chunk from the topmost nodes ────────────────────────────
        seedNodes.clear();
        Array<float[]> sorted = new Array<>(newNodes);
        sorted.sort((a, b) -> Float.compare(b[1], a[1])); // descending Y
        int seedCount = Math.min(3, sorted.size);
        for (int i = 0; i < seedCount; i++) seedNodes.add(sorted.get(i));
        if (seedNodes.size == 0) { // fallback — shouldn't happen
            seedNodes.add(new float[]{SCREEN_WIDTH / 2f, chunkTop - CHUNK_TOP_MARGIN});
        }

        return platforms;
    }

    // ── Node placement ────────────────────────────────────────────────────────

    private float[] placeNode(Array<float[]> existing, float chunkYStart, float chunkTop, ChunkTheme theme) {
        float bestScore = Float.NEGATIVE_INFINITY;
        float[] best = null;

        for (int t = 0; t < CANDIDATE_TRIES; t++) {
            float[] candidate = generateCandidate(existing, chunkYStart, chunkTop, theme);
            if (candidate == null) continue;
            if (!isReachable(candidate, existing)) continue;
            if (isTooClose(candidate, existing)) continue;

            float score = scoreCandidate(candidate, existing, chunkTop, theme);
            if (score > bestScore) {
                bestScore = score;
                best = candidate;
            }
        }

        if (best != null) return best;

        // Fallback: guaranteed reachable node directly above the highest existing node
        float[] highest = existing.get(0);
        for (float[] n : existing) if (n[1] > highest[1]) highest = n;

        float fallbackX = MathUtils.clamp(
            highest[0] + MathUtils.random(-MAX_JUMP_W * 0.6f, MAX_JUMP_W * 0.6f),
            MARGIN + PLATFORM_HALF, SCREEN_WIDTH - MARGIN - PLATFORM_HALF
        );
        float fallbackY = highest[1] + MathUtils.random(MIN_STEP_Y, MAX_JUMP_H * 0.85f);

        if (fallbackY > chunkTop - CHUNK_TOP_MARGIN) return null;
        return new float[]{fallbackX, fallbackY};
    }

    private float[] generateCandidate(Array<float[]> existing, float chunkYStart, float chunkTop, ChunkTheme theme) {
        if (existing.size == 0) return null;

        // Pick a random existing node as the jump-off point
        float[] from = existing.get(MathUtils.random(Math.max(0, existing.size - 4), existing.size - 1));

        float jumpFrac = MathUtils.random(0.5f, 1.0f);
        float dy = MIN_STEP_Y + (MAX_JUMP_H - MIN_STEP_Y) * jumpFrac * theme.verticalBias;
        float dx = MAX_JUMP_W * MathUtils.random(0.3f, 0.9f) * (MathUtils.randomBoolean() ? 1 : -1) * theme.horizontalBias;

        float nx = MathUtils.clamp(from[0] + dx, MARGIN + PLATFORM_HALF, SCREEN_WIDTH - MARGIN - PLATFORM_HALF);
        float ny = from[1] + dy;

        if (ny > chunkTop - CHUNK_TOP_MARGIN) return null;
        if (ny < chunkYStart) return null;

        return new float[]{nx, ny};
    }

    /** Physics-based check: is this node reachable from at least one existing node below it? */
    private boolean isReachable(float[] node, Array<float[]> existing) {
        for (float[] other : existing) {
            if (other[1] >= node[1]) continue; // must be below
            float dy = node[1] - other[1];
            float dx = Math.abs(node[0] - other[0]);
            if (dy <= MAX_JUMP_H && dx <= MAX_JUMP_W) return true;
        }
        return false;
    }

    private boolean isTooClose(float[] node, Array<float[]> existing) {
        for (float[] other : existing) {
            float dx = Math.abs(node[0] - other[0]);
            float dy = Math.abs(node[1] - other[1]);
            // Only reject if they'd visually overlap
            if (dx < PLATFORM_HALF * 2 + 10f && dy < TILE_SIZE + 5f) return true;
        }
        return false;
    }

    /** Score encourages spread, variety, and staying in-theme. */
    private float scoreCandidate(float[] node, Array<float[]> existing, float chunkTop, ChunkTheme theme) {
        float score = 0f;

        // Reward distance from existing nodes (spread)
        float minDist = Float.MAX_VALUE;
        for (float[] other : existing) {
            float d = dst(node, other);
            if (d < minDist) minDist = d;
        }
        score += minDist * 0.5f;

        // Reward being away from screen edges
        float edgeDist = Math.min(node[0] - MARGIN, SCREEN_WIDTH - MARGIN - node[0]);
        score += edgeDist * theme.edgeBias;

        // Reward height progress
        score += node[1] * 0.3f;

        return score;
    }

    // ── Wall placement ────────────────────────────────────────────────────────

    private void placeWalls(Array<Platform> platforms, Array<float[]> newNodes, Array<float[]> allNodes) {
        for (float[] node : newNodes) {
            if (!MathUtils.randomBoolean(0.28f)) continue;

            // Only place a wall if the player has an alternate route to the node above
            float[] nodeAbove = findNearestAbove(node, allNodes);
            if (nodeAbove == null) continue;
            if (!hasAlternateRoute(node, nodeAbove, allNodes)) continue;

            // Place wall on whichever side of the platform is less obstructive
            float wallX = MathUtils.randomBoolean() ? node[0] + PLATFORM_HALF : node[0] - PLATFORM_HALF;
            wallX = MathUtils.clamp(wallX, MARGIN, SCREEN_WIDTH - MARGIN);

            float wallHeight = TILE_SIZE * MathUtils.random(2, 3);
            float baseY = node[1]; // sits on top of the platform surface
            platforms.add(new Platform(wallX, baseY, wallX, baseY + wallHeight));
        }
    }

    private float[] findNearestAbove(float[] node, Array<float[]> allNodes) {
        float[] best = null;
        float bestDy = Float.MAX_VALUE;
        for (float[] other : allNodes) {
            float dy = other[1] - node[1];
            if (dy > 0 && dy < bestDy) {
                bestDy = dy;
                best = other;
            }
        }
        return best;
    }

    /** Returns true if there is at least one node that can reach nodeAbove WITHOUT going through node. */
    private boolean hasAlternateRoute(float[] node, float[] nodeAbove, Array<float[]> allNodes) {
        for (float[] other : allNodes) {
            if (other == node || other == nodeAbove) continue;
            if (other[1] >= nodeAbove[1]) continue;
            float dy = nodeAbove[1] - other[1];
            float dx = Math.abs(nodeAbove[0] - other[0]);
            if (dy <= MAX_JUMP_H && dx <= MAX_JUMP_W) return true;
        }
        return false;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private float dst(float[] a, float[] b) {
        float dx = a[0] - b[0];
        float dy = a[1] - b[1];
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    // ── Chunk themes ─────────────────────────────────────────────────────────

    private enum ChunkTheme {
        ZIGZAG      (0,  1.0f, 1.2f,  0.2f), // tight horizontal hops
        OPEN        (1,  1.1f, 0.8f,  0.5f), // tall airy jumps, centered
        DENSE       (2,  0.7f, 0.7f, -0.2f), // lots of close platforms
        GAUNTLET    (0,  1.3f, 1.0f,  0.1f), // tall vertical corridor
        MIXED       (1,  1.0f, 1.0f,  0.3f); // default balanced

        final int   extraNodes;
        final float verticalBias;
        final float horizontalBias;
        final float edgeBias; // positive = prefer center, negative = prefer edges

        ChunkTheme(int extraNodes, float verticalBias, float horizontalBias, float edgeBias) {
            this.extraNodes     = extraNodes;
            this.verticalBias   = verticalBias;
            this.horizontalBias = horizontalBias;
            this.edgeBias       = edgeBias;
        }

        static ChunkTheme random() {
            ChunkTheme[] values = values();
            return values[MathUtils.random(values.length - 1)];
        }
    }
}
