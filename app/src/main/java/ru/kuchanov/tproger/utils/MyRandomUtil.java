package ru.kuchanov.tproger.utils;

import java.util.Random;

/**
 * Created by Юрий on 13.11.2015 16:47 16:18.
 * For TProger.
 */
public class MyRandomUtil
{
    private static int randomInt;

    /**
     * @return recursivly check for not matching to given previous new random int in range
     */
    public static int nextInt(int prevInt, int range)
    {
        Random random = new Random();
        randomInt = random.nextInt(range);

        if (randomInt != prevInt)
        {
            return randomInt;
        }
        else
        {
            return nextInt(prevInt, range);
        }
    }
}
