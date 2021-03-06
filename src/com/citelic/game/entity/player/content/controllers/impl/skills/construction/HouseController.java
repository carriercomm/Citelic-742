package com.citelic.game.entity.player.content.controllers.impl.skills.construction;

import com.citelic.GameConstants;
import com.citelic.game.entity.Animation;
import com.citelic.game.entity.player.Player;
import com.citelic.game.entity.player.content.actions.skills.Skills;
import com.citelic.game.entity.player.content.actions.skills.construction.House;
import com.citelic.game.entity.player.content.actions.skills.construction.HouseConstants;
import com.citelic.game.entity.player.content.actions.skills.construction.HouseConstants.Builds;
import com.citelic.game.entity.player.content.actions.skills.construction.SitChair;
import com.citelic.game.entity.player.content.actions.skills.cooking.Cooking;
import com.citelic.game.entity.player.content.actions.skills.cooking.Cooking.Cookables;
import com.citelic.game.entity.player.content.controllers.Controller;
import com.citelic.game.entity.player.item.Item;
import com.citelic.game.map.objects.GameObject;

public class HouseController extends Controller {

	private House house;

	@Override
	public boolean canDropItem(Item item) {
		if (house.isBuildMode()) {
			player.getPackets().sendGameMessage(
					"You cannot drop items while in building mode.");
			return false;
		}
		return true;
	}

	@Override
	public boolean checkWalkStep(int lastX, int lastY, int nextX, int nextY) {
		return !house.isSky(nextX, nextY, player.getZ());
	}

	// shouldnt happen
	@Override
	public void forceClose() {
		house.leaveHouse(player, House.TELEPORTED);
	}

	public House getHouse() {
		return house;
	}

	public boolean handleItemOnObject(GameObject object, Item item) {
		if (object.getId() == HouseConstants.HObject.CLAY_FIREPLACE.getId()
				|| object.getId() == HouseConstants.HObject.STONE_FIREPLACE
						.getId()
				|| object.getId() == HouseConstants.HObject.MARBLE_FIREPLACE
						.getId()) {
			if (item.getId() != 1511) {
				player.getPackets()
						.sendGameMessage(
								"Only ordinary logs can be used to light the fireplace.");
				return false;
			}
			if (!player.getInventory().containsItemToolBelt(590)) {
				player.getPackets().sendGameMessage(
						"You do not have the required items to light this.");
				return false;
			}
			player.lock(2);
			player.setNextAnimation(new Animation(3658));
			player.getSkills().addXp(Skills.FIREMAKING, 40);
			GameObject objectR = new GameObject(object);
			objectR.setId(object.getId() + 1);
			// wiki says: If you light a fire in your fireplace, then change the
			// graphic settings, the fire will disappear meaning its not realy
			// spawned
			for (Player player : house.getPlayers()) {
				player.getPackets().sendSpawnedObject(objectR);
			}
			return false;
		} else if (HouseConstants.Builds.STOVE.containsObject(object)) {
			Cookables cook = Cooking.isCookingSkill(item);
			if (cook != null) {
				player.getDialogueManager().startDialogue("CookingD", cook,
						object);
				return false;
			}
			player.getDialogueManager()
					.startDialogue(
							"SimpleMessage",
							"You can't cook that on a "
									+ (object.getDefinitions().name
											.equals("Fire") ? "fire" : "range")
									+ ".");
			return false;
		}
		return true;
	}

	// shouldnt happen but lets imagine somehow in a server restart
	@Override
	public boolean login() {
		player.setNextTile(GameConstants.RESPAWN_PLAYER_LOCATION);
		removeController();
		return false; // remove controller manualy since i dont want to call
		// forceclose
	}

	@Override
	public boolean logout() {
		house.leaveHouse(player, House.LOGGED_OUT);
		return false; // leave house method removes controller already
	}

	@Override
	public void magicTeleported(int type) {
		house.leaveHouse(player, House.TELEPORTED);
	}

	@Override
	public boolean processObjectClick1(GameObject object) {
		// System.out.println(object.getXInChunk() + ", " + object.getYInChunk()
		// + ", 1");
		if (object.getId() == HouseConstants.HObject.EXIT_PORTAL.getId()) {
			house.leaveHouse(player, House.KICKED);
		} else if (object.getId() == HouseConstants.HObject.CLAY_FIREPLACE
				.getId()
				|| object.getId() == HouseConstants.HObject.STONE_FIREPLACE
						.getId()
				|| object.getId() == HouseConstants.HObject.MARBLE_FIREPLACE
						.getId()) {
			player.getPackets().sendGameMessage(
					"Use some logs on the fireplace in order to light it.");
		} else if (object.getId() >= HouseConstants.HObject.CRUDE_WOODEN_CHAIR
				.getId()
				&& object.getId() <= HouseConstants.HObject.MAHOGANY_ARMCHAIR
						.getId()) {
			int chair = object.getId()
					- HouseConstants.HObject.CRUDE_WOODEN_CHAIR.getId();
			player.getActionManager().setAction(
					new SitChair(player, chair, object));
		} else if (HouseConstants.Builds.BOOKCASE.containsObject(object)) {
			player.getPackets().sendGameMessage(
					"You search the bookcase but find nothing.");
		} else if (HouseConstants.Builds.STAIRCASE.containsObject(object)
				|| HouseConstants.Builds.STAIRCASE_DOWN.containsObject(object)) {
			if (object.getDefinitions().getOption(1).equals("Climb")) {
				player.getDialogueManager().startDialogue("ClimbHouseStairD",
						object);
			} else {
				house.climbStaircase(object,
						object.getDefinitions().getOption(1).equals("Climb-up"));
			}
		}
		return false;
	}

	@Override
	public boolean processObjectClick2(GameObject object) {
		// System.out.println(object.getXInChunk() + ", " + object.getYInChunk()
		// + ", 2");
		if (object.getId() == HouseConstants.HObject.EXIT_PORTAL.getId()) {
			house.switchLock(player);
		} else if (HouseConstants.Builds.STAIRCASE.containsObject(object)
				|| HouseConstants.Builds.STAIRCASE_DOWN.containsObject(object)) {
			house.climbStaircase(object, true);
		}
		return false;
	}

	@Override
	public boolean processObjectClick3(GameObject object) {
		if (HouseConstants.Builds.STAIRCASE.containsObject(object)
				|| HouseConstants.Builds.STAIRCASE_DOWN.containsObject(object)) {
			house.climbStaircase(object, false);
		}
		return false;
	}

	@Override
	public boolean processObjectClick4(GameObject object) {
		if (HouseConstants.Builds.STAIRCASE.containsObject(object)
				|| HouseConstants.Builds.STAIRCASE_DOWN.containsObject(object)) {
			house.removeRoom();
		}
		return false;
	}

	/**
	 * return process normaly
	 */
	@Override
	public boolean processObjectClick5(GameObject object) {
		// System.out.println(object.getXInChunk() + ", " + object.getYInChunk()
		// + ", 5");
		if (object.getDefinitions().containsOption(4, "Build")) {
			if (!house.isOwner(player)) {
				player.getPackets().sendGameMessage(
						"You can only do that in your own house.");
				return false;
			}
			if (house.isDoor(object)) {
				house.openRoomCreationMenu(object);
			} else {
				for (Builds build : HouseConstants.Builds.values()) {
					if (build.containsId(object.getId())) {
						house.openBuildInterface(object, build);
						return false;
					}

				}
			}
		} else if (object.getDefinitions().containsOption(4, "Remove")) {
			if (!house.isOwner(player)) {
				player.getPackets().sendGameMessage(
						"You can only do that in your own house.");
				return false;
			}
			house.openRemoveBuild(object);
		}
		return false;
	}

	@Override
	public void start() {
		house = (House) getArguments()[0];
		getArguments()[0] = null; // its was gonna be saved unless somehow in a
		// server restart but lets be safe
	}

}
