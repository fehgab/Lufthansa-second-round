package maintancecontrol;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class Checker
{
    ParseMasterdata parse_master;
    ParseSchedule parse_schedule;

    private List<String> GetXMLPath()
            throws IOException
    {
        List<String> xml_pathes = new ArrayList<String>();
        File current_dir = new File(".");
        String[] dir_elements = current_dir.list();
        for (String name : dir_elements)
        {
            String sub_element_path = current_dir.getCanonicalPath() + "\\" + name;
            File sub_element = new File(sub_element_path);
            if (sub_element.isDirectory())
            {
                if (name.toLowerCase().contains("xml"))
                {
                    String[] xml_files = sub_element.list();
                    for (String xml : xml_files)
                    {
                        xml_pathes.add(sub_element_path + "\\" + xml);
                    }
                    return xml_pathes;
                }
            }
        }
        return xml_pathes;
    }

    public void ParseXMLs(ParseMasterdata pm, ParseSchedule ps)
            throws Exception
    {
        parse_master = pm;
        parse_schedule = ps;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        for (String xml : GetXMLPath())
        {
            System.out.println(xml);
            Document doc = builder.parse(new File(xml));
            Element elem = doc.getDocumentElement();
            String tag_name = elem.getTagName();
            if (tag_name.contains("masterdata"))
            {
                pm.ParseMasterdata(elem);
            }
            if (tag_name.contains("schedule"))
            {
                ps.ParseSchedule(elem);
            }
        }
        System.out.println();
    }
    
    
    
    public void Check(String type)
    {
        Boolean[] checker = new Boolean[3];
        Arrays.fill(checker, Boolean.FALSE);
        int flight_hours = 0;
        int cycles = 0;
        int calendarDays = 0;
        Object include = "";
        Map master_values = parse_master.master_data.get(type);
        if(master_values.get("flightHours") != null)
            flight_hours = Integer.parseInt(String.valueOf(master_values.get("flightHours")));
        if(master_values.get("cycles") != null)
            cycles = Integer.parseInt(String.valueOf(master_values.get("cycles")));
        if(master_values.get("calendarDays") != null)
            calendarDays = Integer.parseInt(String.valueOf(master_values.get("calendarDays")));
        if(master_values.get("includeCheck") != null)
            include = master_values.get("includeCheck");
        if (String.valueOf(include).matches("[A-Z]"))
        {
            Map include_values = parse_master.master_data.get(include);
            if (calendarDays == 0)
                if(include_values.get("calendarDays") != null)
                    calendarDays = Integer.parseInt(String.valueOf(include_values.get("calendarDays")));
            if (cycles == 0)
                if(include_values.get("cycles") != null)
                    cycles = Integer.parseInt(String.valueOf(include_values.get("cycles")));
            if (flight_hours == 0)
                if(include_values.get("flightHours") != null)
                    flight_hours = Integer.parseInt(String.valueOf(include_values.get("flightHours")));
        }
        Iterator in_sch = parse_schedule.schedule.entrySet().iterator();
        while (in_sch.hasNext())
        {
            String identifier = "";
            String where_to_where = "";
            String day_of_origin = "";
            int cd = 0;
            int s = 0;
            int c = 0;
            Arrays.fill(checker, Boolean.FALSE);
            Map.Entry sch_pair = (Map.Entry)in_sch.next();
            Object key = sch_pair.getKey();
            Iterator in_params = parse_schedule.schedule.get(key).entrySet().iterator();
            System.out.println(sch_pair.getKey() + " = " + sch_pair.getValue());
            while (in_params.hasNext())
            {
                Map.Entry param_pair = (Map.Entry)in_params.next();
                String param_key = String.valueOf(param_pair.getKey());
                String param_value = String.valueOf(param_pair.getValue());
                if (param_key.contains("calendar_days"))
                {
                    cd = Integer.parseInt(param_value);
                    if (calendarDays != 0)
                        if(cd > calendarDays)
                        {
                           checker[0] = Boolean.TRUE; 
                        }
                }
                if (param_key.contains("state"))
                {
                    s = Integer.parseInt(param_value);
                    if (flight_hours != 0)
                        if(s > flight_hours)
                        {
                            checker[1] = Boolean.TRUE;
                        }
                }
                if (param_key.contains("cycle"))
                {
                   c =  Integer.parseInt(param_value);
                   if (cycles != 0)
                        if(c > cycles)
                        {
                            checker[2] = Boolean.TRUE;
                        }
                }
                if (param_key.contains("identifier"))
                    identifier = param_value;
                if (param_key.contains("where_to_where"))
                    where_to_where = param_value;
                if (param_key.contains("dayOfOrigin"))
                    day_of_origin = param_value;
            }   
            if(checker[0])
            {
                System.out.println("\"" + type + "\" Check violation for leg " + identifier + " " + where_to_where + " " + day_of_origin + ": " + String.valueOf(cd) + " calnedar days at landing ( limit: " + String.valueOf(calendarDays) + ")");   
                System.out.println();
            }
            if(checker[1])
            {
                System.out.println("\"" + type + "\" Check violation for leg " + identifier + " " + where_to_where + ": " + String.valueOf(s) + " flight hours at landing ( limit: " + String.valueOf(flight_hours) + ")");   
                System.out.println();
            }
            if(checker[2])
            {
                System.out.println("\"" + type + "\" Check violation for leg " + identifier + " " + where_to_where + ": " + String.valueOf(c) + " cycles at landing ( limit: " + String.valueOf(cycles) + ")");   
                System.out.println();
            }
            if (!Arrays.asList(checker).contains(Boolean.TRUE))
            {
                System.out.println("\"" + type + "\" Check OKAY!");
                System.out.println();
            }
        }
    }
}
