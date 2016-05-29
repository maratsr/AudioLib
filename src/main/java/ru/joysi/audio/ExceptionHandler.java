package ru.joysi.audio;

/**
 * Created by 886 on 24.05.2016.
 */
public class ExceptionHandler {
    /**
     * Вывод сообщения об ошибке //TODO сделать в лог
     * @param e Исключение
     * @param errorMsg Описание исключения
     */
    public static void log(Exception e, String errorMsg) {
            System.out.println(e.getClass().getSimpleName() + ":" + errorMsg);
    }
}
