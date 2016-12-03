package com.brain_socket.thagheralrafedain.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.brain_socket.thagheralrafedain.MapActivity;
import com.brain_socket.thagheralrafedain.R;
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


public class FragMap extends Fragment implements OnMapReadyCallback, OnMarkerClickListener, GoogleMap.InfoWindowAdapter, FiltersPickerCallback{


    SupportMapFragment fragment;
    GoogleMap googleMap;

    public enum MAP_TYPE {SEARCH, BRAND}

    MAP_TYPE type;
    ArrayList<LocatableProvider> providers = null;
    ArrayList<WorkshopModel> workshops = null;
    HashMap<String, LocatableProvider> mapMarkerIdToLocatableProvider;
    LocatableProvider selectedLocatableProvider;

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
                        ArrayList<WorkshopModel> recievedShops = (ArrayList<WorkshopModel>) result.get("products");
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
        init();
    }

    private void init() {

        focusMap = false;

        vItemDetailsPreview = (AppWorkshopCard) getView().findViewById(R.id.vProviderPreview);
        mapMarkerIdToLocatableProvider = new HashMap<>();

        handler = new Handler();

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
                selectedLocatableProvider = null;
                hidePreview();
            }
        });

        // initial data load
        selectedLocatableProvider = null;
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
                    LocatableProvider locatableProvider = new LocatableProvider();
                    locatableProvider.provider = brand;
                    locatableProvider.type = LocatableProvider.MarkType.BRAND;
                    boolean isSelected = selectedLocatableProvider != null
                            && locatableProvider.provider != null
                            && locatableProvider.provider.getId().equals(selectedLocatableProvider.provider.getId());

                    locatableProvider.markerOptions = new MarkerOptions()
                                    .position(brand.getCoords())
                                    // if its the highlighted provider then draw it with a different marker icon
                                    .icon(BitmapDescriptorFactory.fromResource(isSelected ? R.drawable.ic_marker_active : R.drawable.ic_marker));
                    this.providers.add(locatableProvider);
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
    private void drawProvidersOnMap(ArrayList<LocatableProvider> providers, boolean focusMap) {
        try {
            if (providers != null && googleMap != null) {
                googleMap.clear();

                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (LocatableProvider provider : providers) {
                    try {

                        Marker marker = googleMap.addMarker(provider.markerOptions);
                        mapMarkerIdToLocatableProvider.put(marker.getId(), provider);
                        LatLng position = provider.markerOptions.getPosition();
                        builder.include(position);
                        if (selectedLocatableProvider != null && provider.provider.getId().equals(selectedLocatableProvider.provider.getId()))
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

    public void displayProviderDetailsPreview(LocatableProvider locatableProvider) {
        if (locatableProvider != null && locatableProvider.provider != null) {
            vItemDetailsPreview.updateUI(locatableProvider.provider);
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
            LocatableProvider locatableProvider = mapMarkerIdToLocatableProvider.get(marker.getId());
            if (locatableProvider != null) {
                displayProviderDetailsPreview(locatableProvider);
                selectedLocatableProvider = locatableProvider;
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
     * update the camer to make the marker at the bottom half of the screen and zoom in to focus on marker
     * by first zoomin in
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

        //calculate distane between middleSouthEdge and screen center map location

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
            if (type == MAP_TYPE.SEARCH)
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
        this.selectedBrands = selectedBrands;
        getNearByBrands();
    }

    public static class LocatableProvider {
        WorkshopModel provider;
        MarkerOptions markerOptions;

        enum MarkType {BRAND, BRANCH}
        MarkType type;
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
}
