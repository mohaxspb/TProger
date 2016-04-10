package ru.kuchanov.tproger.otto;

/**
 * Created by Юрий on 25.02.2016 18:07.
 * For SimpleRSSReader.
 */
public class EventShowImage
{
    private String imageUrl;

    public EventShowImage(String imageUrl)
    {
        this.imageUrl = imageUrl;
    }

    public String getImageUrl()
    {
        return imageUrl;
    }
}