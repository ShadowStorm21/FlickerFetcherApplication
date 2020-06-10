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
    public static final String PREF_LAST_RESULT_ID = "lastResultId";
    public static final String PREF_SEARCH_QUERY = "searchQuery";
    private static final String PARAM_EXTRAS = "extras";
    private static final String EXTRA_SMALL_URL = "url_s";
    private static final String XML_PHOTO = "photo";
    private static final String PARAM_TEXT = "text";
    public static int count = 0;

    public byte[] getBytes(String url) throws IOException {           // Method to get the images in byte format
        URL mUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) mUrl.openConnection();

        try {
            if(connection.getResponseCode() != HttpURLConnection.HTTP_OK)    // check if connection is valid or not
            {
                return null;
            }
            else
            {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                InputStream inputStream = connection.getInputStream();
                int byteRead = 0;
                byte[] buffer = new byte[1024];

                while ((byteRead = inputStream.read(buffer)) > 0)              // read the input from the website
                {
                    byteArrayOutputStream.write(buffer,0,byteRead);      // write the bytes
                }
                byteArrayOutputStream.close();
                return byteArrayOutputStream.toByteArray();
            }

        }finally {
            connection.disconnect();
        }
    }
    public String getUrl(String url) throws IOException {  // method to get the url

        return new String(getBytes(url));
    }


    public ArrayList<GalleryItem> downloadGalleryItems(String url)  // method to to download the items from flicker
    {
        ArrayList<GalleryItem> items = new ArrayList<>();

        try {
            String xmlString = getUrl(url); // get the url
            XmlPullParserFactory parserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = parserFactory.newPullParser();
            parser.setInput(new StringReader(xmlString));
            parseItems(items,parser); // parse the xml content

        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
        return items;
    }
    public ArrayList<GalleryItem> fetchItems()        // method to build the url to fetch the recent items from flicker
    {
        String url = Uri.parse(ENDPOINT).buildUpon().appendQueryParameter("method",METHOD_GET_RECENT).appendQueryParameter("api_key",API_KEY)
                .appendQueryParameter(PARAM_EXTRAS,EXTRA_SMALL_URL).build().toString();
        return downloadGalleryItems(url);
    }
    public ArrayList<GalleryItem> search(String query) // method to build the url to search about an item in flicker
    {
        String url = Uri.parse(ENDPOINT).buildUpon().appendQueryParameter("method",METHOD_SEARCH).appendQueryParameter("api_key",API_KEY)
                .appendQueryParameter(PARAM_EXTRAS,EXTRA_SMALL_URL).appendQueryParameter(PARAM_TEXT,query).build().toString();
        return downloadGalleryItems(url);
    }




    public void parseItems(ArrayList<GalleryItem> items, XmlPullParser parser) throws XmlPullParserException, IOException {   // method to parse the items from XML format

        int eventType = parser.getEventType();     // get the event type

        while (eventType != XmlPullParser.END_DOCUMENT)    // iterate over the document until the end
        {
            if(eventType == XmlPullParser.START_TAG && XML_PHOTO.equals(parser.getName()))          // check if the event type equals the starting tag and it is a photo
            {
                String id = parser.getAttributeValue(null,"id");
                String caption = parser.getAttributeValue(null,"title");      // get the id and caption and url from it
                String smallUrl = parser.getAttributeValue(null,EXTRA_SMALL_URL);
                String owner = parser.getAttributeValue(null,"owner");

                GalleryItem galleryItem = new GalleryItem(id,caption,smallUrl,owner);       // create a new item
                count++;
                items.add(galleryItem);         // add the new item to the arraylist

            }
            eventType = parser.next(); // go the next tag in the document
        }
    }

}
