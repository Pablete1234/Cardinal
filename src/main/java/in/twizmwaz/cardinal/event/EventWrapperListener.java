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

package in.twizmwaz.cardinal.event;

import in.twizmwaz.cardinal.Cardinal;
import in.twizmwaz.cardinal.event.wrappers.PlayerChangePositionEvent;
import in.twizmwaz.cardinal.match.Match;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class EventWrapperListener implements Listener {

  public EventWrapperListener() {
    Cardinal.registerEvents(this);
  }

  @EventHandler
  public void onPlayerMove(PlayerMoveEvent event) {
    if (handleMoveEvent(event.getPlayer(), event.getFrom(), event.getTo())) {
      event.setTo(event.getFrom());
    }
  }

  @EventHandler
  public void onPlayerTeleport(PlayerTeleportEvent event) {
    if (handleMoveEvent(event.getPlayer(), event.getFrom(), event.getTo())) {
      event.setTo(event.getFrom());
    }
  }

  /* Return true if event should be cancelled, false otherwise */
  private boolean handleMoveEvent(Player player, Location from, Location to) {
    if (!from.equals(to)) {
      Match match = Cardinal.getMatch(player);
      PlayerChangePositionEvent moveEvent = new PlayerChangePositionEvent(match, player, from, to, false);

      Bukkit.getPluginManager().callEvent(moveEvent);
      return moveEvent.isCancelled();
    }
    return false;
  }

}
