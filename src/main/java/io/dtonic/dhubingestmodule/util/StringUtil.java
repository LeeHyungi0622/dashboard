package io.dtonic.dhubingestmodule.util;

import io.dtonic.dhubingestmodule.common.code.Constants;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility for string type
 * @FileName StringUtil.java
 * @Project citydatahub_datacore_ui
 * @Brief
 * @Version 1.0
 * @Date 2022. 3. 25.
 * @Author Elvin
 */
public class StringUtil {

  /**
   * Convert camel type to db style
   * @param str
   * @return
   */
  public static String camelToDbStyle(String str) {
    String regex = "([A-Z])";
    String replacement = Constants.COLUMN_DELIMITER + "$1";
    return (
      str.substring(0, 1).toLowerCase() +
      str
        .substring(1, str.length())
        .replaceAll(regex, replacement)
        .toLowerCase()
    );
  }

  /**
   * Convert array string to db style
   * @param strList
   * @return
   */
  public static String arrayStrToDbStyle(List<String> strList) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < strList.size(); i++) {
      if (i > 0) sb.append(Constants.COLUMN_DELIMITER);
      sb.append(strList.get(i));
    }

    return sb.toString();
  }

  /**
   * Get the hierarchy of attribute IDs.
   * @param attributeId
   * @return
   */
  public static List<String> getHierarchyAttributeIds(String attributeId) {
    List<String> hierarchyAttributeIds = new ArrayList<>();

    if (attributeId.contains("\\[")) {
      String[] attributeIdArr = attributeId.split("\\[");
      for (String id : attributeIdArr) {
        hierarchyAttributeIds.add(id.replaceAll("]", ""));
      }
    } else if (attributeId.contains("\\.")) {
      String[] attributeIdArr = attributeId.split("\\.");
      for (String id : attributeIdArr) {
        hierarchyAttributeIds.add(id.replaceAll("\\.", ""));
      }
    } else {
      hierarchyAttributeIds.add(attributeId);
    }
    return hierarchyAttributeIds;
  }
}
