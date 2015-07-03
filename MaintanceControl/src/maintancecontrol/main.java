package maintancecontrol;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class main {

//  Get path of the xmls from the working directory. Xmls have to be in .*xml.* directory.
    private static List<String> GetXMLPath()
            throws IOException {
        List<String> xml_pathes = new ArrayList<String>();
        File current_dir = new File(".");
        String[] dir_elements = current_dir.list();
        for (String name : dir_elements) {
            String sub_element_path = current_dir.getCanonicalPath() + "\\" + name;
            File sub_element = new File(sub_element_path);
            if (sub_element.isDirectory()) {
                if (name.toLowerCase().contains("xml")) {
                    String[] xml_files = sub_element.list();
                    for (String xml : xml_files) {
                        xml_pathes.add(sub_element_path + "\\" + xml);
                    }
                    return xml_pathes;
                }
            }
        }
        return xml_pathes;
    }

//  Call the parser functions.
    public static void ParseXMLs(ParseMasterdata pm, ParseSchedule ps)
            throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        for (String xml : GetXMLPath()) {
            System.out.println(xml);
            Document doc = builder.parse(new File(xml));
            Element elem = doc.getDocumentElement();
            String tag_name = elem.getTagName();
            if (tag_name.contains("masterdata")) {
                pm.ParseMasterdata(elem);
            }
            if (tag_name.contains("schedule")) {
                ps.ParseSchedule(elem);
            }
        }
        System.out.println();
    }

    public static void main(String[] args)
            throws Exception {
        Checker ch = new Checker();
        ParseMasterdata pm = new ParseMasterdata();
        ParseSchedule ps = new ParseSchedule();
        ParseXMLs(pm, ps);
        ch.Check("A", pm, ps);
        ch.Check("B", pm, ps);
        ch.Check("C", pm, ps);
        ch.Check("D", pm, ps);
    }
}
