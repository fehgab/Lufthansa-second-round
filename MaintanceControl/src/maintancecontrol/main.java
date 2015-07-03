package maintancecontrol;


public class main {

    public static void main(String[] args)
            throws Exception {
        Checker ch = new Checker();
        ParseMasterdata pm = new ParseMasterdata();
        ParseSchedule ps = new ParseSchedule();
        ch.ParseXMLs(pm, ps);
        ch.Check("A");
        ch.Check("B");
        ch.Check("C");
        ch.Check("D");
    }
}
