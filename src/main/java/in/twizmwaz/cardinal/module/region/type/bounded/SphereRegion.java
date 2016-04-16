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

package in.twizmwaz.cardinal.module.region.type.bounded;

import in.twizmwaz.cardinal.module.region.parser.bounded.SphereRegionParser;
import in.twizmwaz.cardinal.module.region.type.BoundedRegion;
import lombok.AllArgsConstructor;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
public class SphereRegion implements BoundedRegion {

  private final Vector origin;
  private final double radius;

  public SphereRegion(SphereRegionParser parser) {
    this(parser.getOrigin(), parser.getRadius());
  }

  @Override
  public List<Block> getBlocks() {
    CuboidRegion bound = new CuboidRegion(
        new Vector(origin.getX() - radius, origin.getY() - radius, origin.getZ() - radius),
        new Vector(origin.getX() + radius, origin.getY() + radius, origin.getZ() + radius));
    return bound.getBlocks().stream().filter(block -> evaluate(block.getLocation().toVector().add(0.5, 0.5, 0.5)))
        .collect(Collectors.toList());
  }

  @Override
  public BlockRegion getCenterBlock() {
    return new BlockRegion(origin);
  }

  @Override
  public boolean evaluate(Vector evaluating) {
    return evaluating.isInSphere(origin, radius);
  }
}