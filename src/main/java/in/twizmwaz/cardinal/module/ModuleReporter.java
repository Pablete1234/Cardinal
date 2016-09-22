package in.twizmwaz.cardinal.module;

import in.twizmwaz.cardinal.match.Match;
import in.twizmwaz.cardinal.module.id.IdModule;
import in.twizmwaz.cardinal.module.repository.LoadedMap;
import in.twizmwaz.cardinal.util.ParseUtil;
import in.twizmwaz.cardinal.util.document.XML;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jdom2.Attribute;
import org.jdom2.Element;

@RequiredArgsConstructor
public class ModuleReporter {

  private final Module module;
  private final LoadedMap map;
  @Getter
  private final Match match;

  public ModuleReporter(Module module, Match match) {
    this(module, match.getMap(), match);
  }

  @Getter
  private boolean canLoad = false;

  public void reset() {
    canLoad = true;
  }

  /**
   * Gets an attribute value from an element. If it fails to get a value, en error will be thrown.
   * @param element The element to get the attribute from.
   * @param attr The attribute to get.
   * @param type The class of the type to convert the attribute to.
   * @param <T> The type to convert the attribute value to.
   * @return An object of the correct type, or null if argument is missing or invalid.
   */
  public <T> T getAttr(Element element, String attr, Class<T> type) {
    return getAttr(element, attr, type, null, true);
  }

  /**
   * Gets an attribute value from an element. Defaults to fallback if not found.
   * @param element The element to get the attribute from.
   * @param attr The attribute to get.
   * @param type The class of the type to convert the attribute to.
   * @param fallback The fallback in case it's null or an error is thrown, null can be used as fallback.
   * @param <T> The type to convert the attribute value to.
   * @return An object of the correct type, or null if argument is missing or invalid.
   */
  public <T> T getAttr(Element element, String attr, Class<T> type, T fallback) {
    return getAttr(element, attr, type, fallback, false);
  }

  private <T> T getAttr(Element element, String attr, Class<T> type, T fallback, boolean required) {
    Attribute attribute = element.getAttribute(attr);
    if (required && attribute == null) {
      module.getErrors().add(new ModuleError(module, map, element, "Missing required attribute:'" + attr + "'", false));
      return null;
    }
    T result = getAttribute(attribute, type);
    if (required && result == null) {
      canLoad = false;
    }
    return result != null ? result : fallback;
  }

  private <T> T getAttribute(Attribute attr, Class<T> type) {
    try {
      return XML.getAttribute(attr, type);
    } catch (Exception e) {
      module.getErrors().add(new ModuleError(module, map, attr, e.getMessage(), false));
    }
    return null;
  }

  /**
   * Gets an attribute value from an element. If it fails to get a value, en error will be thrown.
   * @param element The element to get the object from.
   * @param type The class of the type to convert the element to.
   * @param <T> The type to convert the attribute value to.
   * @return An object of the correct type, or null if argument is missing or invalid.
   */
  public <T> T getEl(Element element, Class<T> type) {
    return getEl(element, type, null, true);
  }

  /**
   * Gets an attribute value from an element.
   * @param element The element to get the object from.
   * @param type The class of the type to convert the element to.
   * @param fallback The fallback in case it's null or an error is thrown, null can be used as fallback.
   * @param <T> The type to convert the attribute value to.
   * @return An object of the correct type, or null if argument is missing or invalid.
   */
  public <T> T getEl(@NonNull Element element, Class<T> type, T fallback) {
    return getEl(element, type, fallback, false);
  }

  private <T> T getEl(@NonNull Element element, Class<T> type, T fallback, boolean required) {
    T result = getElement(element, type);
    if (required && result == null) {
      canLoad = false;
    }
    return result != null ? result : fallback;
  }

  private <T> T getElement(Element el, Class<T> type) {
    try {
      return XML.getElementObject(el, type);
    } catch (Exception e) {
      module.getErrors().add(new ModuleError(module, map, el, e.getMessage(), false));
    }
    return null;
  }

  /**
   * Gets a property value from an element. It will try to get the object from the attribute,
   * if null, from child element.
   * @param element The element to get the property from.
   * @param prop The property to get.
   * @param type The class of the type to convert the property to.
   * @param <T> The type to convert the attribute value to.
   * @return An object of the correct type, or null if argument is missing or invalid.
   */
  public <T> T getProp(Element element, String prop, Class<T> type) {
    return getProp(element, prop, type, null, true);
  }

  /**
   * Gets a property value from an element. It will try to get the object from the attribute,
   * if missing, from child element.
   * @param element The element to get the property from.
   * @param prop The property to get.
   * @param type The class of the type to convert the property to.
   * @param fallback The fallback in case it's null or an error is thrown, null can be used as fallback.
   * @param <T> The type to convert the attribute value to.
   * @return An object of the correct type, or null if argument is missing or invalid.
   */
  public <T> T getProp(Element element, String prop, Class<T> type, T fallback) {
    return getProp(element, prop, type, fallback, false);
  }

  private  <T> T getProp(Element element, String prop, Class<T> type, T fallback, boolean required) {
    T result;
    Attribute attribute = element.getAttribute(prop);
    Element child = element.getChild(prop);
    if (attribute == null && child == null && required) {
      module.getErrors().add(new ModuleError(module, map, element, "Missing required property:'" + prop + "'", false));
      return null;
    }
    if (attribute != null) {
      result = getAttribute(attribute, type);
    } else {
      result = getElement(element, type);
    }
    if (required && result == null) {
      canLoad = false;
    }
    return result != null ? result : fallback;
  }

  public void checkId(Attribute attribute, String id) {
    if (!IdModule.get().canAdd(match, id)) {
      module.getErrors().add(new ModuleError(module, map, attribute, "Invalid or duplicated ID specified", false));
      canLoad = false;
    }
  }

}
