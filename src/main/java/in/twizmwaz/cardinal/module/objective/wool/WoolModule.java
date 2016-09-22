/*
 * Copyright (c) 2016, Kevin Phoenix
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package in.twizmwaz.cardinal.module.objective.wool;

import ee.ellytr.chat.ChatConstant;
import ee.ellytr.chat.component.builder.LocalizedComponentBuilder;
import in.twizmwaz.cardinal.Cardinal;
import in.twizmwaz.cardinal.component.team.TeamComponent;
import in.twizmwaz.cardinal.event.objective.ObjectiveCompleteEvent;
import in.twizmwaz.cardinal.match.Match;
import in.twizmwaz.cardinal.module.AbstractListenerModule;
import in.twizmwaz.cardinal.module.ModuleEntry;
import in.twizmwaz.cardinal.module.ModuleError;
import in.twizmwaz.cardinal.module.ModuleReporter;
import in.twizmwaz.cardinal.module.apply.AppliedModule;
import in.twizmwaz.cardinal.module.apply.AppliedRegion;
import in.twizmwaz.cardinal.module.apply.ApplyType;
import in.twizmwaz.cardinal.module.apply.regions.WoolMonumentPlace;
import in.twizmwaz.cardinal.module.filter.FilterState;
import in.twizmwaz.cardinal.module.filter.type.StaticFilter;
import in.twizmwaz.cardinal.module.id.IdModule;
import in.twizmwaz.cardinal.module.objective.ProximityMetric;
import in.twizmwaz.cardinal.module.objective.ProximityRule;
import in.twizmwaz.cardinal.module.region.Region;
import in.twizmwaz.cardinal.module.region.RegionException;
import in.twizmwaz.cardinal.module.region.RegionModule;
import in.twizmwaz.cardinal.module.team.Team;
import in.twizmwaz.cardinal.module.team.TeamModule;
import in.twizmwaz.cardinal.playercontainer.PlayingPlayerContainer;
import in.twizmwaz.cardinal.util.Channels;
import in.twizmwaz.cardinal.util.Components;
import in.twizmwaz.cardinal.util.Numbers;
import in.twizmwaz.cardinal.util.ParseUtil;
import in.twizmwaz.cardinal.util.Proto;
import in.twizmwaz.cardinal.util.Strings;
import lombok.NonNull;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jdom2.Document;
import org.jdom2.Element;

import java.util.List;

@ModuleEntry(depends = {IdModule.class, TeamModule.class, RegionModule.class, AppliedModule.class})
public class WoolModule extends AbstractListenerModule {

  @Override
  public boolean loadMatch(Match match) {
    ModuleReporter reporter = new ModuleReporter(this, match);
    Document document = match.getMap().getDocument();
    for (Element woolElement : ParseUtil.getElementsIn(document.getRootElement(), "wools", "wool")) {
      parseWool(reporter, woolElement);
      reporter.reset();
    }
    return true;
  }

  public static Wool parseWool(ModuleReporter reporter, Element element) {
    String id = reporter.getAttr(element, "id", String.class, null);
    if (id != null) {
      reporter.checkId(element.getAttribute(id), id);
    }

    boolean required = reporter.getAttr(element, "required", Boolean.class, true);
    Team team = reporter.getAttr(element, "team", Team.class);
    DyeColor color = reporter.getAttr(element, "color", DyeColor.class);
    Region monument = reporter.getProp(element, "monument", Region.class);
    boolean craftable = reporter.getAttr(element, "craftable", Boolean.class, false);
    boolean show = reporter.getAttr(element, "show", Boolean.class, true);

    Vector location = reporter.getAttr(element, "location", Vector.class, null);

    ProximityRule woolProximity = new ProximityRule(
        reporter.getAttr(element, "woolproximity-metric", ProximityMetric.class, ProximityMetric.CLOSEST_KILL),
        reporter.getAttr(element, "woolproximity-horizontal", Boolean.class, false));
    ProximityRule monumentProximity = new ProximityRule(
        reporter.getAttr(element, "monumentproximity-metric", ProximityMetric.class, ProximityMetric.CLOSEST_BLOCK),
        reporter.getAttr(element, "monumentproximity-horizontal", Boolean.class, false));

    if (reporter.isCanLoad()) {
      Wool wool = new Wool(reporter.getMatch(), required, team, color, monument, craftable,
          show, location, woolProximity, monumentProximity);
      IdModule.get().add(reporter.getMatch(), null, wool, true);

      AppliedModule appliedModule = Cardinal.getModule(AppliedModule.class);
      appliedModule.add(reporter.getMatch(), new WoolMonumentPlace(wool), true);
      appliedModule.add(reporter.getMatch(),
          new AppliedRegion(ApplyType.BLOCK_BREAK, monument, new StaticFilter(FilterState.DENY),
              new LocalizedComponentBuilder(
                  ChatConstant.getConstant("objective.wool.error.break"),
                  wool.getComponent()
              ).color(ChatColor.RED).build()), true);
      return wool;
    } else {
      return null;
    }
  }

  public List<Wool> getWools(@NonNull Match match) {
    return IdModule.get().getList(match, Wool.class);
  }

  /**
   * Checks if the wool has been picked up when a player clicks on an item in their inventory.
   *
   * @param event The event.
   */
  @EventHandler(ignoreCancelled = true)
  public void onInventoryClick(InventoryClickEvent event) {
    for (Wool wool : getWools(Cardinal.getMatch(event.getWorld()))) {
      Player player = event.getActor();
      ItemStack item = event.getCurrentItem();
      Team team = wool.getTeam();
      Match match = Cardinal.getMatch(player);
      PlayingPlayerContainer container = match.getPlayingContainer(player);
      if (!wool.isComplete()
          && item.getType().equals(Material.WOOL)
          && item.getData().getData() == wool.getColor().getData()
          && team.equals(container)) {
        wool.setTouched(true);
        boolean showMessage = false;
        if (wool.isShow() && !wool.hasPlayerTouched(player)) {
          wool.addPlayerTouched(player);
          showMessage = true;

          Channels.getTeamChannel(match, team).sendPrefixedMessage(
              new LocalizedComponentBuilder(
                  ChatConstant.getConstant("objective.wool.touched"),
                  Components.getName(player).build(),
                  wool.getComponent(),
                  new TeamComponent(wool.getTeam())
              ).build()
          );
          //todo: send message to observers
        }
      }
    }
  }

  /**
   * Checks if the wool has been picked up when a player picks an item up from the ground.
   *
   * @param event The event.
   */
  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onPlayerPickupItem(PlayerPickupItemEvent event) {
    for (Wool wool : getWools(Cardinal.getMatch(event.getWorld()))) {
      Player player = event.getPlayer();
      ItemStack item = event.getItem().getItemStack();
      Team team = wool.getTeam();
      Match match = Cardinal.getMatch(player);
      PlayingPlayerContainer container = match.getPlayingContainer(player);
      if (!wool.isComplete()
          && item.getType().equals(Material.WOOL)
          && item.getData().getData() == wool.getColor().getData()
          && team.equals(container)) {
        wool.setTouched(true);
        if (wool.isShow() && !wool.hasPlayerTouched(player)) {
          wool.addPlayerTouched(player);

          Channels.getTeamChannel(match, team).sendPrefixedMessage(
              new LocalizedComponentBuilder(
                  ChatConstant.getConstant("objective.wool.touched"),
                  Components.getName(player).build(),
                  wool.getComponent(),
                  new TeamComponent(wool.getTeam())
              ).build()
          );
          //todo: send message to observers
        }
      }
    }
  }

  /**
   * Checks if this wool has been captured when a block is placed.
   *
   * @param event The event.
   */
  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onBlockPlace(BlockPlaceEvent event) {
    for (Wool wool : getWools(Cardinal.getMatch(event.getWorld()))) {
      if (wool.isComplete()) {
        continue;
      }
      Player player = event.getPlayer();
      Block block = event.getBlock();
      if (wool.getMonument().contains(block.getLocation().toVector()) && block.getType().equals(Material.WOOL)
          && ((org.bukkit.material.Wool) block.getState().getMaterialData()).getColor().equals(wool.getColor())) {
        wool.setComplete(true);

        if (wool.isShow()) {
          //fixme: unchecked cast
          Match match = Cardinal.getMatch(event.getWorld());
          Team team = (Team) match.getPlayingContainer(player);
          Channels.getGlobalChannel(match.getMatchThread()).sendMessage(
              new LocalizedComponentBuilder(ChatConstant.getConstant("objective.wool.completed"),
                  Components.getName(player).build(),
                  wool.getComponent(),
                  new TeamComponent(team)).color(ChatColor.GRAY).build());
        }
        Bukkit.getPluginManager().callEvent(new ObjectiveCompleteEvent(wool, player));
      }
    }
  }

  /**
   * Prevents the wool from being crafted if specified when registering the wool.
   *
   * @param event The event.
   */
  @EventHandler(ignoreCancelled = true)
  public void onCraftItem(CraftItemEvent event) {
    for (Wool wool : getWools(Cardinal.getMatch(event.getWorld()))) {
      if (event.getRecipe().getResult().equals(new ItemStack(Material.WOOL, 1, wool.getColor().getData()))
          && !wool.isCraftable()) {
        event.setCancelled(true);
        break;
      }
    }
  }

  /**
   * Removes the player from the list of players who have touched the wool during their previous life.
   *
   * @param event The event.
   */
  @EventHandler
  public void onPlayerDeath(PlayerDeathEvent event) {
    for (Wool wool : getWools(Cardinal.getMatch(event.getWorld()))) {
      wool.removePlayerTouched(event.getEntity());
    }
  }

}
