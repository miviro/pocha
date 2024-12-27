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

    public static void cargarCSV(GeneradorRL generador) {
        try (BufferedReader reader = new BufferedReader(new FileReader("output.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                short[] key = new short[10];
                for (int i = 0; i < 10; i++) {
                    key[i] = Short.parseShort(parts[i]);
                }
                float[] value = new float[11];
                int valueCount = parts.length - 10;
                for (int i = 0; i < 11; i++) {
                    value[i] = (i < valueCount) ? Float.parseFloat(parts[10 + i]) : 0f;
                }
                ShortArrayKey wrappedKey = new ShortArrayKey(key);
                generador.map.put(wrappedKey, value);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void guardarCSV(GeneradorRL generador) {
        try (FileWriter writer = new FileWriter("output.csv")) {
            for (Map.Entry<ShortArrayKey, float[]> entry : generador.map.entrySet()) {
                float[] value = entry.getValue();
                short[] key = entry.getKey().getArray();
                for (short k : key) {
                    writer.write(k + ",");
                }
                int lastNonZeroIndex = -1;
                for (int i = 0; i < value.length; i++) {
                    if (value[i] != 0f) {
                        lastNonZeroIndex = i;
                    }
                }
                for (int i = 0; i <= lastNonZeroIndex; i++) {
                    if (value[i] == 0f) {
                        writer.write("0");
                    } else {
                        writer.write(String.format("%.2f", value[i]).replace("0.", "."));
                    }
                    if (i < lastNonZeroIndex) {
                        writer.write(",");
                    }
                }
                writer.write("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
