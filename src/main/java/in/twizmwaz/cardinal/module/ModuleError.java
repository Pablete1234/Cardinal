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

package in.twizmwaz.cardinal.module;

import in.twizmwaz.cardinal.module.repository.LoadedMap;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.contrib.input.LineNumberElement;

@Data
@AllArgsConstructor
public final class ModuleError {

  private final Module module;
  private final LoadedMap map;
  private final String[] message;
  private final boolean critical;

  public ModuleError(Module module, LoadedMap map, boolean critical, String... message) {
    this(module, map, message, critical);
  }

  public ModuleError(Module module, LoadedMap map, Element element, String message, boolean critical) {
    this(module, map, critical, getErrorLocation(element), message);
  }

  public ModuleError(Module module, LoadedMap map, Attribute attr, String message, boolean critical) {
    this(module, map, critical, getErrorLocation(attr), message);
  }

  private static String getErrorLocation(Element element) {
    String result = "'" + element.getName() + "' element";
    if (element instanceof LineNumberElement) {
      LineNumberElement lineElement = (LineNumberElement) element;
      result += lineElement.getStartLine() == lineElement.getEndLine()
          ? " on line " + lineElement.getStartLine()
          : " from line " + lineElement.getStartLine() + " to " + lineElement.getEndLine();
    }
    return result;
  }

  private static String getErrorLocation(Attribute attribute) {
    return "'" + attribute.getName() + "' attribute in " + getErrorLocation(attribute.getParent());
  }

}
