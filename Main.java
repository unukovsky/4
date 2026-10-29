import java.io.*;
import java.util.*;

class GradeBook {
    private static List<GradeBook> students = new ArrayList<>();

    private String lastName;
    private String firstName;
    private int course;
    private String group;
    private List<Session> sessions;

    public GradeBook(String lastName, String firstName, int course, String group) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.course = course;
        this.group = group;
        this.sessions = new ArrayList<>();
    }
    class Session {
        private int sessionNumber;
        Map<String, Integer> exams = new HashMap<>();
        Map<String, String> quizzes = new HashMap<>();

        public Session(int sessionNumber) {
            this.sessionNumber = sessionNumber;
        }

        public int getSessionNumber() { return sessionNumber; }
        public void addExam(String subject, int grade) { exams.put(subject, grade); }
        public void addQuiz(String subject, String status) { quizzes.put(subject, status); }

        public double getAverage() {
            if (exams.isEmpty()) return 0.0;
            double sum = 0;
            List<Integer> grades = new ArrayList<>(exams.values());
            for (int i = 0; i < grades.size(); i++) {
                sum += grades.get(i);
            }
            return sum / exams.size();
        }
    }
    public Session getOrCreateSession(int sessionNumber) {
        for (int i = 0; i < sessions.size(); i++) {
            Session s = sessions.get(i);
            if (s.getSessionNumber() == sessionNumber) {
                return s;
            }
        }
        Session newSession = new Session(sessionNumber);
        sessions.add(newSession);
        return newSession;
    }

    public boolean check1() {
        if (sessions.isEmpty()) return false;
        boolean hasExamsOrQuizzes = false;

        for (int i = 0; i < sessions.size(); i++) {
            Session session = sessions.get(i);

            List<String> quizStatuses = new ArrayList<>(session.quizzes.values());
            for (int j = 0; j < quizStatuses.size(); j++) {
                hasExamsOrQuizzes = true;
                if (!quizStatuses.get(j).equalsIgnoreCase("Зачет")) return false;
            }

            List<Integer> examGrades = new ArrayList<>(session.exams.values());
            for (int j = 0; j < examGrades.size(); j++) {
                hasExamsOrQuizzes = true;
                if (examGrades.get(j) < 9) return false;
            }
        }
        return hasExamsOrQuizzes;
    }

    public boolean check2() {
        if (sessions.isEmpty()) return false;

        for (int i = 0; i < sessions.size(); i++) {
            Session session = sessions.get(i);

            List<String> quizStatuses = new ArrayList<>(session.quizzes.values());
            for (int j = 0; j < quizStatuses.size(); j++) {
                if (!quizStatuses.get(j).equalsIgnoreCase("Зачет")) return false;
            }

            List<Integer> examGrades = new ArrayList<>(session.exams.values());
            for (int j = 0; j < examGrades.size(); j++) {
                if (examGrades.get(j) < 4) return false;
            }
        }
        return true;
    }

    public String getName() {
        return lastName + " " + firstName;
    }

    public int getCourse() { return course; }
    public String getGroup() { return group; }
    public List<Session> getSessions() { return sessions; }

    private static GradeBook findOrCreateStudent(String fullName, int course, String group) {
        for (int i = 0; i < students.size(); i++) {
            GradeBook student = students.get(i);
            if (student.getName().equalsIgnoreCase(fullName)) {
                return student;
            }
        }
        String[] parts = fullName.split(" ");
        String lastName = parts.length > 0 ? parts[0] : "";
        String firstName = parts.length > 1 ? parts[1] : "";

        GradeBook newStudent = new GradeBook(lastName, firstName, course, group);
        students.add(newStudent);
        return newStudent;
    }

    public static void loadStudentsData(String fileName) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] data = line.split(";");
                String fullName = data[0].trim();
                int course = Integer.parseInt(data[1].trim());
                String group = data[2].trim();
                int sessionNum = Integer.parseInt(data[3].trim());
                String type = data[4].trim();
                String subject = data[5].trim();
                String value = data[6].trim();

                GradeBook student = findOrCreateStudent(fullName, course, group);
                GradeBook.Session session = student.getOrCreateSession(sessionNum);

                if (type.equalsIgnoreCase("Экзамен")) {
                    session.addExam(subject, Integer.parseInt(value));
                } else if (type.equalsIgnoreCase("Зачет")) {
                    session.addQuiz(subject, value);
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка!");
        }
    }

    public static void ExcellentStudentsFile(String fileName) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(fileName))) {
            for (int i = 0; i < students.size(); i++) {
                GradeBook student = students.get(i);
                if (student.check1()) {
                    String[] parts = student.getName().split(" ");
                    String ln = parts[0];
                    String fn = parts.length > 1 ? parts[1] : "";

                    List<GradeBook.Session> studentSessions = student.getSessions();
                    for (int j = 0; j < studentSessions.size(); j++) {
                        GradeBook.Session session = studentSessions.get(j);

                        List<Map.Entry<String, Integer>> examEntries = new ArrayList<>(session.exams.entrySet());
                        for (int k = 0; k < examEntries.size(); k++) {
                            Map.Entry<String, Integer> exam = examEntries.get(k);
                            pw.printf("%s, %s, Курс: %d, Группа: %s, Сессия: %d, Предмет: %s, Оценка: %d%n", ln, fn, student.getCourse(), student.getGroup(), session.getSessionNumber(), exam.getKey(), exam.getValue());
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка!");
        }
    }

    public static void SuccessfulStudentsFile(String fileName) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(fileName))) {
            for (int i = 0; i < students.size(); i++) {
                GradeBook student = students.get(i);
                if (student.check2()) {
                    pw.printf("Студент: %s  Курс: %d  Группа: %s%n",
                            student.getName(), student.getCourse(), student.getGroup());
                    List<GradeBook.Session> studentSessions = student.getSessions();
                    for (int j = 0; j < studentSessions.size(); j++) {
                        GradeBook.Session session = studentSessions.get(j);
                        pw.printf(" Сессия №%d  Средний балл: %.2f%n",
                                session.getSessionNumber(), session.getAverage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка!");
        }
    }
}

public class Main {
   public static void main(String[] args)
    {
        GradeBook.loadStudentsData("students_data.txt");
        GradeBook.ExcellentStudentsFile("excellent_students.txt");
        GradeBook.SuccessfulStudentsFile("successful_students.txt");
    }
}
