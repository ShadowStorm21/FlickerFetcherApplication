package com.example.flickerfetcherapplication;

import android.net.Uri;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class FlickerFetcher {
    private static final String TAG = "FlickerFetcher";
    private static final String ENDPOINT = "https://api.flickr.com/services/rest/";
    private static final String API_KEY = "ec2c476009243cb859330e55f4784da2";
    private static final String METHOD_GET_RECENT = "flickr.photos.getRecent";
    private static final String METHOD_SEARCH = "flickr.photos.search";
    public static final String PREF_SEARCH_QUERY = "searchQuery";
    private static final String PARAM_EXTRAS = "extras";
    private static final String EXTRA_SMALL_URL = "url_s";
    private static final String XML_PHOTO = "photo";
    private static final String PARAM_TEXT = "text";
    public static int count = 0;

    public byte[] getBytes(String url) throws IOException {
        URL mUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) mUrl.openConnection();

        try {
            if(connection.getResponseCode() != HttpURLConnection.HTTP_OK)
            {
                return null;
            }
            else
            {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                InputStream inputStream = connection.getInputStream();
                int byteRead = 0;
                byte[] buffer = new byte[1024];

                while ((byteRead = inputStream.read(buffer)) > 0)
                {
                    byteArrayOutputStream.write(buffer,0,byteRead);
                }
                byteArrayOutputStream.close();
                return byteArrayOutputStream.toByteArray();
            }

        }finally {
            connection.disconnect();
        }
    }
    public String getUrl(String url) throws IOException {

        return new String(getBytes(url));
    }


    public ArrayList<GalleryItem> downloadGalleryItems(String url)
    {
        ArrayList<GalleryItem> items = new ArrayList<>();

        try {
            String xmlString = getUrl(url);
            XmlPullParserFactory parserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = parserFactory.newPullParser();
            parser.setInput(new StringReader(xmlString));
            parseItems(items,parser);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
        return items;
    }
    public ArrayList<GalleryItem> fetchItems()
    {
        String url = Uri.parse(ENDPOINT).buildUpon().appendQueryParameter("method",METHOD_GET_RECENT).appendQueryParameter("api_key",API_KEY)
                .appendQueryParameter(PARAM_EXTRAS,EXTRA_SMALL_URL).build().toString();
        return downloadGalleryItems(url);
    }
    public ArrayList<GalleryItem> search(String query)
    {
        String url = Uri.parse(ENDPOINT).buildUpon().appendQueryParameter("method",METHOD_SEARCH).appendQueryParameter("api_key",API_KEY)
                .appendQueryParameter(PARAM_EXTRAS,EXTRA_SMALL_URL).appendQueryParameter(PARAM_TEXT,query).build().toString();
        return downloadGalleryItems(url);
    }



    public void parseItems(ArrayList<GalleryItem> items, XmlPullParser parser) throws XmlPullParserException, IOException {

        int eventType = parser.getEventType();

        while (eventType != XmlPullParser.END_DOCUMENT)
        {
            if(eventType == XmlPullParser.START_TAG && XML_PHOTO.equals(parser.getName()))
            {
                String id = parser.getAttributeValue(null,"id");
                String caption = parser.getAttributeValue(null,"title");
                String smallUrl = parser.getAttributeValue(null,EXTRA_SMALL_URL);

                GalleryItem galleryItem = new GalleryItem(id,caption,smallUrl);
                count++;
                items.add(galleryItem);

            }
            eventType = parser.next();
        }
    }

}
