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

package in.twizmwaz.cardinal.module.region.type.modifications;

import in.twizmwaz.cardinal.module.region.AbstractRegion;
import in.twizmwaz.cardinal.module.region.Region;
import in.twizmwaz.cardinal.module.region.parser.modifications.TranslatedRegionParser;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.stream.Collectors;

public class TranslatedRegion extends AbstractRegion {

  private final Region region;
  private final Vector offset;

  /**
   * Creates a region translated by an offset from an original region.
   *
   * @param region The original region.
   * @param offset The offset.
   */
  public TranslatedRegion(Region region, Vector offset) {
    super(region.getBounds().translate(offset));
    this.region = region;
    this.offset = offset;
  }

  public TranslatedRegion(TranslatedRegionParser parser) {
    this(parser.getRegion(), parser.getOffset());
  }

  @Override
  public boolean contains(Vector vector) {
    return region.contains(vector.minus(offset));
  }

  @Override
  public boolean isRandomizable() {
    return region.isRandomizable();
  }

  @Override
  public boolean isBounded() {
    return region.isBounded();
  }

  @Override
  public Collection<Block> getBlocks() {
    if (!isBounded()) {
      throw new UnsupportedOperationException("Cannot get blocks in unbounded region");
    }
    if (super.getBlocks() != null) {
      return super.getBlocks();
    }
    Collection<Block> blocks = getBounds().getBlocks().stream().filter(
        block -> contains(block.getLocation().toVector().plus(0.5, 0.5, 0.5))).collect(Collectors.toSet());
    setBlocks(blocks);
    return super.getBlocks();
  }

  @Override
  public Vector getRandomPoint() {
    if (!isRandomizable()) {
      throw new UnsupportedOperationException("Cannot get random point in non-randomizable region");
    }
    return region.getRandomPoint().plus(offset);
  }

}
