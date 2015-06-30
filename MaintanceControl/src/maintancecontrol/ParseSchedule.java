package maintancecontrol;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class ParseSchedule
{

    private static long CalculateTimeDifference(String bigger_time, String smaller_time)
            throws ParseException
    {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        Date date1 = format.parse(bigger_time);
        Date date2 = format.parse(smaller_time);
        long diff = TimeUnit.MILLISECONDS.toHours(date1.getTime() - date2.getTime());
        System.out.println(diff);
        return diff;
    }

    public static HashMap ParseSchedule(Element elem)
            throws ParseException
    {
        HashMap schedule = new HashMap<String, HashMap<String, String>>();
        HashMap params = new HashMap<String, String>();
        NodeList leg = elem.getChildNodes();
        for (int i = 0; i < leg.getLength(); i++)
        {
            Node leg_item = leg.item(i);
            if (leg_item instanceof Element == false)
            {
                continue;
            }
            NamedNodeMap leg_attributes = leg_item.getAttributes();
            String node_name = leg_item.getNodeName();
            if (node_name.contains("leg"))
            {
                String scheduled_arrive = "";
                String scheduled_departure = "";
                String actual_arrive = "";
                String actual_departure = "";
                String actual_takeoff = "";
                String actual_landing = "";
                long scheduled_hours = 0;
                long departure_hours = 0;
                long arrive_hours = 0;
                String aircraft_leg = leg_attributes.getNamedItem("aircraft").getNodeValue();
                String state = leg_attributes.getNamedItem("state").getNodeValue();
                String id = leg_attributes.getNamedItem("identifier").getNodeValue();
                String day_of_origin = leg_attributes.getNamedItem("dayOfOrigin").getNodeValue();
                String arrive = leg_attributes.getNamedItem("arr").getNodeValue();
                String departure = leg_attributes.getNamedItem("dep").getNodeValue();
                String where_to_where = departure + "-" + arrive;
                NodeList time = leg_item.getChildNodes();
                for (int k = 0; k < time.getLength(); k++)
                {
                    Node time_item = time.item(k);
                    if (time_item instanceof Element == false)
                    {
                        continue;
                    }
                    NamedNodeMap time_attributes = time_item.getAttributes();
                    String time_status = time_attributes.getNamedItem("status").getNodeValue();
                    if (time_status.contains("Scheduled"))
                    {
                        scheduled_arrive = time_attributes.getNamedItem("arr").getNodeValue();
                        scheduled_departure = time_attributes.getNamedItem("dep").getNodeValue();
                    }
                    if (time_status.contains("Actual"))
                    {
                        if (time_attributes.getNamedItem("arr") instanceof Node)
                        {
                            actual_arrive = time_attributes.getNamedItem("arr").getNodeValue();
                        }
                        if (time_attributes.getNamedItem("dep") instanceof Node)
                        {
                            actual_departure = time_attributes.getNamedItem("dep").getNodeValue();
                        }
                        if (time_attributes.getNamedItem("takeoff") instanceof Node)
                        {
                            actual_takeoff = time_attributes.getNamedItem("takeoff").getNodeValue();
                        }
                        if (time_attributes.getNamedItem("landing") instanceof Node)
                        {
                            actual_landing = time_attributes.getNamedItem("landing").getNodeValue();
                        }
                    }
                }
                if (state.contains("SKD"))
                {
                    scheduled_hours = CalculateTimeDifference(scheduled_arrive, scheduled_departure);
                }
                if (state.contains("DIV") || state.contains("DEP"))
                {
                    departure_hours = CalculateTimeDifference(scheduled_arrive, actual_departure);
                }
                if (state.contains("ARR"))
                {
                    arrive_hours = CalculateTimeDifference(actual_arrive, actual_departure);
                }

//                TODO: Dictionary-be lementeni  az adatokat
            }
            if (node_name.contains("check"))
            {
                String aircraft_check = leg_attributes.getNamedItem("aircraft").getNodeValue();
                String check_type = leg_attributes.getNamedItem("type").getNodeValue();
                String end = leg_attributes.getNamedItem("end").getNodeValue();
//                TODO: Repulonevnel nullazni a szamlalokat
            }
        }
        return schedule;
    }
}
