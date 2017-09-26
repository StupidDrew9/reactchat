package me.stupiddrew9.reactchat.listeners;

import java.util.List;
import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.stupiddrew9.reactchat.React;
import me.stupiddrew9.reactchat.util.InvUtil;
import net.md_5.bungee.api.ChatColor;

public class InvListener implements Listener {
	
	int currentInv = 0;
	Inventory inventory = null;
	Inventory inventoryMenu = null;
	ItemStack menuItem = null;
	
	HashMap<Player, Boolean> ifMenuOpen = new HashMap<Player, Boolean>();
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent clicc) {

		Player player = (Player) clicc.getWhoClicked();
		String playerName = player.getName();
		Inventory cliccInv = clicc.getInventory();
		
		if (!(ifMenuOpen.containsKey(player))) {
		    ifMenuOpen.put(player, false);
		}
		
		Player invOwner = null;
		String originalMsg = null;
		
		for (Inventory inv : React.getInvIdentity().keySet()) {
			if (inv.getTitle() == cliccInv.getTitle()) {
				inventory = inv;
				invOwner = React.getPlayersHash().get(inv);
				originalMsg = React.getMsgHash().get(inv);
				currentInv = React.getInventories().indexOf(inv);
				player.sendMessage(ChatColor.RED + Integer.toString(currentInv));
				inventoryMenu = React.getInventoriesMenu().get(currentInv);
				break;
			} else {
				continue;
			}
		}
		
		for (Inventory inv : React.getInvMenuIdentity().keySet()) {
			if (inv.getTitle() == cliccInv.getTitle()) {
				inventory = React.getInventories().get(React.getInvMenuIdentity().get(inv).intValue());
				invOwner = React.getPlayersHash().get(inventory);
				originalMsg = React.getMsgHash().get(inventory);
				currentInv = React.getInventories().indexOf(inventory);
				player.sendMessage(ChatColor.RED + Integer.toString(currentInv));
				inventoryMenu = React.getInventoriesMenu().get(currentInv);
				break;
			} else {
				continue;
			}
		}
		
		if ((invOwner == null) || (inventory == null)) {
			player.sendMessage(ChatColor.GOLD + "Wrong!");
			return;
		}

		String invOwnerName = invOwner.getName();

		ItemStack item = clicc.getCurrentItem();
		
		clicc.setCancelled(true);

		if (clicc.getClick().isShiftClick()) {
			return;
		}
		
		if (!(clicc.getSlotType() == SlotType.CONTAINER)) {
			player.closeInventory();
			return;
		}
		
		if (clicc.getCurrentItem().getType() == Material.STAINED_GLASS_PANE ||
				  clicc.getCurrentItem().getType() == Material.AIR) {
			return;
		}

		if (clicc.getRawSlot() == 22) {
			
			if (ifMenuOpen.get(player) == true) {
				player.openInventory(React.getInventories().get(currentInv));
			    ifMenuOpen.put(player, false);
			    return;
			}
			
			player.openInventory(React.getInventoriesMenu().get(currentInv));
		    ifMenuOpen.put(player, true);
			return;
			
		}
		
		if (playerName == invOwnerName) {
			player.sendMessage(ChatColor.GOLD + "You can't react to your own message!");
			player.closeInventory();
			return;
		}
		
		int slot = 0;
		
		for (int j = 27; j <= inventoryMenu.getSize(); j++) {
		    menuItem = inventoryMenu.getItem(j);
		    if (item.getItemMeta().getDisplayName() == menuItem.getItemMeta().getDisplayName()) {
				slot = j;
		    	break;
		    }
		} 
		if (!(item.getItemMeta().getDisplayName() == menuItem.getItemMeta().getDisplayName())) {
			return;
		}

		// reactions per player
		int reactAmnt = 0;
		if (!(React.getReactCount().get(currentInv).get(player) == null)) {
			reactAmnt = React.getReactCount().get(currentInv).get(player).intValue();
		}
		// new item for the added reactions section
		ItemStack newReaction = new ItemStack(item);
		// reactors
		List<String> reactors = new ArrayList<String>();
		// counts amnt of reactions
		int reactionsAmnt = 0;
		
		if (clicc.getRawSlot() < 18) {
			// look in the added reactions only
			// look in both invs
			
			player.sendMessage(ChatColor.GOLD + "1");
			// iterates through each reacted reaction in that inventory
			for (Entry<ItemStack, List<String>> reaction : React.getAddedReactions().get(currentInv).entrySet()) {
				reactionsAmnt++;
				// reactcount is for checking if too many reactions are added to the msg
				if ((React.getReactCount().get(currentInv).containsKey(player) == false)) {
					React.getReactCount().get(currentInv).put(player, 0);
				}
				player.sendMessage(ChatColor.GOLD + "2");
				// if reaction is in the hash of reactions
				if (reaction.getKey().getItemMeta().getDisplayName() == item.getItemMeta().getDisplayName()) {
					player.sendMessage(ChatColor.GOLD + "3");
					// if the player is already a reactor of the reaction
					if (reaction.getValue().contains(playerName)) {
						player.sendMessage(ChatColor.GOLD + "5");
						// remove the player from the list of reactors
						reaction.getValue().remove(playerName);
						React.getReactCount().get(currentInv).put(player, reactAmnt--);
						// if there are no reactors, remove the item from reactions
						if (reaction.getValue().isEmpty()) {
							React.getAddedReactions().get(currentInv).remove(reaction);
						}
						
						InvUtil.setReactions(inventory, inventoryMenu, invOwner, 
					             React.getAddedReactions().get(currentInv));
						
						break;
					}
					reaction.getValue().add(playerName);
					break;
				}
				
				if (reactAmnt >= 3) {
					player.sendMessage(ChatColor.GOLD + "You can't react to more than three messages!");
					player.closeInventory();
					return;
				}
				
				React.getReactCount().get(currentInv).put(player, reactAmnt++);
				
				// if reaction is not in the array (no prior reactions)
				// cantdew since only checking in the added reactions (0-17 inv slots)
				if (reactionsAmnt == React.getAddedReactions().get(currentInv).size()) {
					player.sendMessage(ChatColor.GOLD + "Error encountered.");
				}
				player.sendMessage(ChatColor.GOLD + "4");
				// update list of added reactions
				InvUtil.setReactions(inventory, inventoryMenu, invOwner, 
			             React.getAddedReactions().get(currentInv));
				// alert users of reaction event
				InvUtil.sendAlert(player, invOwner, originalMsg, React.reactNames[slot - 27]);
			}
		}
		
		if (clicc.getRawSlot() > 26) {
			// look in all reactions
			// look in menu inv
			player.sendMessage(ChatColor.GOLD + "11");
			for (Entry<ItemStack, List<String>> reaction : React.getAddedReactions().get(currentInv).entrySet()) {
				reactionsAmnt++;
				// reactcount is for checking if too many reactions are added to the msg
				if ((React.getReactCount().get(currentInv).containsKey(player) == false)) {
					React.getReactCount().get(currentInv).put(player, 0);
				}
				player.sendMessage(ChatColor.GOLD + "22");
				// if reaction is in the hash of reactions
				if (reaction.getKey().getItemMeta().getDisplayName() == item.getItemMeta().getDisplayName()) {
					player.sendMessage(ChatColor.GOLD + "33");
					if (React.getAddedReactions().get(currentInv) == null) {
						player.sendMessage(ChatColor.GOLD + "Whoops I Screwed Up Maybe");
					}
					if (reaction.getValue() == null) {
						player.sendMessage(ChatColor.GOLD + "Whoops I Screwed Up");
						return;
					}
					// if the player is already a reactor of the reaction
					if (reaction.getValue().contains(playerName)) {
						player.sendMessage(ChatColor.GOLD + "55");
						// remove the player from the list of reactors
						reaction.getValue().remove(playerName);
						React.getReactCount().get(currentInv).put(player, reactAmnt--);
						// if there are no reactors, remove the item from reactions
						if (reaction.getValue().isEmpty()) {
							React.getAddedReactions().get(currentInv).remove(reaction);
						}
						
						InvUtil.setReactions(inventory, inventoryMenu, invOwner, 
					             React.getAddedReactions().get(currentInv));
						
						break;
					}
					reaction.getValue().add(playerName);
					// update the reactions list
					InvUtil.setReactions(inventory, inventoryMenu, invOwner, 
				             React.getAddedReactions().get(currentInv));
					// alert users of reaction event
					InvUtil.sendAlert(player, invOwner, originalMsg, React.reactNames[slot - 27]);
					break;
				}
				
				if (reactAmnt >= 3) {
					player.sendMessage(ChatColor.GOLD + "You can't react to more than three messages!");
					player.closeInventory();
					return;
				}
				
				React.getReactCount().get(currentInv).put(player, reactAmnt++);
				
				player.sendMessage(ChatColor.GOLD + "44");
				// if reaction is not in the array (no prior reactions)
				if (reactionsAmnt == React.getAddedReactions().get(currentInv).size()) {
					// check if reaction limit is reached
					if (React.getAddedReactions().get(currentInv).size() == 18) {
						player.sendMessage(ChatColor.GOLD + "This message has reached the reaction limit.");
						return;
					}
					// add the reaction to the list of reactions
				 	reactors.add(playerName);
				 	React.getAddedReactions().get(currentInv).put(newReaction, reactors);
				}
				
				// update the reactions list
				InvUtil.setReactions(inventory, inventoryMenu, invOwner, 
			             React.getAddedReactions().get(currentInv));
				// alert users of reaction event
				InvUtil.sendAlert(player, invOwner, originalMsg, React.reactNames[slot - 27]);
				return;
			}
			
			player.sendMessage(ChatColor.GOLD + "reactioawjiotajioawstjio");
			// check if reaction limit is reached
			if (React.getAddedReactions().get(currentInv).size() == 18) {
				player.sendMessage(ChatColor.GOLD + "This message has reached the reaction limit.");
				return;
			}
			// add the reaction to the list of reactions
		 	reactors.add(playerName);
		 	React.getAddedReactions().get(currentInv).put(newReaction, reactors);
		 	for (String string : React.getAddedReactions().get(currentInv).get(newReaction)) {
		 		player.sendMessage(ChatColor.GOLD + newReaction.getItemMeta().getDisplayName());
		 		player.sendMessage(ChatColor.GOLD + string);
		 	}
		 	
			// update the reactions list
			InvUtil.setReactions(inventory, inventoryMenu, invOwner, 
		             React.getAddedReactions().get(currentInv));
			// alert users of reaction event
			InvUtil.sendAlert(player, invOwner, originalMsg, React.reactNames[slot - 27]);
			
		}

		currentInv = 0;
		
		inventory = null;
		inventoryMenu = null;
		menuItem = null;
		reactors = null;

	}	
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onInventoryExit(InventoryCloseEvent event) {
		Player player = (Player) event.getPlayer();
	    ifMenuOpen.put(player, false);
	}

}
