import java.util.HashMap;
import java.util.Map;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class GeneradorRL {
    public static class ShortArrayKey {
        private final short[] array;

        public ShortArrayKey(short[] array) {
            this.array = array;
        }

        public short[] getArray() {
            return array;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof ShortArrayKey
                    && java.util.Arrays.equals(array, ((ShortArrayKey) o).array);
        }

        @Override
        public int hashCode() {
            return java.util.Arrays.hashCode(array);
        }
    }

    public Map<ShortArrayKey, float[]> map = new HashMap<>();

    // guardar como int y dividir por 100.0f es mas rapido que parseFloat
    public static void cargarCSV(GeneradorRL generador) {
        long startTime = System.nanoTime();

        // Adjust buffer size if needed, e.g. 8192 or larger if the file is big
        try (BufferedReader reader = new BufferedReader(new FileReader("output.csv"), 8192)) {
            String line;
            while ((line = reader.readLine()) != null) {
                // First 10 chars => short[] key
                short[] key = new short[10];
                for (int i = 0; i < 10; i++) {
                    key[i] = (short) (line.charAt(i) - '0');
                }

                // Next pairs => float[] value
                float[] value = new float[11];
                int valueIndex = 0;
                for (int i = 10; i + 1 < 32 && valueIndex < 11; i += 2) {
                    int intVal = (line.charAt(i) - '0') * 10 + (line.charAt(i + 1) - '0');
                    value[valueIndex++] = intVal / 100f;
                }

                // Insert into map
                generador.map.put(new ShortArrayKey(key), value);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        long endTime = System.nanoTime();
        System.out.println("Loading CSV took: " + (endTime - startTime) / 1_000_000 + " ms");
    }

    public static void guardarCSV(GeneradorRL generador) {
        try (FileWriter writer = new FileWriter("output.csv")) {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<ShortArrayKey, float[]> entry : generador.map.entrySet()) {
                short[] key = entry.getKey().getArray();
                float[] value = entry.getValue();
                // Write key digits
                for (short k : key) {
                    sb.append(k);
                }
                for (int i = 0; i < value.length; i++) {
                    int intVal = (int) (value[i] * 100);
                    sb.append(String.format("%02d", intVal));
                }
                sb.append("\n");
            }
            writer.write(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
