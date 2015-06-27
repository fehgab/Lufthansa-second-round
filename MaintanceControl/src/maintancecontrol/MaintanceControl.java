package maintancecontrol;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;
import java.util.*;
import org.xml.sax.SAXException;


public class MaintanceControl
{

    private static final String[] EMPTY_STRING_ARRAY = new String[0];
    
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
    

    public static void ParseXMLs()
            throws Exception
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        for (String xml : GetXMLPath())
        {
            System.out.println(xml);
            Document doc = builder.parse(new File(xml));
            String root = doc.getDocumentElement().getTagName();
            System.out.println(root);
        }
    }

    public static void main(String[] args)
            throws Exception
    {
        ParseXMLs();
    }

}
