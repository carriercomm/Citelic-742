package com.citelic.game.entity.npc.combat.impl;

import com.citelic.game.entity.Animation;
import com.citelic.game.entity.Entity;
import com.citelic.game.entity.Graphics;
import com.citelic.game.entity.npc.NPC;
import com.citelic.game.entity.npc.combat.CombatScript;
import com.citelic.game.entity.npc.combat.NPCCombatDefinitions;
import com.citelic.game.entity.player.Player;

public class AbbysalTitanCombat extends CombatScript {

	@Override
	public int attack(NPC npc, Entity target) {
		final NPCCombatDefinitions defs = npc.getCombatDefinitions();
		int damage = 0;
		damage = getRandomMaxHit(npc, 140, NPCCombatDefinitions.MELEE, target);
		npc.setNextAnimation(new Animation(7980));
		npc.setNextGraphics(new Graphics(1490));

		if (target instanceof Player) { // cjay failed dragonkk saved the day
			Player player = (Player) target;
			if (damage > 0 && player.getPrayer().getPrayerpoints() > 0)
				player.getPrayer().drainPrayer(damage / 2);
		}
		delayHit(npc, 0, target, getMeleeHit(npc, damage));
		return defs.getAttackDelay();
	}

	@Override
	public Object[] getKeys() {
		return new Object[] { 7350, 7349 };
	}
}
