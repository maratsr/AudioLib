package ru.joysi.audio.processing.combines;

import java.util.Arrays;

/**
 * Сумматоры сигнала , реализованные в виде static функций
 * @author MaratSR
 */
public class Combines {

    /**
     * Складывает аудиосигналы + проводит постнормализацию в [-1;1]
     * @param audio входные аудиосигналы
     * @return сложенный аудиосигнал
     */
    public static double[] combineWithNormalize( double[]... audio) {
        if (audio.length == 0) return null;
        if (audio.length == 1) return audio[1];

        int maxIdx = 0;
        // Найдем самый длинный семпл
        for(double[] arr: audio)
            if (arr.length > maxIdx)
                maxIdx = arr.length;

        // Приведем все входные семплы к максимальной длине
        for(int i=0; i < audio.length; i++)
            if (audio[i].length < maxIdx)
                audio[i] = Arrays.copyOf(audio[i], maxIdx);

        // Сложим все аудиосемплы (+ выделим пиковый аудиосигнал)
        double[] result = new double[maxIdx];
        double normalizer  = 1.0;
        for (int i = 0; i < maxIdx; i++) {
            for (int j = 0; j < audio.length; j++)
                result[i] += audio[j][i];
            double res = Math.abs(result[i]);
            if (res > normalizer)
                normalizer = res;
        }

        double coeff = 1.0/ normalizer;
        if (normalizer !=1.0)
            for (int i = 0; i < maxIdx; i++)
                result[i] *= coeff;
        return result;
    }

    /**
     * Объединение нескольких массив последовательно в один
     * @param audio исходные массивы
     * @return итоговый массив
     */
    public static double[] getSequenceByAdd(double[]... audio) {
        int length=0;
        for(double[] s: audio)
            length += s.length;

        double[] result = new double[length];

        int destPos = 0;
        for (int i = 0; i <audio.length ; i++) {
            System.arraycopy(audio[i], 0, result, destPos, audio[i].length);
            destPos += audio[i].length;
        }
        return result;
    }

    /**
     *  Складывает аудиосигналы c использование линейной компрессии диапазона
     * @param threshold пороговый уровень компрессии
     * @param audio входные аудиосигналы (должны быть нормализованы в [-1;1] !)
     * @return сложенный аудиосигнал
     */
    public static double[] combineWithLinearDynaRangeCompression(double threshold, double[]... audio) {
        if (audio.length == 0 || threshold >= 1 || threshold < 0) return null;
        if (audio.length == 1) return audio[1];
        int maxIdx = 0;

        // Найдем самый длинный семпл
        for(double[] arr: audio)
            if (arr.length > maxIdx)
                maxIdx = arr.length;

        // Приведем все входные семплы к максимальной длине
        for(int i=0; i < audio.length; i++)
            if (audio[i].length < maxIdx)
                audio[i] = Arrays.copyOf(audio[i], maxIdx);

        double[] result = Arrays.copyOf(audio[0], maxIdx); // Нормализованный результируюший массив.
        double linearCoeff  = (1-threshold)/(2-threshold);

        // Сложим все аудиосемплы по принципу
        for (int i = 1; i < audio.length; i++)
            for (int j = 0; j < maxIdx; j++) {
                double res = result[j] + audio[i][j];
                double absRes = Math.abs(result[j] + audio[i][j]);
                if (absRes <= threshold)
                    result[j] = result[j] + audio[i][j];
                else
                    result[j] = Math.signum(res) * (threshold + linearCoeff * (absRes - threshold));
            }
        return result;
    }

    /**
     *  Складывает аудиосигналы c использование логарифмической компрессии диапазона
     * @param threshold пороговый уровень компрессии
     * @param audio входные аудиосигналы (должны быть нормализованы в [-1;1] !)
     * @return сложенный аудиосигнал
     */
    public static double[] combineWithLnDynaRangeCompression(double threshold, double[]... audio) {
        if (audio.length == 0 || threshold >= 1 || threshold < 0) return null;
        if (audio.length == 1) return audio[1];
        int maxIdx = 0;

        // Найдем самый длинный семпл
        for(double[] arr: audio)
            if (arr.length > maxIdx)
                maxIdx = arr.length;

        // Приведем все входные семплы к максимальной длине
        for(int i=0; i < audio.length; i++)
            if (audio[i].length < maxIdx)
                audio[i] = Arrays.copyOf(audio[i], maxIdx);

        double[] result = Arrays.copyOf(audio[0], maxIdx); // Нормализованный результируюший массив.
        double expCoeff = alphaT[(int) threshold*100];

        // Сложим все аудиосемплы по принципу
        for (int i = 1; i < audio.length; i++)
            for (int j = 0; j < maxIdx; j++) {
                double res = result[j] + audio[i][j];
                double absRes = Math.abs(result[j] + audio[i][j]);
                if (absRes <= threshold)
                    result[j] = result[j] + audio[i][j];
                else
                    result[j] = Math.signum(res) * (threshold + ( 1 - threshold) *
                            Math.log(1.0 + expCoeff * (absRes-threshold) /(2-threshold)) /
                            Math.log(1.0 + expCoeff ));
            }
        return result;
    }

    // Решение уравнений pow(1+x,1/x)=exp((1-t)/(2-t)) при t=0, 0.01, 0.02 ... 0.99
    final private static double[] alphaT =
            {
                    2.51286, 2.54236, 2.57254, 2.60340, 2.63499, 2.66731, 2.70040, 2.73428, 2.76899, 2.80454,
                    2.84098, 2.87833, 2.91663, 2.95592, 2.99622, 3.03758, 3.08005, 3.12366, 3.16845, 3.21449,
                    3.26181, 3.31048, 3.36054, 3.41206, 3.46509, 3.51971, 3.57599, 3.63399, 3.69380, 3.75550,
                    3.81918, 3.88493, 3.95285, 4.02305, 4.09563, 4.17073, 4.24846, 4.32896, 4.41238, 4.49888,
                    4.58862, 4.68178, 4.77856, 4.87916, 4.98380, 5.09272, 5.20619, 5.32448, 5.44790, 5.57676,
                    5.71144, 5.85231, 5.99980, 6.15437, 6.31651, 6.48678, 6.66578, 6.85417, 7.05269, 7.26213,
                    7.48338, 7.71744, 7.96541, 8.22851, 8.50810, 8.80573, 9.12312, 9.46223, 9.82527, 10.21474,
                    10.63353, 11.08492, 11.57270, 12.10126, 12.67570, 13.30200, 13.98717, 14.73956, 15.56907, 16.48767,
                    17.50980, 18.65318, 19.93968, 21.39661, 23.05856, 24.96984, 27.18822, 29.79026, 32.87958, 36.59968,
                    41.15485, 46.84550, 54.13115, 63.74946, 76.95930, 96.08797, 125.93570, 178.12403, 289.19889, 655.12084
            };

}
