package ru.joysi.audio.exceptions;

import ru.joysi.audio.GlobalHelper;

/**
 * Created by 886 on 27.05.2016.
 */
public class AudioFilterException extends Exception {
    public AudioFilterException() {
        super(GlobalHelper.props.getProperty("error.filter.incorrectParameters"));
    }
}
