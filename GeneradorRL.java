import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class GeneradorRL {
    public Map<short[], float[]> map = new HashMap<short[], float[]>() {
        @Override
        public boolean containsKey(Object key) {
            if (key instanceof short[]) {
                for (short[] k : keySet()) {
                    if (Arrays.equals(k, (short[]) key)) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public float[] get(Object key) {
            if (key instanceof short[]) {
                for (Map.Entry<short[], float[]> entry : entrySet()) {
                    if (Arrays.equals(entry.getKey(), (short[]) key)) {
                        return entry.getValue();
                    }
                }
            }
            return null;
        }

        @Override
        public float[] put(short[] key, float[] value) {
            for (Map.Entry<short[], float[]> entry : entrySet()) {
                if (Arrays.equals(entry.getKey(), key)) {
                    return super.put(entry.getKey(), value);
                }
            }
            return super.put(key, value);
        }
    };

    public static void main(String[] args) {
        GeneradorRL generador = new GeneradorRL();

        // inicializarCSV(generador);
        cargarCSV(generador);
        porcentajeEntrenado(generador);
        System.out.println("Acabe");
    }

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
                generador.map.put(key, value);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void inicializarCSV(GeneradorRL generador) {
        // Triunfos
        for (int T1 = 0; T1 < 2; T1++) {
            for (int T3 = 0; T3 < 2; T3++) {
                for (int TRC = 0; TRC < 3; TRC++) {
                    for (int TS7 = 0; TS7 < 3; TS7++) {
                        for (int T6542 = 0; T6542 < 6; T6542++) {
                            // No Triunfos (Demas)
                            for (int D1 = 0; D1 < 5; D1++) {
                                for (int D3 = 0; D3 < 5; D3++) {
                                    for (int DRC = 0; DRC < 8; DRC++) {
                                        for (int DS7 = 0; DS7 < 8; DS7++) {
                                            for (int D6542 = 0; D6542 < 14; D6542++) {
                                                int suma = T1 + T3 + TRC + TS7 + T6542 + D1 + D3 + DRC + DS7 + D6542;
                                                if (suma == 10) {
                                                    float probabilidad = (float) 1 / (float) 11;
                                                    generador.map.put(new short[] { (short) T1, (short) T3, (short) TRC,
                                                            (short) TS7, (short) T6542,
                                                            (short) D1, (short) D3, (short) DRC, (short) DS7,
                                                            (short) D6542 },
                                                            new float[] { probabilidad, probabilidad, probabilidad,
                                                                    probabilidad, probabilidad, probabilidad,
                                                                    probabilidad, probabilidad, probabilidad,
                                                                    probabilidad, probabilidad });
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        guardarCSV(generador);
    }

    private static boolean isDefaultProbabilities(float[] values) {
        for (float v : values) {
            if (v != 0.09f) {
                return false;
            }
        }
        return true;
    }

    public static void guardarCSV(GeneradorRL generador) {
        try (FileWriter writer = new FileWriter("output.csv")) {
            for (Map.Entry<short[], float[]> entry : generador.map.entrySet()) {
                float[] value = entry.getValue();
                if (isDefaultProbabilities(value)) {
                    continue;
                }
                short[] key = entry.getKey();
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

    public static void porcentajeEntrenado(GeneradorRL generador) {
        int total = 0;
        int entrenado = 0;
        for (Map.Entry<short[], float[]> entry : generador.map.entrySet()) {
            total++;
            float[] value = entry.getValue();
            // solo miramos los primneros valores
            if (isDefaultProbabilities(value)) {
                entrenado++;
            }
        }
        System.out.println("Entrenado: " + entrenado + " de " + total);
    }
}
