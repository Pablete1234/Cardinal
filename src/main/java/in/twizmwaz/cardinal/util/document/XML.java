package in.twizmwaz.cardinal.util.document;

import com.google.common.collect.Maps;
import in.twizmwaz.cardinal.util.Numbers;
import in.twizmwaz.cardinal.util.Strings;
import lombok.NonNull;
import org.bukkit.DyeColor;
import org.jdom2.Attribute;
import org.jdom2.Element;

import java.util.Map;
import java.util.function.Function;

public class XML {

  private static Map<Class, Function<String, ?>> stringProviders = Maps.newHashMap();
  private static Map<Class, Function<Element, ?>> elementParser = Maps.newHashMap();

  static {
    stringProviders.put(String.class, (String in) -> in);
    stringProviders.put(Integer.class, Numbers::parseInteger);
    stringProviders.put(Double.class, Numbers::parseDouble);
    stringProviders.put(DyeColor.class, (String in) -> DyeColor.valueOf(Strings.getTechnicalName(in)));

    elementParser.put(String.class, Element::getText);

    /*
      TODO:
      Add Parsers:
        boolean, team, regions, vector, ProximityMetric, ProximtiyRule
     */
  }


  /**
   * Gets an object from an attribute.
   * @param attr The attribute name.
   * @param type The class you want.
   * @param <T> The type.
   * @return An object of that type if possible, null otherwise.
   */
  public static <T> T getAttribute(@NonNull Attribute attr, Class<T> type) throws Exception {
    return getFromString(attr.getValue(), type);
  }

  /**
   * Gets an object from an element's text.
   * @param el The Element.
   * @param type The class you want.
   * @param <T> The type.
   * @return An object of that type if possible, null otherwise.
   */
  public static <T> T getElementString(@NonNull Element el, Class<T> type) throws Exception {
    return getFromString(el.getText(), type);
  }

  private static <T> T getFromString(String in, Class<T> type) {
    if (in == null) {
      return null;
    }
    if (XML.stringProviders.containsKey(type)) {
      return (T) XML.stringProviders.get(type).apply(in);
    }
    throw new IllegalStateException("Missing ObjectProvider for class: " + type.getName());
  }

  /**
   * Gets an object from an element.
   * @param el The Element name.
   * @param type The class you want.
   * @param <T> The type.
   * @return An object of that type if possible, null otherwise.
   */
  public static <T> T getElementObject(@NonNull Element el, Class<T> type) throws Exception {
    if (el == null) {
      return null;
    }
    if (XML.elementParser.containsKey(type)) {
      return (T) XML.elementParser.get(type).apply(el);
    }
    throw new IllegalStateException("Missing ObjectProvider for class: " + type.getName());
  }

}