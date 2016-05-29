package ru.joysi.audio.processing;

/**
 * Created by Marat on 28.05.2016.
 */
public final class Volume {
    // Минимальное значение громкости - на этом уровне идёт отключение звука
    public final static double EPSILON = 0.001;

    // Коэффициент для преобразований в dBFS и обратно
    public final static double DBFS_COEF = 20 / Math.log(10);

    // По положению на шкале вычисляет громкость
    public static double volumeToExponent(double value) {
        double volume = Math.pow(EPSILON, 1 - value);
        return volume > EPSILON ? volume : 0;
    }

    // По значению громкости вычисляет положение на шкале
    public static double volumeFromExponent(double volume) {
        return 1 - Math.log(Math.max(volume, EPSILON)) / Math.log(EPSILON);
    }

    // Перевод значения громкости в dBFS
    public static double volumeToDBFS(double volume) {
        return Math.log(volume) * DBFS_COEF;
    }

    // Перевод значения dBFS в громкость
    public static double volumeFromDBFS( double dbfs) {
        return Math.exp(dbfs / DBFS_COEF);
    }

    // Уровень громкости участка
    public static double calcAvgAmpl(double[] audio) {
        double res=0;
        for(double elem: audio)
            res += Math.abs(elem);
        return res / audio.length;


    }
}
