package maintancecontrol;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//Parse Schedule.xml and save data into hashmap.
//Hashmap structure:
//          key: aircraft_name -> value: parameter hashmap
//                                         key: parameter_name -> value: parsed xml value
//Example: {"D-AVRO":{"id":"LH400"}}
//keys and vaules have String type
public class ParseSchedule {

    public Map<String, Map<String, String>> schedule = new HashMap<>();

//    Calculate time difference between two given time, format is 14:23.
//    Returns the difference as double.
    private double CalculateTimeDifference(String bigger_time, String smaller_time)
            throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        Date date1 = format.parse(bigger_time);
        Date date2 = format.parse(smaller_time);
        long d = TimeUnit.MILLISECONDS.toMinutes(date1.getTime() - date2.getTime());
        double diff = d / 60.0;
//        System.out.println(diff);
        return diff;
    }

//    Calculate date difference between today and the given date, format is 1990-03-14.
//    Returns the difference as string. 
    private String CalculateDateDifference(String parsed_date)
            throws ParseException {
        Date today = new Date();
        SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd");
        Date date = date_format.parse(parsed_date);
        long d = TimeUnit.MILLISECONDS.toHours(today.getTime() - date.getTime());
        String diff = String.valueOf((d / 24.0));
//        System.out.println(diff);
        return diff;
    }

    public void ParseSchedule(Element elem)
            throws ParseException {
        Map<String, String> params = new HashMap<>();
        Map<String, String> temp = new HashMap<>();
        NodeList leg = elem.getChildNodes();
        for (int i = 0; i < leg.getLength(); i++) {
            Node leg_item = leg.item(i);
            if (leg_item instanceof Element == false) {
                continue;
            }
            NamedNodeMap leg_attributes = leg_item.getAttributes();
            String node_name = leg_item.getNodeName();
            if (node_name.contains("leg")) {
//              Skip RTR and CNL states.
                String state = leg_attributes.getNamedItem("state").getNodeValue();
                if (state.contains("RTR")) {
                    continue;
                }
                if (state.contains("CNL")) {
                    continue;
                }
                String scheduled_arrive = "";
                String scheduled_departure = "";
                String actual_arrive = "";
                String actual_departure = "";
                double scheduled_hours = 0;
                double departure_hours = 0;
                double arrive_hours = 0;
                String aircraft = leg_attributes.getNamedItem("aircraft").getNodeValue();
//                System.out.println(aircraft);
                String id = leg_attributes.getNamedItem("identifier").getNodeValue();
                String day_of_origin = leg_attributes.getNamedItem("dayOfOrigin").getNodeValue();
                String calendar_days = CalculateDateDifference(day_of_origin);
                String arrive = leg_attributes.getNamedItem("arr").getNodeValue();
                String departure = leg_attributes.getNamedItem("dep").getNodeValue();
                String where_to_where = departure + "-" + arrive;
                temp.put("identifier", id);
                temp.put("dayOfOrigin", day_of_origin);
                temp.put("calendar_days", calendar_days);
                temp.put("where_to_where", where_to_where);
                if (!schedule.containsKey(aircraft)) {
                    schedule.put(aircraft, temp);
                }
                NodeList time = leg_item.getChildNodes();
                for (int k = 0; k < time.getLength(); k++) {
                    Node time_item = time.item(k);
                    if (time_item instanceof Element == false) {
                        continue;
                    }
                    NamedNodeMap time_attributes = time_item.getAttributes();
                    String time_status = time_attributes.getNamedItem("status").getNodeValue();
                    if (time_status.contains("Scheduled")) {
                        scheduled_arrive = time_attributes.getNamedItem("arr").getNodeValue();
                        scheduled_departure = time_attributes.getNamedItem("dep").getNodeValue();
                    }
                    if (time_status.contains("Actual")) {
                        if (time_attributes.getNamedItem("arr") instanceof Node) {
                            actual_arrive = time_attributes.getNamedItem("arr").getNodeValue();
                        }
                        if (time_attributes.getNamedItem("dep") instanceof Node) {
                            actual_departure = time_attributes.getNamedItem("dep").getNodeValue();
                        }
                    }
                }
                Object counted_cycles = schedule.get(aircraft).get("cycles");
                Object counted_hours = schedule.get(aircraft).get("flightHours");
                if (state.contains("SKD")) {
                    scheduled_hours = CalculateTimeDifference(scheduled_arrive, scheduled_departure);
                    if (counted_hours != null) {
                        temp.put("flightHours", String.valueOf(Double.parseDouble(String.valueOf(counted_hours)) + scheduled_hours));
                    } else {
                        temp.put("flightHours", String.valueOf(scheduled_hours));
                    }

                }
                if (state.contains("DIV") || state.contains("DEP")) {
                    departure_hours = CalculateTimeDifference(scheduled_arrive, actual_departure);
                    if (counted_hours != null) {
                        temp.put("flightHours", String.valueOf(Double.parseDouble(String.valueOf(counted_hours)) + departure_hours));
                    } else {
                        temp.put("flightHours", String.valueOf(departure_hours));
                    }
                }
                if (state.contains("ARR")) {
                    arrive_hours = CalculateTimeDifference(actual_arrive, actual_departure);
                    if (counted_hours != null) {
                        temp.put("flightHours", String.valueOf(Double.parseDouble(String.valueOf(counted_hours)) + arrive_hours));
                    } else {
                        temp.put("flightHours", String.valueOf(arrive_hours));
                    }
                }
                if (counted_cycles != null) {
                    temp.put("cycles", String.valueOf(Integer.parseInt(String.valueOf(counted_cycles)) + 1));
                } else {
                    temp.put("cycles", "1");
                }
                params = temp;
                schedule.put(aircraft, params);
//                System.out.println(schedule.get(aircraft).get("flightHours"));
            }

            if (node_name.contains("check")) {
                String aircraft_check = leg_attributes.getNamedItem("aircraft").getNodeValue();
                String end_date = leg_attributes.getNamedItem("end").getNodeValue();
                if (end_date.contains(" ")) {
                    end_date = end_date.split(" ")[0];
                }
                if (schedule.containsKey(aircraft_check)) {
                    schedule.get(aircraft_check).remove("cycles");
                    schedule.get(aircraft_check).remove("flightHours");
                    schedule.get(aircraft_check).remove("calendar_days");
                }
            }
            temp = new HashMap<>();
        }
    }
}
