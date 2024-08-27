import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/** Класс, куда записываются упражнения и их вес*/
class Exercise {
    String name;
    int weight;
    public Exercise(String name, int weight) {
        this.name = name;
        this.weight = weight;
    }
    public void setWeight(int weight) {
        this.weight = weight;
    }

    @Override
    public String toString() {
        return name + " (Вес: " + weight + " кг)";
    }
}

/** Класс, куда записываются мышцы, необходимое количество подходов, и их выполненное кол-во*/
class MuscleGroup {
    String name;
    int weeklySets;
    int completedSets;

    public MuscleGroup(String name, int sets) {
        this.name = name;
        this.weeklySets = sets;
        this.completedSets = 0;
    }

    @Override
    public String toString() {
        return name + " (Подходы в неделю: " + weeklySets + ", Выполнено: " + completedSets + ")";
    }




    public void addCompletedSet() {
        completedSets++;
    }
    public void subtractCompletedSet() {
        completedSets--;
    }
}

/**
 * Все манипуляции через интерфес с данными, осуществляем через этот класс
 */
class Workout {
    private ArrayList<MuscleGroup> muscleGroups;
    private ArrayList<Exercise> exercises;

    public ArrayList<Exercise> getExercisesList() {
        return exercises;
    }

    public Workout() {
        muscleGroups = new ArrayList<>();
        exercises = new ArrayList<>();
    }

    public void addMuscleGroup(String name, int sets) {
        muscleGroups.add(new MuscleGroup(name, sets));
    }

    public void addExercise(String name, int weight) {
        exercises.add(new Exercise(name, weight));
    }

    public void setExerciseWeight(String name, int weight) {
        for (Exercise ex : exercises) {
            if (ex.name.equals(name)) {
                ex.setWeight(weight);
                return;

            }
        }
    }

    public String getMuscleGroups() {
        StringBuilder sb = new StringBuilder();
        for (MuscleGroup mg : muscleGroups) {
            sb.append(mg).append("\n");
        }
        return sb.toString();
    }

    public String getExercises() {
        StringBuilder sb = new StringBuilder();
        for (Exercise ex : exercises) {
            sb.append(ex).append("\n");
        }
        return sb.toString();
    }


    /* TODO Переделать так, чтобы метод добавлял подход
        к мышечной группе по нажатию кнопки
        рядом с наименованием этой мышечной группы */

    /**
     * Добавляет подход к выбранной мышечной группе
     * @param indexMuscleGroup принимает индекс мышечной группы: <br>
     * [0] Грудные мышцы <br>
     * [1] Спина <br>
     * [2] Ноги <br>
     * [3] Пресс <br>
     * [4] Трицепс <br>
     * [5] Бицепс <br>
     * [6] Плечи <br>
     * [7] Шея <br>
     */
    public void addSet (int indexMuscleGroup) {


        // TODO updateDataOnScreen


    }
}

/** Интерфейс программы */
public class WorkoutApp extends JFrame {
    private Workout workout;
    private JTextArea muscleGroupArea;
    private JTextArea exerciseArea;
    private JTextField muscleGroupInput;

    public WorkoutApp() {
        workout = new Workout();

        // Инициализация мышечных групп и упражнений
        loadMuscleGroupsFromFile();
        loadExercisesFromFile();

        // Настройка UI
        setTitle("Deu sEX machina!");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        muscleGroupArea = new JTextArea();
        exerciseArea = new JTextArea();
        muscleGroupInput = new JTextField();

        JButton addSetButton = new JButton("Добавить подход");

        addSetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String input = muscleGroupInput.getText();
                workout.addSet(Integer.parseInt(input));
                JOptionPane.showMessageDialog(null, "Добавлен подход к " + input + ".");
                muscleGroupInput.setText("");
                muscleGroupArea.setText(workout.getMuscleGroups());
            }
        });

        // Добавление заголовка первой колонки
        JPanel panel = new JPanel(new GridLayout(1, 1));
        panel.add(new JLabel("Необходимо выполнить"));

        add(panel, BorderLayout.NORTH);
        add(new JScrollPane(muscleGroupArea), BorderLayout.WEST);
        add(new JScrollPane(exerciseArea), BorderLayout.CENTER);
        add(muscleGroupInput, BorderLayout.SOUTH);
        add(addSetButton, BorderLayout.EAST);

        // Заполнение текстовых областей
        muscleGroupArea.setText(workout.getMuscleGroups());
        exerciseArea.setText(workout.getExercises());

        setVisible(true);

        // Загружаем веса для упражнений
        loadWeights();



    }


    //todo у меня здесь идёт 4 метода, некоторые из которых лишние. надо убрать лишние
    /**
     * Загружает мышечные группы из файла.
     *
     */
    private void loadMuscleGroupsFromFile() {
            try (BufferedReader br = new BufferedReader(new FileReader("muscle_groups.txt"))) {
                String line;
//                List<Integer> sets = new ArrayList<>();
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split(":");
                    workout.addMuscleGroup(parts[0],Integer.parseInt(parts[1]));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
    }


    private void loadExercisesFromFile() {
        try (BufferedReader br = new BufferedReader(new FileReader("exercises.txt"))) {
            String line;
//            List<Integer> sets = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(":");
                workout.addExercise(parts[0],Integer.parseInt(parts[1]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }





    /**
     * Загрузка данных о весе в упражнениях
     * <br><br>
     *  Если файл существует - достаём данные из файла <br>
     *  Если файла нет - запрашиваем данные у пользователя, создаём файл, и записываем в файл эти данные
     */

    private void loadWeights() {
        File file = new File("exercises.txt");

        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(":");
                    if (parts.length == 2) {
                        String exerciseName = parts[0];
                        int weight = Integer.parseInt(parts[1].trim());
                        workout.setExerciseWeight(exerciseName, weight);
                    }
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Ошибка при загрузке данных веса в упражнениях: " + e.getMessage());
            }
        } else {
            for (Exercise exercise : workout.getExercisesList()) {
                String input = JOptionPane.showInputDialog("Введите вес в кг для упражнения \"" + exercise.name + "\" (при 8 повторениях):");
                try {
                    if (input != null) {
                        int weight = Integer.parseInt(input);
                        workout.setExerciseWeight(exercise.name, weight);
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Введите корректное число.");
                }
            }
           // createWeightFile();
        }

        // Обновляем отображение упражнений с учетом введенного веса
        exerciseArea.setText(workout.getExercises());
    }
/*
    private void createWeightFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("weight.txt"))) {
            for (Exercise exercise : workout.getExercisesList()) {
                writer.write(exercise.name + ": " + exercise.weight);
                writer.newLine();
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Ошибка при сохранении данных о весе в упражнениях: " + e.getMessage());
        }
    }*/
/* todo приложение сейчас, при неправильных данных файлов упражнений и мышечных групп
    не запускается, и даже не отображает ошибку. надо хотя бы ошибку сделать
 */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new WorkoutApp());
    }
}

