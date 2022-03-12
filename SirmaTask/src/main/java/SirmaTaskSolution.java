import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Pattern;

public class SirmaTaskSolution {

    private static final String DELIMITER = ",";
    private static final int NUMBER_OF_CSV_ELEMENTS = 4;
    private static final Pattern DATE_PATTERN = Pattern.compile("^((19|2[0-9])[0-9]{2})[-/](0[1-9]|1[012])[-/](0[1-9]|[12][0-9]|3[01])$");
    private static final Pattern FEBRUARY_MONTH_PATTERN = Pattern.compile("^(((19|2[0-9])[0-9]{2})[-/]02[-/](0[1-9]|1[0-9]|2[0-8]))$");
    private static final Pattern INTEGER_PATTERN = Pattern.compile("^\\d+$");
    private static final int MAX_NUMBER_OF_EMPLOYEES_ON_SAME_PROJECT = 2;
    private static final int EMPLOYEE_ID_INDEX = 0;
    private static final int PROJECT_ID_INDEX = 1;
    private static final int DATE_FROM_INDEX = 2;
    private static final int DATE_TO_INDEX = 3;

    public static void main(String[] args) {

        Map<Integer, List<Employee>> projectEmployees = new HashMap<>();
        List<String[]> CSVElements = new ArrayList<>();
        HashSet<Integer> employeesIds = new HashSet<>();

        try {
            FileReader fileReader = new FileReader("myFile.CSV");
            CSVReader reader = new CSVReader(fileReader);
            reader.readNext(); //skipping first line
            String[] line = reader.readNext(); //outside the loop, because reader.readNext() should be variable
            while (line != null) {
                CSVElements.add(line); //store the CSV file lines -> O(1) time complexity
                line = reader.readNext();
            }

            for (String[] s : CSVElements) {
                if (s.length < NUMBER_OF_CSV_ELEMENTS || s.length > NUMBER_OF_CSV_ELEMENTS) {//check for valid CSV format
                    throw new RuntimeException("Wrong CSV format");
                }

                checkForInvalidIntegerInput(s[EMPLOYEE_ID_INDEX]);
                Employee e = new Employee();
                int employeeId = Integer.parseInt(s[EMPLOYEE_ID_INDEX]);
                if (employeeId <= 0) { //check for wrong employeeId input
                    throw new RuntimeException("Wrong credentials");
                }

                e.setId(employeeId);
                employeesIds.add(employeeId);//for the sample output, Set Data Structure give us unique elements
                checkForInvalidIntegerInput(s[PROJECT_ID_INDEX]);

                int projectId = Integer.parseInt(s[PROJECT_ID_INDEX].trim());
                if (projectId <= 0) { //check for wrong projectId input
                    throw new RuntimeException("Wrong credentials");
                }
                e.setProjectId(projectId);
                if (s[DATE_FROM_INDEX].trim().equalsIgnoreCase("NULL") || !s[DATE_FROM_INDEX].trim().matches(DATE_PATTERN.pattern())) {
                    throw new RuntimeException("Wrong credentials");
                }

                String newDateFrom = changeDateDelimiter(s[DATE_FROM_INDEX]);
                checkForInvalidFebruaryMonth(newDateFrom);
                LocalDate dateFrom = LocalDate.parse(newDateFrom.trim());
                e.setDateFrom(dateFrom);

                if (!s[DATE_TO_INDEX].trim().equalsIgnoreCase("NULL")) {
                    if (!s[DATE_TO_INDEX].trim().matches(DATE_PATTERN.pattern())) {
                        throw new RuntimeException("Wrong date format");
                    }
                    String newDateTo = changeDateDelimiter(s[DATE_TO_INDEX]);
                    checkForInvalidFebruaryMonth(newDateTo);
                    e.setDateTo(LocalDate.parse(newDateTo.trim()));
                } else {
                    e.setDateTo(LocalDate.now());
                }

                if (e.getDateFrom().isAfter(e.getDateTo())) {
                    throw new RuntimeException("Wrong date credentials");
                }
                groupEmployeesByProjectId(e, projectEmployees);
            }

            printProjectsInfo(projectEmployees);
            printEmployeesIds(employeesIds);
        } catch (IOException | CsvException e) {
            System.out.println("File not found");
        }
    }

    private static void groupEmployeesByProjectId(Employee e, Map<Integer, List<Employee>> employees) {
        if (!employees.containsKey(e.getProjectId())) {
            employees.put(e.getProjectId(), new ArrayList<>());//O(1) time complexity
        }
        employees.get(e.getProjectId()).add(e);//O(1) time complexity
        if (employees.get(e.getProjectId()).size() > MAX_NUMBER_OF_EMPLOYEES_ON_SAME_PROJECT) {
            throw new RuntimeException("Maximum two employees can work on same project");
        }
    }

    private static long getAllWorkingDaysOnProject(Map<Integer, List<Employee>> employees, int projectId) {
        List<LocalDate> dateFromList = new ArrayList<>();
        List<LocalDate> dateToList = new ArrayList<>();
        for (Employee emp : employees.get(projectId)) {
            dateFromList.add(emp.getDateFrom());//O(1) time complexity
        }
        for (Employee emp : employees.get(projectId)) {
            dateToList.add(emp.getDateTo());//O(1) time complexity
        }
        LocalDate minDate = Collections.min(dateFromList);
        LocalDate maxDate = Collections.max(dateToList);
        return ChronoUnit.DAYS.between(minDate, maxDate);
    }

    private static void printProjectsInfo(Map<Integer, List<Employee>> employees) {
        for (Map.Entry<Integer, List<Employee>> emp : employees.entrySet()) {
            int counter = 0;
            for (Employee e : emp.getValue()) {
                System.out.print("EmployeeID: " + e.getId());
                if (counter != emp.getValue().size()) {
                    System.out.print(DELIMITER);
                }
                counter++;
            }
            System.out.print("ProjectID: " + emp.getKey() + DELIMITER + getAllWorkingDaysOnProject(employees, emp.getKey()) + " days worked.");
            System.out.println();
        }
    }

    private static void printEmployeesIds(HashSet<Integer> employeesIds) {
        int counter = 0;
        for (Integer i : employeesIds) {
            System.out.print(i);
            if (counter != employeesIds.size() - 1) {
                System.out.print(DELIMITER);
            }
            counter++;
        }
    }

    private static void checkForInvalidFebruaryMonth(String date) {
        String[] elements = date.split("-");
        String monthPosition = elements[1];
        if (monthPosition.equals("02")) {
            if (!date.trim().matches(FEBRUARY_MONTH_PATTERN.pattern())) {
                throw new RuntimeException("Wrong date format");
            }
        }
    }

    private static void checkForInvalidIntegerInput(String input) {
        if (!input.trim().matches(INTEGER_PATTERN.pattern())) {
            throw new RuntimeException("Wrong integer format");
        }
    }

    private static String changeDateDelimiter(String date) {
        return date.replace("/", "-");
    }
}