package io.github.krankenwagen.slf4fx.message;

@SuppressWarnings("serial")
public class UnknownMessageException extends Exception
{
    private final int value;
    
    UnknownMessageException(int value)
    {
        super("for value: " + value);
        this.value = value;
    }

    public int getValue()
    {
        return value;
    }
}
