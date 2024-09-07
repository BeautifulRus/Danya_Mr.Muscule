import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

class Exercise {
    String name;
    double standardWeight;
    double currentWeight;

    public Exercise(String name, double weight) {
        this.name = name;
        this.standardWeight = weight;
        this.currentWeight = weight;
    }

    public void setWeight(double weight) {
        this.currentWeight = weight;
    }

    public double getStandardWeight() {
        return standardWeight;
    }

    public void increaseWeight() {
        standardWeight *= 1.05;
    }

    public double getReducedWeight() {
        return standardWeight * 0.7;
    }

    public void resetWeight() {
        currentWeight = standardWeight;
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
        updateMuscleGroupsFile(); // Обновляем файл после добавления группы
    }

    public void addExercise(String name, double weight) {
        exercises.add(new Exercise(name, weight));
        updateExercisesFile(); // Обновляем файл после добавления упражнения
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
            checkAndUpdateWeights();
        }
    }

    private void checkAndUpdateWeights() {
        if (muscleGroups.stream().allMatch(mg -> mg.completedSets >= mg.weeklySets)) {
            increaseStandardWeights();
            resetCompletedSets();
        }
    }

    public void updateWeights(int weekCount) {
        for (Exercise ex : exercises) {
            if (weekCount % 4 < 2) {
                ex.setWeight(ex.getReducedWeight());
            } else {
                ex.resetWeight();
            }
        }
    }

    public void increaseStandardWeights() {
        for (Exercise ex : exercises) {
            ex.increaseWeight();
        }
        updateExercisesFile();
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

    public void updateMuscleGroupsFile() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("muscle_groups.txt"))) {
            for (MuscleGroup group : muscleGroups) {
                bw.write(group.name + ":" + group.weeklySets + ":" + group.completedSets);
                bw.newLine();
            }
        } catch (IOException e) {
            showError("Ошибка при сохранении мышечных групп: " + e.getMessage());
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
            showError("Ошибка при загрузке мышечных групп: " + e.getMessage());
        } catch (NumberFormatException e) {
            showError("Ошибка формата данных в файле мышечных групп: " + e.getMessage());
        }
    }

    public void loadExercisesFromFile(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(":");
                addExercise(parts[0], Double.parseDouble(parts[1].replace(",", ".")));
            }
        } catch (IOException ex) {
            showError("Ошибка при загрузке упражнений: " + ex.getMessage());
        } catch (NumberFormatException ex) {
            showError("Ошибка формата данных в файле упражнений: " + ex.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(null, message, "Ошибка", JOptionPane.ERROR_MESSAGE);
        System.err.println(message);
    }

    public void updateExercisesFile() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("exercises.txt"))) {
            for (Exercise ex : exercises) {
                bw.write(ex.name + ":" + String.format("%.2f", ex.standardWeight).replace(".", ","));
                bw.newLine();
            }
        } catch (IOException e) {
            showError("Ошибка при сохранении упражнений: " + e.getMessage());
        }
    }
}

public class WorkoutApp extends JFrame {
    private Workout workout;
    private JTextArea muscleGroupArea;
    private JTextArea exerciseArea;
    private JTextField muscleGroupInput;
    private JTextField exerciseNameInput;
    private JTextField exerciseWeightInput;
    private JTabbedPane tabbedPane;
    private Timer timer;
    private long timerStart;
    private int weekCount = 0;

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
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        tabbedPane = new JTabbedPane();

        JPanel mainTab = new JPanel();
        mainTab.setLayout(new BorderLayout());

        muscleGroupArea = new JTextArea(10, 30);
        JButton addSetButton = new JButton("Добавить подход");
        muscleGroupInput = new JTextField(5);

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

        exerciseArea = new JTextArea(10, 30);
        updateExerciseArea();

        JPanel inputPanel = new JPanel();
        inputPanel.add(new JLabel("Индекс мышечной группы:"));
        inputPanel.add(muscleGroupInput);
        inputPanel.add(addSetButton);

        mainTab.add(inputPanel, BorderLayout.NORTH);
        mainTab.add(new JScrollPane(muscleGroupArea), BorderLayout.WEST);
        mainTab.add(new JScrollPane(exerciseArea), BorderLayout.EAST);

        tabbedPane.addTab("Тренировки", mainTab);

        JPanel editTab = new JPanel();
        editTab.setLayout(new BorderLayout());

        exerciseNameInput = new JTextField(10);
        exerciseWeightInput = new JTextField(5);
        JButton addExerciseButton = new JButton("Добавить упражнение");
        JButton addMuscleGroupButton = new JButton("Добавить мышечную группу");
        JTextField newMuscleGroupNameInput = new JTextField(10);
        JTextField newMuscleGroupSetsInput = new JTextField(5);

        addExerciseButton.addActionListener(e -> {
            try {
                String name = exerciseNameInput.getText();
                double weight = Double.parseDouble(exerciseWeightInput.getText().replace(",", "."));
                workout.addExercise(name, weight);
                exerciseNameInput.setText("");
                exerciseWeightInput.setText("");
                updateExerciseArea();
                JOptionPane.showMessageDialog(this, "Упражнение добавлено.");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Ошибка: Введите корректный вес.");
            }
        });

        addMuscleGroupButton.addActionListener(e -> {
            try {
                String name = newMuscleGroupNameInput.getText();
                int sets = Integer.parseInt(newMuscleGroupSetsInput.getText());
                workout.addMuscleGroup(name, sets);
                newMuscleGroupNameInput.setText("");
                newMuscleGroupSetsInput.setText("");
                JOptionPane.showMessageDialog(this, "Мышечная группа добавлена.");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Ошибка: Введите корректное число подходов.");
            }
        });

        JPanel exercisePanel = new JPanel();
        exercisePanel.add(new JLabel("Имя упражнения:"));
        exercisePanel.add(exerciseNameInput);
        exercisePanel.add(new JLabel("Вес:"));
        exercisePanel.add(exerciseWeightInput);
        exercisePanel.add(addExerciseButton);

        JPanel muscleGroupPanel = new JPanel();
        muscleGroupPanel.add(new JLabel("Имя новой группы:"));
        muscleGroupPanel.add(newMuscleGroupNameInput);
        muscleGroupPanel.add(new JLabel("Подходы:"));
        muscleGroupPanel.add(newMuscleGroupSetsInput);
        muscleGroupPanel.add(addMuscleGroupButton);

        editTab.add(exercisePanel, BorderLayout.NORTH);
        editTab.add(muscleGroupPanel, BorderLayout.CENTER);

        tabbedPane.addTab("Редактирование", editTab);

        add(tabbedPane, BorderLayout.CENTER);

        updateMuscleGroupArea();

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
                workout.updateWeights(weekCount);
                workout.resetCompletedSets();
                workout.updateExercisesFile(); // Обновляем файл после последнего обновления весов
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
            return System.currentTimeMillis();
        }
    }

    private void saveLastRunTime(long time) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("timer.txt"))) {
            bw.write(String.valueOf(time));
        } catch (IOException e) {
            System.err.println("Ошибка при сохранении таймера: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(WorkoutApp::new);
    }
}
