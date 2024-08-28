import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

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

    public void addExercise(String name, int weight) {
        exercises.add(new Exercise(name, weight));
    }

    public void setExerciseWeight(String name, int weight) {
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
        }
    }

    public String getMuscleGroups() {
        StringBuilder sb = new StringBuilder();
        for (MuscleGroup group : muscleGroups) {
            sb.append(group.toString()).append("\n"); // Добавляем разрыв строки
        }
        return sb.toString().trim(); // Удаляем лишний символ переноса в конце
    }

    public String getExercises() {
        StringBuilder sb = new StringBuilder();
        for (Exercise ex : exercises) {
            sb.append(ex.toString()).append("\n"); // Добавляем разрыв строки
        }
        return sb.toString().trim(); // Удаляем лишний символ переноса в конце
    }

    public List<Exercise> getExercisesList() {
        return exercises;
    }
}

public class WorkoutApp extends JFrame {
    private Workout workout;
    private JTextArea muscleGroupArea;
    private JTextArea exerciseArea;
    private JTextField muscleGroupInput;

    public WorkoutApp() {
        workout = new Workout();
        loadFromFile("muscle_groups.txt", true);
        loadFromFile("exercises.txt", false);
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Deu sEX machina!");
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

    private void loadFromFile(String filename, boolean isMuscleGroups) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(":");
                if (isMuscleGroups) {
                    workout.addMuscleGroup(parts[0], Integer.parseInt(parts[1]));
                } else {
                    workout.addExercise(parts[0], Integer.parseInt(parts[1]));
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Ошибка загрузки из файла: " + e.getMessage());
        }
    }

    private void updateMuscleGroupArea() {
        muscleGroupArea.setText(workout.getMuscleGroups());
    }

    private void updateExerciseArea() {
        exerciseArea.setText(workout.getExercises());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(WorkoutApp::new);
    }
}
