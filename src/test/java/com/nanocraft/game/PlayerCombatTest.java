package com.nanocraft.game;

import com.nanocraft.game.entity.Player;
import com.nanocraft.game.entity.Entity;
import com.nanocraft.game.core.GameHandler;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class PlayerCombatTest {

    private GameHandler gh;
    private Player player;
    private Entity monster;

    @Before
    public void setUp() {
        gh = new GameHandler();
        player = gh.player;

        monster = new Entity(gh);
        monster.name = "Test Monster";
        monster.life = 10;
        monster.defense = 1;
        monster.exp = 3;
        monster.invincible = false;
        monster.dying = false;

        gh.monsters[0] = monster;
    }

    @Test
    public void testDamageReducesMonsterLife() {
        int startLife = monster.life;

        player.damage(0, 5);

        assertTrue(monster.life < startLife);
    }

    @Test
    public void testDamageSetsMonsterInvincible() {
        player.damage(0, 5);

        assertTrue(monster.invincible);
    }

    @Test
    public void testZeroOrLowDamageDoesNotHealMonster() {
        int startLife = monster.life;

        player.damage(0, 0);

        assertTrue(monster.life <= startLife);
    }

    @Test
    public void testKillMonsterSetsDyingTrue() {
        monster.life = 2;
        monster.defense = 0;

        player.damage(0, 10);

        assertTrue(monster.dying);
    }

    @Test
    public void testKillMonsterAwardsExp() {
        monster.life = 2;
        monster.defense = 0;
        monster.exp = 4;

        int startExp = player.exp;

        player.damage(0, 10);

        assertEquals(startExp + 4, player.exp);
    }

    @Test
    public void testLevelUpWhenEnoughExpIsEarned() {
        monster.life = 1;
        monster.defense = 0;
        monster.exp = player.nextLevelExp; // enough to level up in one kill

        int startLevel = player.level;
        int startMaxLife = player.maxLife;
        int startStrength = player.strength;
        int startDexterity = player.dexterity;

        player.damage(0, 10);

        assertEquals(startLevel + 1, player.level);
        assertTrue(player.maxLife > startMaxLife);
        assertTrue(player.strength > startStrength);
        assertTrue(player.dexterity > startDexterity);
    }

    @Test
    public void testLevelUpChangesGameStateToDialogue() {
        monster.life = 1;
        monster.defense = 0;
        monster.exp = player.nextLevelExp;

        player.damage(0, 10);

        assertEquals(gh.dialogue, gh.gameState);
        assertNotNull(gh.ui.currentDialogue);
        assertTrue(gh.ui.currentDialogue.contains("level"));
    }

    @Test
    public void testDamageDoesNothingForInvalidMonsterIndex() {
        int startExp = player.exp;

        player.damage(999, 10);

        assertEquals(startExp, player.exp);
    }

    @Test
    public void testDamageDoesNotApplyIfMonsterAlreadyInvincible() {
        monster.invincible = true;
        int startLife = monster.life;

        player.damage(0, 10);

        assertEquals(startLife, monster.life);
    }
}