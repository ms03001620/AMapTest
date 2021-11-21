package com.quadtree;


import com.amap.api.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class JsonTestUtil {
    private static final String REGEX_INPUT_BOUNDARY_BEGINNING = "\\A";
    public static List<ClusterItem> read(String fileName) throws Exception {
        List<ClusterItem> items = new ArrayList();

        InputStream stream = JsonTestUtil.class.getClassLoader().getResourceAsStream(fileName);
        String json = new Scanner(stream).useDelimiter(REGEX_INPUT_BOUNDARY_BEGINNING).next();

        JSONArray array = new JSONArray(json);
        for (int i = 0; i < array.length(); i++) {
            String title = null;
            String snippet = null;
            JSONObject object = array.getJSONObject(i);
            double lat = object.getDouble("lat");
            double lng = object.getDouble("lng");
            if (!object.isNull("title")) {
                title = object.getString("title");
            }
            if (!object.isNull("snippet")) {
                snippet = object.getString("snippet");
            }
            items.add(new TestClusterItem(lat, lng, title, snippet));
        }
        return items;
    }

    static class TestClusterItem implements ClusterItem{
        private final LatLng mPosition;
        private String mTitle;
        private String mSnippet;

        public TestClusterItem(double lat, double lng, String title, String snippet) {
            mPosition = new LatLng(lat, lng);
            mTitle = title;
            mSnippet = snippet;
        }


        @Override
        public LatLng getPosition() {
            return mPosition;
        }

        @Override
        public String getTitle() { return mTitle; }

        @Override
        public String getSnippet() { return mSnippet; }

        /**
         * Set the title of the marker
         * @param title string to be set as title
         */
        public void setTitle(String title) {
            mTitle = title;
        }

        /**
         * Set the description of the marker
         * @param snippet string to be set as snippet
         */
        public void setSnippet(String snippet) {
            mSnippet = snippet;
        }
    }
}
