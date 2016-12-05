package com.brain_socket.thagheralrafedain.fragments;

import android.Manifest.permission;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.brain_socket.thagheralrafedain.MapActivity;
import com.brain_socket.thagheralrafedain.R;
import com.brain_socket.thagheralrafedain.ThagherApp;
import com.brain_socket.thagheralrafedain.data.DataStore;
import com.brain_socket.thagheralrafedain.data.DataStore.DataRequestCallback;
import com.brain_socket.thagheralrafedain.data.ServerResult;
import com.brain_socket.thagheralrafedain.fragments.DiagPickFilter.FiltersPickerCallback;
import com.brain_socket.thagheralrafedain.model.BrandModel;
import com.brain_socket.thagheralrafedain.model.WorkshopModel;
import com.brain_socket.thagheralrafedain.view.AppWorkshopCard;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;


public class FragMap extends Fragment implements View.OnClickListener, OnMapReadyCallback, OnMarkerClickListener, GoogleMap.InfoWindowAdapter, FiltersPickerCallback{


    SupportMapFragment fragment;
    GoogleMap googleMap;

    public enum MAP_TYPE {SEARCH, BRAND}

    MAP_TYPE type;
    ArrayList<LocatableWorkshop> providers = null;
    ArrayList<WorkshopModel> workshops = null;
    HashMap<String, LocatableWorkshop> mapMarkerIdToLocatableProvider;
    LocatableWorkshop selectedLocatableWorkshop;

    AppWorkshopCard vItemDetailsPreview;
    boolean focusMap;

    MapActivity activity;
    float radius;
    double centerLat;
    double centerLon;

    Handler handler;

    //TempDataHolders
    BrandModel brandToShow;

    ArrayList<BrandModel> selectedBrands;

    public DataRequestCallback searchResultCallback = new DataRequestCallback() {
        @Override
        public void onDataReady(ServerResult result, boolean success) {
            if (success) {
                ArrayList<WorkshopModel> brands;
                try {
                    if (result.getPairs().containsKey("workshops")) {
                        brands = new ArrayList<>();
                        ArrayList<WorkshopModel> recievedShops = (ArrayList<WorkshopModel>) result.get("workshops");
                        brands.addAll(recievedShops);
                        updateView(brands, focusMap);
                    }
                } catch (Exception e) {
                }
            }
        }
    };

    public static FragMap newInstance(MAP_TYPE type, BrandModel brandToShow) {
        FragMap frag = new FragMap();
        Bundle extras = new Bundle();
        extras.putInt("type", type.ordinal());

        if(brandToShow != null)
            extras.putString("brand", brandToShow.getJsonString());

        frag.setArguments(extras);
        return frag;
    }

    public void resolveExtra(Bundle extra) {
        try {
            type = MAP_TYPE.values()[extra.getInt("type")];
            if(extra.containsKey("brand")) {
                String jsonStr = extra.getString("brand");
                JSONObject jsonObject = new JSONObject(jsonStr);
                brandToShow = BrandModel.fromJson(jsonObject);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_main_map, container, false);
        resolveExtra(getArguments());
        if (type == MAP_TYPE.SEARCH)
            this.activity = (MapActivity) getContext();
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        checkPlayServices(getActivity());
        requestLocationPermissionIfRequired();
        init();
    }

    private void init() {

        vItemDetailsPreview = (AppWorkshopCard) getView().findViewById(R.id.vProviderPreview);
        View btnFilter = getView().findViewById(R.id.btnFilter);
        View btnClose = getView().findViewById(R.id.btnClose);

        focusMap = false;
        mapMarkerIdToLocatableProvider = new HashMap<>();
        handler = new Handler();

        btnClose.setOnClickListener(this);
        btnFilter.setOnClickListener(this);

        // load Map Frag
        FragmentManager fm = getChildFragmentManager();
        fragment = (SupportMapFragment) fm.findFragmentById(R.id.flContentFrame);
        if (fragment == null) {
            fragment = SupportMapFragment.newInstance();
            fm.beginTransaction().replace(R.id.flContentFrame, fragment).commit();
            fragment.getMapAsync(this);
        }

        //inital state
        hidePreview();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        // this block is needed on some devices that throw a
        // "java.lang.NullPointerException: IBitmapDescriptorFactory is not initialized" exception
        try {
            MapsInitializer.initialize(getActivity());
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.googleMap = googleMap;
        this.googleMap.setOnCameraChangeListener(onCameraChangeListener);
        this.googleMap.setOnMarkerClickListener(this);
        // used to force Google maps bring
        // the marker to top onClick by showing an empty info window
        this.googleMap.setInfoWindowAdapter(this);
        this.googleMap.setOnMapClickListener(new OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                selectedLocatableWorkshop = null;
                hidePreview();
            }
        });

        // initial data load
        selectedLocatableWorkshop = null;
        focusMap = true;
        getNearByBrands();
    }

    @SuppressLint("StringFormatMatches")
    private void updateView(ArrayList<WorkshopModel> brands, boolean reFocusMap) {
        this.workshops = brands;
        try {
            if (brands != null && googleMap != null) {

                this.providers = new ArrayList<>();

                for (WorkshopModel brand : brands) {
                    LocatableWorkshop locatableWorkshop = new LocatableWorkshop();
                    locatableWorkshop.workshop = brand;
                    locatableWorkshop.type = LocatableWorkshop.MarkType.BRAND;
                    boolean isSelected = selectedLocatableWorkshop != null
                            && locatableWorkshop.workshop != null
                            && locatableWorkshop.workshop.getId().equals(selectedLocatableWorkshop.workshop.getId());

                    locatableWorkshop.markerOptions = new MarkerOptions()
                                    .position(brand.getCoords())
                                    // if its the highlighted workshop then draw it with a different marker icon
                                    .icon(BitmapDescriptorFactory.fromResource(isSelected ? R.drawable.ic_marker_active : R.drawable.ic_marker));
                    this.providers.add(locatableWorkshop);
                }
                // Map
                drawProvidersOnMap(this.providers, reFocusMap);
            }
        } catch (Exception e) {
        }
    }

    /**
     * used to re draw all providers and update Camera position if required
     * @param providers array of providers that wil be represented on map with markers
     * @param focusMap: if true, we animated the map camera to fit all the markers in view
     */
    private void drawProvidersOnMap(ArrayList<LocatableWorkshop> providers, boolean focusMap) {
        try {
            if (providers != null && googleMap != null) {
                googleMap.clear();

                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (LocatableWorkshop provider : providers) {
                    try {

                        Marker marker = googleMap.addMarker(provider.markerOptions);
                        mapMarkerIdToLocatableProvider.put(marker.getId(), provider);
                        LatLng position = provider.markerOptions.getPosition();
                        builder.include(position);
                        if (selectedLocatableWorkshop != null && provider.workshop.getId().equals(selectedLocatableWorkshop.workshop.getId()))
                            marker.showInfoWindow();
                    } catch (Exception e) {
                    }
                }

                if (focusMap) {
                    LatLngBounds bounds = builder.build();
                    focusMap(bounds);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void focusMap(LatLngBounds bounds) {
        int padding = 50;
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        googleMap.animateCamera(cu);
    }

    public void displayProviderDetailsPreview(LocatableWorkshop locatableWorkshop) {
        if (locatableWorkshop != null && locatableWorkshop.workshop != null) {
            vItemDetailsPreview.updateUI(locatableWorkshop.workshop);
            vItemDetailsPreview.setVisibility(View.VISIBLE);
        }
    }

    public void displayBrandPreview(WorkshopModel workshop) {
        if (workshop != null) {
            vItemDetailsPreview.updateUI(workshop);
            vItemDetailsPreview.setVisibility(View.VISIBLE);
        }
    }

    /**
     * hide details Preview
     */
    public void hidePreview() {
        vItemDetailsPreview.setVisibility(View.GONE);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        try {
            LocatableWorkshop locatableWorkshop = mapMarkerIdToLocatableProvider.get(marker.getId());
            if (locatableWorkshop != null) {
                displayProviderDetailsPreview(locatableWorkshop);
                selectedLocatableWorkshop = locatableWorkshop;
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_active));
                focusMapOnMarker(marker.getPosition());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    // used to force Google maps bring
    // the marker to top onClick by showing an empty info window
    @Override
    public View getInfoWindow(Marker marker) {
        View v = getActivity().getLayoutInflater().inflate(R.layout.layout_marker_info_window, null);
        return v;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    /**
     * update the camera to make the marker at the bottom half of the screen and zoom in to focus on marker
     * by first zooming in
     */
    private void focusMapOnMarker(LatLng latLng) {
        try {
            // zooming above the marker a little to make sure the marker stays at
            // the lower half of screen so it wont be covered by details card
            float paddingAboveMarker = 0.006f;
            final LatLng markerLatLng = new LatLng(latLng.latitude + paddingAboveMarker, latLng.longitude);
            CameraUpdate center = CameraUpdateFactory.newLatLng(markerLatLng);
            googleMap.animateCamera(center);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getNearByBrands() {
        Log.i("MAP", "getNearByBrands");
        //get coordinates of center screen point
        VisibleRegion vr = googleMap.getProjection().getVisibleRegion();
        centerLat = vr.latLngBounds.getCenter().latitude;
        centerLon = vr.latLngBounds.getCenter().longitude;

        //calculate distance between middleSouthEdge and screen center map location

        //center location
        Location centerLoc = new Location("center");
        centerLoc.setLatitude(centerLat);
        centerLoc.setLongitude(centerLon);
        //middle South location
        Location middlleSouthLocation = new Location("left");
        middlleSouthLocation.setLongitude(centerLoc.getLongitude());
        middlleSouthLocation.setLatitude(vr.latLngBounds.southwest.latitude);

        radius = centerLoc.distanceTo(middlleSouthLocation);
        focusMap = false;
        Log.i("Params", centerLat + "," + centerLon + "," + radius);
        DataStore.getInstance().requestNearbyWorkshops((float) centerLat, (float) centerLon, radius, selectedBrands, searchResultCallback);
    }

    GoogleMap.OnCameraChangeListener onCameraChangeListener = new GoogleMap.OnCameraChangeListener() {
        @Override
        public void onCameraChange(CameraPosition cameraPosition) {
            getNearByBrands();
        }
    };

    public void setFocusMap(boolean focusMap) {
        this.focusMap = focusMap;
    }

    @Override
    public void onFiltersSelected(ArrayList<BrandModel> categories) {
        hidePreview();
        setFocusMap(true);
        this.selectedBrands = categories;
        getNearByBrands();
    }

    public static class LocatableWorkshop {
        WorkshopModel workshop;
        MarkerOptions markerOptions;

        enum MarkType {BRAND, BRANCH}
        MarkType type;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnClose:
                getActivity().finish();
            break;
            case R.id.btnFilter:
                DiagPickFilter diag = new DiagPickFilter(getContext(), selectedBrands, this);
                diag.show();
                break;
        }
    }

    public static boolean checkPlayServices(final Activity activity) {
        final int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, activity, 300);//PLAY_SERVICES_RESOLUTION_REQUEST
                if (dialog != null) {
                    dialog.show();
                    return false;
                }
            }
            new AlertDialog.Builder(activity)
                    .setCancelable(false)
                    .setMessage("This device is not supported for required Google Play Services")
                    .setNeutralButton("OK", new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    }).show();
            return false;
        }
        return true;
    }

    public void requestLocationPermissionIfRequired(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(activity, permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{permission.ACCESS_FINE_LOCATION},
                        ThagherApp.PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case ThagherApp.PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                }
            }
        }
    }
}
