package maintancecontrol;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import static maintancecontrol.ParseMasterdata.ParseMasterdata;
import static maintancecontrol.ParseSchedule.ParseSchedule;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class MaintanceControl
{

    private static List<String> GetXMLPath()
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
            Element elem = doc.getDocumentElement();
            String tag_name = elem.getTagName();
            if (tag_name.contains("masterdata"))
            {
                master_data = ParseMasterdata(elem);
            }
            if (tag_name.contains("schedule"))
            {
                schedule = ParseSchedule(elem);
            }
        }
    }

    public static void main(String[] args)
            throws Exception
    {
        ParseXMLs();
    }

}
