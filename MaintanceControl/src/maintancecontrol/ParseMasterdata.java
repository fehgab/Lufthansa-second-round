package maintancecontrol;

import java.util.HashMap;
import java.util.Map;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

//Parse MasterData.xml and save data into hashmap.
//Hashmap structure:
//          key: check_type -> value: parameter hashmap
//                                         key: parameter_name -> value: parsed xml value
//Example: {"B":{"calendarDays":"180"}}
//keys and values have String type
public class ParseMasterdata {

    public Map<String, Map<String, String>> master_data = new HashMap<>();

    public void ParseMasterdata(Element elem) {
        Map<String, String> params = new HashMap<>();
        NodeList checks = elem.getChildNodes();
        for (int j = 0; j < checks.getLength(); j++) {
            if (checks.item(j) instanceof Element == false) {
                continue;
            }
            NodeList check = checks.item(j).getChildNodes();
            for (int i = 0; i < check.getLength(); i++) {
                String node_name = check.item(i).getNodeName();
                if (node_name.matches("^check$")) {
                    String type = check.item(i).getAttributes().getNamedItem("type").getNodeValue();
//                    System.out.println(type);
                    NodeList parameters = check.item(i).getChildNodes();
                    for (int k = 0; k < parameters.getLength(); k++) {
                        NodeList trigger_nodes = parameters.item(k).getChildNodes();
                        for (int z = 0; z < trigger_nodes.getLength(); z++) {
                            if (trigger_nodes.item(z) instanceof Element == false) {
                                continue;
                            }
                            String name = trigger_nodes.item(z).getNodeName();
                            String data = trigger_nodes.item(z).getTextContent();
                            if (name.endsWith("Check")) {
                                data = trigger_nodes.item(z).getAttributes().getNamedItem("type").getNodeValue();
                            }
//                            System.out.println(data);
                            params.putIfAbsent(name, data);
                        }
                    }
                    master_data.putIfAbsent(type, params);
                    params = new HashMap<String, String>();
                }
            }
        }
    }
}
