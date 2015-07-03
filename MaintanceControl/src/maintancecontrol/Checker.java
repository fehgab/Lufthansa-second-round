package maintancecontrol;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

//Read the HashMaps and decide about violation.
public class Checker {

    private String check_type = "";
    private ParseMasterdata pm;
    private ParseSchedule ps;

    private double flight_hours = 0;
    private double cycles = 0;
    private double calendarDays = 0;
    private Object include = "";

    private Boolean[] checker = new Boolean[3];
    private String identifier = "";
    private String where_to_where = "";
    private String day_of_origin = "";
    private double cd = 0;
    private double f = 0;
    private int c = 0;

    private void GetMasterData() {

        Map master_values = pm.master_data.get(check_type);
        if (master_values.get("flightHours") != null) {
            flight_hours = Integer.parseInt(String.valueOf(master_values.get("flightHours")));
        }
        if (master_values.get("cycles") != null) {
            cycles = Integer.parseInt(String.valueOf(master_values.get("cycles")));
        }
        if (master_values.get("calendarDays") != null) {
            calendarDays = Integer.parseInt(String.valueOf(master_values.get("calendarDays")));
        }
        if (master_values.get("includeCheck") != null) {
            include = master_values.get("includeCheck");
        }
        if (String.valueOf(include).matches("[A-Z]")) {
            Map include_values = pm.master_data.get(include);
            if (calendarDays == 0) {
                if (include_values.get("calendarDays") != null) {
                    calendarDays = Integer.parseInt(String.valueOf(include_values.get("calendarDays")));
                }
            }
            if (cycles == 0) {
                if (include_values.get("cycles") != null) {
                    cycles = Integer.parseInt(String.valueOf(include_values.get("cycles")));
                }
            }
            if (flight_hours == 0) {
                if (include_values.get("flightHours") != null) {
                    flight_hours = Integer.parseInt(String.valueOf(include_values.get("flightHours")));
                }
            }
        }
    }

    private void GetSchedule() {
        Iterator in_sch = ps.schedule.entrySet().iterator();
        while (in_sch.hasNext()) {
            Arrays.fill(checker, Boolean.FALSE);
            Map.Entry sch_pair = (Map.Entry) in_sch.next();
            Object key = sch_pair.getKey();
            Iterator in_params = ps.schedule.get(key).entrySet().iterator();
            System.out.println(sch_pair.getKey());
            while (in_params.hasNext()) {
                Map.Entry param_pair = (Map.Entry) in_params.next();
                String param_key = String.valueOf(param_pair.getKey());
                String param_value = String.valueOf(param_pair.getValue());
                if (param_key.contains("calendar_days")) {
                    cd = Double.parseDouble(param_value);
                    if (calendarDays != 0) {
                        if (cd > calendarDays) {
                            checker[0] = Boolean.TRUE;
                        }
                    }
                }
                if (param_key.contains("flightHours")) {
                    f = Double.parseDouble(param_value);
                    if (flight_hours != 0) {
                        if (f > flight_hours) {
                            checker[1] = Boolean.TRUE;
                        }
                    }
                }
                if (param_key.contains("cycles")) {
                    c = Integer.parseInt(param_value);
                    if (cycles != 0) {
                        if (c > cycles) {
                            checker[2] = Boolean.TRUE;
                        }
                    }
                }
                if (param_key.contains("identifier")) {
                    identifier = param_value;
                }
                if (param_key.contains("where_to_where")) {
                    where_to_where = param_value;
                }
                if (param_key.contains("dayOfOrigin")) {
                    day_of_origin = param_value;
                }
            }
            Print();
        }
    }

    // Round double numbers.
    private double round(double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    private void Print() {
        if (checker[0]) {
            System.out.println("\"" + check_type + "\" Check violation for leg " + identifier + " " + where_to_where + " " + day_of_origin + ": " + String.valueOf(round(cd, 2)) + " calnedar days at landing ( limit: " + String.valueOf(calendarDays) + ")");
            System.out.println();
        }
        if (checker[1]) {
            System.out.println("\"" + check_type + "\" Check violation for leg " + identifier + " " + where_to_where + ": " + String.valueOf(round(f, 2)) + " flight hours at landing ( limit: " + String.valueOf(flight_hours) + ")");
            System.out.println();
        }
        if (checker[2]) {
            System.out.println("\"" + check_type + "\" Check violation for leg " + identifier + " " + where_to_where + ": " + String.valueOf(round(c, 2)) + " cycles at landing ( limit: " + String.valueOf(cycles) + ")");
            System.out.println();
        }
        if (!Arrays.asList(checker).contains(Boolean.TRUE)) {
            System.out.println("\"" + check_type + "\" Check OKAY!");
            System.out.println();
        }
    }

    public void Check(String type, ParseMasterdata parse_master, ParseSchedule parse_schedule) {

        check_type = type;
        pm = parse_master;
        ps = parse_schedule;
        GetMasterData();
        GetSchedule();
    }
}
