package maxian.milos.jsontogooglemap;

import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private JSONArray golfCourses;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        FetchDataTask task = new FetchDataTask();
        task.execute("http://ptm.fi/materials/golfcourses/golf_courses.json");

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                View v = getLayoutInflater().inflate(R.layout.custom_info, null);
                TextView title = (TextView) v.findViewById(R.id.title);
                title.setText(marker.getTitle());
                TextView snippet = (TextView) v.findViewById(R.id.snippet);
                snippet.setText(marker.getSnippet());
                return v;
            }
        });
    }

    class FetchDataTask extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... urls) {
            HttpURLConnection urlConnection = null;
            JSONObject json = null;
            try {
                URL url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                bufferedReader.close();
                json = new JSONObject(stringBuilder.toString());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) urlConnection.disconnect();
            }
            return json;
        }

        protected void onPostExecute(JSONObject json) {
            try {
                HashMap<String, BitmapDescriptor> colors = new HashMap<>();
                colors.put("Kulta", BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                colors.put("Kulta/Etu", BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                colors.put("Etu", BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                colors.put("?", BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE));

                LatLng latlng = null;

                golfCourses = json.getJSONArray("courses");
                for (int i = 0; i < golfCourses.length(); i++) {
                    JSONObject course = golfCourses.getJSONObject(i);

                    String type = course.getString("type");
                    BitmapDescriptor color = colors.containsKey(type) ? colors.get(type) : BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);

                    latlng = new LatLng(course.getDouble("lat"), course.getDouble("lng"));
                    String snippet = course.getString("address") + "\n"
                            + course.getString("phone") + "\n"
                            + course.getString("email") + "\n"
                            + course.getString("web") + "\n";

                    MarkerOptions marker = new MarkerOptions()
                            .icon(color)
                            .position(latlng)
                            .title(course.getString("course"))
                            .snippet(snippet);
                    mMap.addMarker(marker);
                }
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 6f));
            } catch (JSONException e) {
                Log.e("JSON", "Error getting data.");
            }
        }
    }
}
