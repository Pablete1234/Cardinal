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

package in.twizmwaz.cardinal.command;

import ee.ellytr.command.Command;
import ee.ellytr.command.CommandContext;
import ee.ellytr.command.PlayerCommand;
import ee.ellytr.command.argument.Optional;
import in.twizmwaz.cardinal.Cardinal;
import in.twizmwaz.cardinal.match.Match;
import in.twizmwaz.cardinal.module.countdown.AbstractCountdown;
import in.twizmwaz.cardinal.module.countdown.CountdownModule;
import org.bukkit.entity.Player;

public class CommandStart {

  /**
   * Starts the map.
   *
   * @param cmd  The context of this command.
   * @param time The time until start, defaults to 30.
   */
  @Command(aliases = "start", description = "Start the game", permissions = "cardinal.start")
  @PlayerCommand
  public static void start(CommandContext cmd, @Optional Integer time) {
    if (time == null) {
      time = 30;
    }
    time *= 20;
    Match match = Cardinal.getMatch((Player) cmd.getSender());
    AbstractCountdown countdown = Cardinal.getModule(CountdownModule.class).getStartCountdown(match);
    countdown.setTime(time);
    countdown.setCancelled(false);
  }

}
