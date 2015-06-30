package maintancecontrol;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;


public class MaintanceControl
{
    
    private static List<String> GetXMLPath()    
            throws IOException
    {
        List<String> xml_pathes = new ArrayList<String>();
        File current_dir = new File(".");
        String[] dir_elements = current_dir.list();
        for(String name : dir_elements)
        {
            String sub_element_path = current_dir.getCanonicalPath() + "\\" + name;
            File sub_element = new File(sub_element_path);
            if (sub_element.isDirectory())
            {
                if(name.toLowerCase().contains("xml"))
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
    
    private static HashMap ParseMasterdata(Document doc)
    {
        HashMap master_data = new HashMap<String, HashMap<String, String>>();
        HashMap params = new HashMap<String,String>();
        NodeList checks = doc.getDocumentElement().getChildNodes();
        for(int j=0; j<checks.getLength();j++)
        {
            if (checks.item(j) instanceof Element == false)
                continue;
            NodeList check = checks.item(j).getChildNodes();
            for (int i=0; i<check.getLength(); i++)
            {
                String node_name = check.item(i).getNodeName();
                if( node_name.matches("^check$"))
                {
                    String type = check.item(i).getAttributes().getNamedItem("type").getNodeValue();
                    System.out.println(type);
                    NodeList parameters = check.item(i).getChildNodes();
                    for (int k=0; k<parameters.getLength();k++)
                    {  
                        NodeList trigger_nodes = parameters.item(k).getChildNodes();
                        for (int z=0; z<trigger_nodes.getLength();z++)
                        {
                            if (trigger_nodes.item(z) instanceof Element == false)
                                continue;
                            String name = trigger_nodes.item(z).getNodeName();
                            String data = trigger_nodes.item(z).getTextContent();
                            if (name.endsWith("Check"))
                            {
                                data = trigger_nodes.item(z).getAttributes().getNamedItem("type").getNodeValue();
                            }
                            System.out.println(data);
                            params.putIfAbsent(name, data);
                        }
                    }
                    master_data.putIfAbsent(type, params);
                    params = new HashMap<String,String>();
                }
            }
        }
        return master_data;
    }
    
    private static HashMap ParseSchedule(Document doc)
            throws ParseException
    {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        HashMap schedule = new HashMap<String, HashMap<String, String>>();
        HashMap params = new HashMap<String, String>();
        NodeList leg = doc.getDocumentElement().getChildNodes();
        for (int i=0; i<leg.getLength(); i++)
        {
            Node leg_item = leg.item(i);
            if (leg_item instanceof Element == false)
                continue;
            NamedNodeMap leg_attributes = leg_item.getAttributes();
            String node_name = leg_item.getNodeName();
            if (node_name.contains("leg"))
            {
                String aircraft_leg = leg_attributes.getNamedItem("aircraft").getNodeValue();
                String state = leg_attributes.getNamedItem("state").getNodeValue();
                String id = leg_attributes.getNamedItem("identifier").getNodeValue();
                String day_of_origin = leg_attributes.getNamedItem("dayOfOrigin").getNodeValue();
                String arrive = leg_attributes.getNamedItem("arr").getNodeValue();
                String departure = leg_attributes.getNamedItem("dep").getNodeValue();
                String where_to_where = departure + "-" + arrive;
                String scheduled_arrive = "";
                String scheduled_departure = "";
                String actual_arrive = "";
                String actual_departure = "";
                String actual_takeoff = "";
                String actual_landing = "";
                NodeList time = leg_item.getChildNodes();
                for(int k=0; k<time.getLength();k++)
                {
                    Node time_item = time.item(k);
                    if (time_item instanceof Element == false)
                        continue;
                    NamedNodeMap time_attributes = time_item.getAttributes();
                    String time_status = time_attributes.getNamedItem("status").getNodeValue();
                    if(time_status.contains("Scheduled"))
                    {
                        scheduled_arrive = time_attributes.getNamedItem("arr").getNodeValue();
                        scheduled_departure = time_attributes.getNamedItem("dep").getNodeValue();
                    }
                    if(time_status.contains("Actual"))
                    {
                        try
                        {
                            actual_arrive = time_attributes.getNamedItem("arr").getNodeValue();
                            actual_departure = time_attributes.getNamedItem("dep").getNodeValue();
                            actual_takeoff = time_attributes.getNamedItem("takeoff").getNodeValue();
                            actual_landing = time_attributes.getNamedItem("landing").getNodeValue();
                        }
                        catch(NullPointerException e){}
                    }
                }
                if (state.contains("SKD"))
                {
                    Date date1 = format.parse(scheduled_arrive);
                    Date date2 = format.parse(scheduled_departure);
                    long difference = date1.getTime() - date2.getTime();
                    long hours = TimeUnit.MILLISECONDS.toHours(difference); 
                    System.out.println(hours);
                }        
                if (state.contains("DIV") || state.contains("DEP"))
                {

                }
                if (state.contains("ARR"))
                {

                }
                
//                TODO: Dictionary-be lementeni  az adatokat
            }
            if (node_name.contains("check"))
            {
                String aircraft_check = leg.item(i).getAttributes().getNamedItem("aircraft").getNodeValue();
                String check_type = leg.item(i).getAttributes().getNamedItem("type").getNodeValue();
                String end = leg.item(i).getAttributes().getNamedItem("end").getNodeValue();
//                TODO: Repulonevnel nullazni a szamlalokat
            }
        }
        return schedule;
    }

    public static void ParseXMLs()
            throws Exception
    {
        HashMap master_data = new HashMap<String, HashMap<String, String>>();
        HashMap schedule = new HashMap<String, HashMap<String, String>>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        for (String xml : GetXMLPath())
        {
            System.out.println(xml);
            Document doc = builder.parse(new File(xml));
            String tag_name = doc.getDocumentElement().getTagName();
            if (tag_name.contains("masterdata"))
                master_data = ParseMasterdata(doc);
            if (tag_name.contains("schedule"))
                schedule = ParseSchedule(doc);
        }
    }

    public static void main(String[] args)
            throws Exception
    {
        ParseXMLs();
    }

}
