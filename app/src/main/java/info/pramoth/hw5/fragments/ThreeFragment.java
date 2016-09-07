package info.pramoth.hw5.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

import info.androidhive.materialtabs.R;


public class ThreeFragment extends Fragment implements View.OnClickListener {

    public ThreeFragment() {
        // Required empty public constructor
    }


    MapView mapView;
    GoogleMap map;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_three
                , container, false);


        MapsInitializer.initialize(getActivity());

        switch (GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity())) {
            case ConnectionResult.SUCCESS:
//                Toast.makeText(getActivity(), "SUCCESS", Toast.LENGTH_SHORT).show();
                mapView = (MapView) v.findViewById(R.id.map);
                mapView.onCreate(savedInstanceState);
                // Gets to GoogleMap from the MapView and does initialization stuff
                if (mapView != null) {
                    map = mapView.getMap();
                    map.getUiSettings().setMyLocationButtonEnabled(false);

                    if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return v;
                    }
//                    map.setMyLocationEnabled(true);
//                    LatLng latLng = new LatLng(0,0);
                    LatLng sydney = new LatLng(-34, 151);
                    map.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
                    map.moveCamera(CameraUpdateFactory.newLatLng(sydney));
//                    map.addMarker(new MarkerOptions().position(new LatLng(40,-79)).title("CMU"));
//                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng();
//                    map.animateCamera(cameraUpdate);
                }
                break;
            case ConnectionResult.SERVICE_MISSING:
                Toast.makeText(getActivity(), "SERVICE MISSING", Toast.LENGTH_SHORT).show();
                break;
            case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
                Toast.makeText(getActivity(), "UPDATE REQUIRED", Toast.LENGTH_SHORT).show();
                break;
            default: Toast.makeText(getActivity(), GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity()), Toast.LENGTH_SHORT).show();
        }




        // Updates the location and zoom of the MapView
        Button btn = (Button) v.findViewById(R.id.searchBtn);
        btn.setOnClickListener(this);

        return v;
    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    public void onSearch(View view) {

    }


    @Override
    public void onClick(View v) {
        Log.d("sss","Searching...");

        map.clear();
        EditText location_tf = (EditText) getActivity().findViewById(R.id.address);
        String location = location_tf.getText().toString();
        Log.d("address",location);
        List<Address> addressList = null;
        if (location != null || !location.equals("")) {
            Geocoder geocoder = new Geocoder(getContext());
            try {
                addressList = geocoder.getFromLocationName(location, 1);

            } catch (IOException e) {
                e.printStackTrace();
            }
            Address address = addressList.get(0);
            Log.d("address",address.toString());
            LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
//            map.addMarker(new MarkerOptions().position(latLng).title(addressList.get(0).getAddressLine(1)));
//            map.addMarker(new MarkerOptions().position(latLng).title(addressList.get(0).getFeatureName()+","+addressList.get(0).getLocality()+","+addressList.get(0).getThoroughfare()+","+addressList.get(0).getPostalCode()+","+addressList.get(0).getCountryCode()+","+addressList.get(0).getCountryName()));
            String exact="";
            for(int i=0; i< addressList.get(0).getMaxAddressLineIndex();i++ ){
                exact+=addressList.get(0).getAddressLine(i);
            }
//            map.addMarker(new MarkerOptions().position(latLng).title(addressList.get(0).getAddressLine(0)));
            map.addMarker(new MarkerOptions().position(latLng).title(exact));
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12.0f));
//            map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location., location.getLongitude()), 12.0f));
        }
    }
}
