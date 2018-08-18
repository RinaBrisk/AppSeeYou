package com.example.rina.testapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.nightonke.boommenu.BoomButtons.ButtonPlaceEnum;
import com.nightonke.boommenu.BoomButtons.HamButton;
import com.nightonke.boommenu.BoomButtons.OnBMClickListener;
import com.nightonke.boommenu.BoomMenuButton;
import com.nightonke.boommenu.ButtonEnum;
import com.nightonke.boommenu.Piece.PiecePlaceEnum;

import java.util.Set;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = MapsActivity.class.getSimpleName();

    private GoogleMap googleMap;
    private BoomMenuButton bmb;

    final int PERSONAL_DATA = 0;
    final int FRIENDS = 1;
    final int SETTINGS = 2;

    private GeoDataClient mGeoDataClient;
    private PlaceDetectionClient mPlaceDetectionClient;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    private boolean mLocationPermissionGranted; // разрешение местоположения предоставлено ?
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    private Location mLastKnownLocation;

    private CameraPosition mCameraPosition;

    private static final int DEFAULT_ZOOM = 18;
    private final LatLng mDefaultLocation = new LatLng(55.6197, 37.46402);  // Мосрентген

    //private static final int M_MAX_ENTRIES = 1;
    private String mLikelyPlaceNames;
    private String mLikelyPlaceAddresses;
    private String mLikelyPlaceAttributions;
    private LatLng mLikelyPlaceLatLngs;

    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // получаем данные, если они сохранены
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        mGeoDataClient = Places.getGeoDataClient(this, null);
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle("Карта");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        bmb = (BoomMenuButton) findViewById(R.id.bmb);
        assert bmb != null;
        bmb.setButtonEnum(ButtonEnum.Ham);
        bmb.setPiecePlaceEnum(PiecePlaceEnum.HAM_3);   // количество элементов bmb, которые отражаются на значке
        bmb.setButtonPlaceEnum(ButtonPlaceEnum.HAM_3); // количество элементов, которое будет появляться на экране при нажатии на bmb

        HamButton.Builder builderPersonalData = new HamButton.Builder()
                .listener(new OnBMClickListener() {
                    @Override
                    public void onBoomButtonClick(int index) {
                        Intent intent = new Intent(MapsActivity.this, PersonalDataActivity.class);
                        startActivity(intent);
                    }
                 })
                .shadowEffect(true)
                .containsSubText(true)
                .normalImageRes(R.drawable.ic_account)    // устанавливает картинку в левый край
                .normalTextRes(R.string.personal_data)
                .textSize(20)
                .imagePadding(new Rect(25, 25, 25, 25))
                .normalColorRes(R.color.colorPersonalData);

        HamButton.Builder builderFriends = new HamButton.Builder()
                .listener(new OnBMClickListener() {
                    @Override
                    public void onBoomButtonClick(int index) {
                        Intent intent = new Intent(MapsActivity.this, FriendsActivity.class);
                        startActivity(intent);
                    }
                })
                .shadowEffect(true)
                .containsSubText(true)
                .normalImageRes(R.drawable.ic_friends)    // устанавливает картинку в левый край
                .normalTextRes(R.string.friends)
                .textSize(20)
                .imagePadding(new Rect(25, 25, 25, 25))
                .normalColorRes(R.color.colorFriends);

        HamButton.Builder builderSettings = new HamButton.Builder()
                .listener(new OnBMClickListener() {
                    @Override
                    public void onBoomButtonClick(int index) {
                        Intent intent = new Intent(MapsActivity.this, SettingsActivity.class);
                        startActivity(intent);
                    }
                })
                .shadowEffect(true)
                .containsSubText(true)
                .normalImageRes(R.drawable.ic_settings)    // устанавливает картинку в левый край
                .normalTextRes(R.string.settings)
                .textSize(20)
                .imagePadding(new Rect(25, 25, 25, 25))
                .normalColorRes(R.color.colorSettings);

        for (int i = 0; i < bmb.getPiecePlaceEnum().pieceNumber(); i++) {
            switch (i){
                case PERSONAL_DATA:
                    bmb.addBuilder(builderPersonalData);
                    break;
                case FRIENDS:
                    bmb.addBuilder(builderFriends);
                    break;
                case SETTINGS:
                    bmb.addBuilder(builderSettings);
                    break;
            }
        }
    }

    // это пока останется здесь. Вскоре туда поместятся мгновенные настройки внешнего вида карты

   // @Override
    //public boolean onCreateOptionsMenu(Menu menu) {
    //    getMenuInflater().inflate(R.menu.current_place_menu, menu);
    //    return true;
   // }

    //получаем текущее место, при нажатии на Получить место
   // @Override
    //public boolean onOptionsItemSelected(MenuItem item) {
    //    if (item.getItemId() == R.id.option_get_place) {
   //        showCurrentPlace();
    //    }
    //    return true;
   // }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;

        updateLocationUI();
        getDeviceLocation();

        googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            //возвращает null, далее вызывается  getInfoContents()
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                // интерфейс для окна
                View infoWindow = getLayoutInflater().inflate(R.layout.custom_info_contents, null);

                TextView title = ((TextView) infoWindow.findViewById(R.id.title));
                title.setText(marker.getTitle());

                TextView snippet = ((TextView) infoWindow.findViewById(R.id.snippet));
                snippet.setText(marker.getSnippet());

                return infoWindow;
            }
        });
        showCurrentPlace();
    }

    // приложение должно запросить разрешение на доступ к местоположению.
    // метод проверяет, дал ли пользователь доступ. Если нет, тогда запрашивает его.
    // если да, результат обрабатывается callBack onRequestPermissionsResult

    private void getLocationPermission() {

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
   }

    // callback method
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    private void updateLocationUI() {
        if (googleMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                googleMap.setMyLocationEnabled(true); // активировать слой MyLocation
                googleMap.getUiSettings().setMyLocationButtonEnabled(true); // и его кнопку отображения
               // googleMap.getUiSettings().setCompassEnabled(true);
            } else {
                googleMap.setMyLocationEnabled(false); // деактивировать слой MyLocation
                googleMap.getUiSettings().setMyLocationButtonEnabled(false); // и его кнопку отображения
               // googleMap.getUiSettings().setCompassEnabled(false);
                mLastKnownLocation = null; // текущее местоположение = null
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getDeviceLocation() {
        //  получение последнего местоположения. В редких случаях бывает = null
        try {
            if (mLocationPermissionGranted) {
                Task locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            // устанавливаем камеру на место текущего расположения на карте
                            mLastKnownLocation = (Location) task.getResult();
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mLastKnownLocation.getLatitude(),
                                           mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                           googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                           googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch(SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void showCurrentPlace() {
        if (googleMap == null) {
            return;
        }

        if (mLocationPermissionGranted) {

            @SuppressWarnings("MissingPermission") final Task<PlaceLikelihoodBufferResponse> placeResult =
                    mPlaceDetectionClient.getCurrentPlace(null);
            placeResult.addOnCompleteListener
                    (new OnCompleteListener<PlaceLikelihoodBufferResponse>() {
                        @Override
                        public void onComplete(@NonNull Task<PlaceLikelihoodBufferResponse> task) {

                             if (task.isSuccessful() && task.getResult() != null) {

                                // получаем места и помещаем их в буффер
                                PlaceLikelihoodBufferResponse likelyPlaces = task.getResult();
                                PlaceLikelihood placeLikelihood = likelyPlaces.get(0);

                                // соunt - колличество записей в выборе мест
                          //      int count;
                         //       if (likelyPlaces.getCount() < M_MAX_ENTRIES) {
                         //           count = likelyPlaces.getCount();
                         //       } else {
                         //           count = M_MAX_ENTRIES;
                        //        }

                            //       int i = 0;
                            //    String mLikelyPlaceNames;
                            //    String mLikelyPlaceAddresses;
                                // String mLikelyPlaceAttributions;
                            //    LatLng mLikelyPlaceLatLngs;

                                //       mLikelyPlaceNames = (String) likelyPlaces.getPlace().getName();
                            //           mLikelyPlaceAddresses[i] = (String) placeLikelihood.getPlace()
                            //                   .getAddress();
                            //           mLikelyPlaceAttributions[i] = (String) placeLikelihood.getPlace()
                            //                   .getAttributions();
                            //           mLikelyPlaceLatLngs[i] = placeLikelihood.getPlace().getLatLng();

                                //for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                                    // места, которые будут показаны пользователю

                                    mLikelyPlaceNames = (String) placeLikelihood.getPlace().getName();
                                    mLikelyPlaceAddresses = (String) placeLikelihood.getPlace()
                                            .getAddress();
                                    mLikelyPlaceAttributions = (String) placeLikelihood.getPlace()
                                            .getAttributions();
                                    mLikelyPlaceLatLngs = placeLikelihood.getPlace().getLatLng();

                                 //   i++;
                                 //   if (i > (M_MAX_ENTRIES - 1)) {
                                 //       break;
                                 //   }
                               // }
                                likelyPlaces.release();
                                //openPlacesDialog();
                                 moveCameraOnTheMap();

                            } else {
                                Log.e(TAG, "Exception: %s", task.getException());
                            }
                        }
                    });
        } else {
            Log.i(TAG, "The user did not grant location permission.");

            googleMap.addMarker(new MarkerOptions()
                    .title(getString(R.string.default_info_title))
                    .position(mDefaultLocation)
                    .snippet(getString(R.string.default_info_snippet)));

            getLocationPermission();
        }
    }

    private void moveCameraOnTheMap(){
        LatLng markerLatLng = mLikelyPlaceLatLngs;
        String markerSnippet = mLikelyPlaceAddresses;
        if (mLikelyPlaceAttributions != null) {
            markerSnippet = markerSnippet + "\n" + mLikelyPlaceAttributions;
        }
        // добавляет маркер для инфо окна
        // показывает информацию о месте
        googleMap.addMarker(new MarkerOptions()
                .title(mLikelyPlaceNames)
                .position(markerLatLng)
                .snippet(markerSnippet));

        // передвинуть камеру на позицию метки на карте
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLatLng,
                DEFAULT_ZOOM));
    }

    private void openPlacesDialog(){
      //  DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
      //      @Override
            // из списка выбирается адрес
      //      public void onClick(DialogInterface dialog, int which) {
                // "which" содержит позицию, выбранного элемента
       //         LatLng markerLatLng = mLikelyPlaceLatLngs;
       //         String markerSnippet = mLikelyPlaceAddresses;
       //         if (mLikelyPlaceAttributions != null) {
       //             markerSnippet = markerSnippet + "\n" + mLikelyPlaceAttributions;
       //         }

                // добавляет маркер для инфо окна
                // показывает информацию о месте
       //         googleMap.addMarker(new MarkerOptions()
       //                 .title(mLikelyPlaceNames)
       //                 .position(markerLatLng)
       //                 .snippet(markerSnippet));

                // передвинуть камеру на позицию метки на карте
       //         googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLatLng,
       //                 DEFAULT_ZOOM));
           // }
      // };
        // показать диалог
        //AlertDialog dialog = new AlertDialog.Builder(this)
        //        .setTitle(R.string.pick_place)
        //        .setItems(mLikelyPlaceNames, listener)
        //        .show();
    }

    // onSaveInstanceState() callback сохраняет позицию карты
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (googleMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, googleMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }
}
