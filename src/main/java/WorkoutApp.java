import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

class Exercise {
    String name;
    double standardWeight; // Обычный вес
    double currentWeight; // Текущий вес

    public Exercise(String name, double weight) {
        this.name = name;
        this.standardWeight = weight;
        this.currentWeight = weight; // Начальный вес совпадает со стандартным
    }

    public void setWeight(double weight) {
        this.currentWeight = weight;
    }

    public double getStandardWeight() {
        return standardWeight;
    }

    public void increaseWeight() {
        standardWeight *= 1.05; // Увеличение стандартного веса на 5%
    }

    public double getReducedWeight() {
        return standardWeight * 0.7; // Уменьшение на 30% от стандартного
    }

    public void resetWeight() {
        currentWeight = standardWeight; // Возвращаем текущий вес к стандартному
    }

    @Override
    public String toString() {
        return name + " (Стандартный вес: " + String.format("%.2f", standardWeight) + " кг, Текущий вес: " + String.format("%.2f", currentWeight) + " кг)";
    }
}

class MuscleGroup {
    String name;
    int weeklySets;
    int completedSets;

    public MuscleGroup(String name, int sets) {
        this.name = name;
        this.weeklySets = sets;
        this.completedSets = 0;
    }

    public void addCompletedSet() {
        completedSets++;
    }

    public void resetCompletedSets() {
        completedSets = 0;
    }

    @Override
    public String toString() {
        return name + " (Подходы в неделю: " + weeklySets + ", Выполнено: " + completedSets + ")";
    }
}

class Workout {
    private List<MuscleGroup> muscleGroups = new ArrayList<>();
    private List<Exercise> exercises = new ArrayList<>();

    public void addMuscleGroup(String name, int sets) {
        muscleGroups.add(new MuscleGroup(name, sets));
    }

    public void addExercise(String name, double weight) {
        exercises.add(new Exercise(name, weight));
    }

    public void setExerciseWeight(String name, double weight) {
        for (Exercise ex : exercises) {
            if (ex.name.equals(name)) {
                ex.setWeight(weight);
                break;
            }
        }
    }

    public void addSet(int index) {
        if (index >= 0 && index < muscleGroups.size()) {
            muscleGroups.get(index).addCompletedSet();
            updateMuscleGroupsFile();
        }
    }

    public void updateWeights(int weekCount) {
        for (Exercise ex : exercises) {
            if (weekCount % 4 < 2) {
                // Первые две недели: применяем -30%
                ex.setWeight(ex.getReducedWeight());
            } else {
                // Последние две недели: сбрасываем к стандартному весу
                ex.resetWeight();
            }
        }
    }

    public void increaseStandardWeights() {
        for (Exercise ex : exercises) {
            ex.increaseWeight(); // Увеличиваем стандартный вес на 5%
        }
    }

    public void resetCompletedSets() {
        for (MuscleGroup group : muscleGroups) {
            group.resetCompletedSets();
        }
    }

    public String getMuscleGroups() {
        StringBuilder sb = new StringBuilder();
        for (MuscleGroup group : muscleGroups) {
            sb.append(group.toString()).append("\n");
        }
        return sb.toString().trim();
    }

    public String getExercises() {
        StringBuilder sb = new StringBuilder();
        for (Exercise ex : exercises) {
            sb.append(ex.toString()).append("\n");
        }
        return sb.toString().trim();
    }

    public void updateFiles() {
        updateExercisesFile();
        updateMuscleGroupsFile();
    }

    private void updateExercisesFile() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("exercises.txt"))) {
            for (Exercise ex : exercises) {
                bw.write(ex.name + ":" + String.format("%.2f", ex.standardWeight));
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateMuscleGroupsFile() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("muscle_groups.txt"))) {
            for (MuscleGroup group : muscleGroups) {
                bw.write(group.name + ":" + group.weeklySets + ":" + group.completedSets);
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadMuscleGroupsFromFile(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(":");
                MuscleGroup group = new MuscleGroup(parts[0], Integer.parseInt(parts[1]));
                group.completedSets = Integer.parseInt(parts[2]);
                muscleGroups.add(group);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Ошибка загрузки из файла: " + e.getMessage());
        }
    }

    public void loadExercisesFromFile(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(":");
                addExercise(parts[0], Double.parseDouble(parts[1]));
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Ошибка загрузки из файла: " + e.getMessage());
        }
    }
}

public class WorkoutApp extends JFrame {
    private Workout workout;
    private JTextArea muscleGroupArea;
    private JTextArea exerciseArea;
    private JTextField muscleGroupInput;
    private Timer timer;
    private long timerStart;
    private int weekCount = 0; // Счетчик недель

    public WorkoutApp() {
        workout = new Workout();
        workout.loadMuscleGroupsFromFile("muscle_groups.txt");
        workout.loadExercisesFromFile("exercises.txt");
        timerStart = loadLastRunTime();
        initializeUI();
        scheduleWeeklyWeightUpdate();
    }

    private void initializeUI() {
        setTitle("Workout Tracker");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        muscleGroupArea = new JTextArea(10, 30);
        exerciseArea = new JTextArea(10, 30);
        muscleGroupInput = new JTextField(10);

        JButton addSetButton = new JButton("Добавить подход");
        addSetButton.addActionListener(e -> {
            try {
                int index = Integer.parseInt(muscleGroupInput.getText());
                workout.addSet(index);
                JOptionPane.showMessageDialog(this, "Добавлен подход к группе " + index + ".");
                muscleGroupInput.setText("");
                updateMuscleGroupArea();
            } catch (NumberFormatException | IndexOutOfBoundsException ex) {
                JOptionPane.showMessageDialog(this, "Ошибка: " + ex.getMessage());
            }
        });

        JPanel panel = new JPanel();
        panel.add(new JLabel("Введите индекс мышечной группы:"));
        panel.add(muscleGroupInput);
        panel.add(addSetButton);

        add(panel, BorderLayout.NORTH);
        add(new JScrollPane(muscleGroupArea), BorderLayout.WEST);
        add(new JScrollPane(exerciseArea), BorderLayout.CENTER);

        updateMuscleGroupArea();
        updateExerciseArea();

        setVisible(true);
    }

    private void updateMuscleGroupArea() {
        muscleGroupArea.setText(workout.getMuscleGroups());
    }

    private void updateExerciseArea() {
        exerciseArea.setText(workout.getExercises());
    }

    private void scheduleWeeklyWeightUpdate() {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - timerStart;

        long timeToNextUpdate = (7 * 24 * 60 * 60 * 1000) - (elapsedTime % (7 * 24 * 60 * 60 * 1000));

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                weekCount++;

                // Обновляем веса на основе периода
                workout.updateWeights(weekCount);

                workout.resetCompletedSets();
                workout.updateFiles();
                saveLastRunTime(System.currentTimeMillis());
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(WorkoutApp.this, "Вес всех упражнений обновлён в связи с периодизацией нагрузки.");
                    updateExerciseArea();
                    updateMuscleGroupArea();
                });
            }
        }, timeToNextUpdate, 1000 * 60 * 60 * 24 * 7);
    }

    private long loadLastRunTime() {
        try (BufferedReader br = new BufferedReader(new FileReader("timer.txt"))) {
            return Long.parseLong(br.readLine());
        } catch (IOException | NumberFormatException e) {
            return System.currentTimeMillis(); // Используем текущее время, если файл отсутствует или поврежден
        }
    }

    private void saveLastRunTime(long time) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("timer.txt"))) {
            bw.write(String.valueOf(time));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(WorkoutApp::new);
    }
}
