package com.example.qfoodly.ui.deposit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.qfoodly.databinding.FragmentDepositBinding;
import com.example.qfoodly.model.ReturnPoint;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class DepositFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private FragmentDepositBinding binding;
    private GoogleMap googleMap;
    private List<ReturnPoint> returnPoints;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        DepositViewModel depositViewModel =
                new ViewModelProvider(this).get(DepositViewModel.class);

        binding = FragmentDepositBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Inicjalizuj mapę
        binding.mapView.onCreate(savedInstanceState);
        binding.mapView.getMapAsync(this);

        // Przygotuj dane automatów zwrotu
        initReturnPoints();

        return root;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        googleMap.setOnMarkerClickListener(this);

        // Dodaj markery dla każdego automatu
        for (ReturnPoint point : returnPoints) {
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(point.getLocation())
                    .title(point.getName())
                    .snippet(point.getAddress())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

            googleMap.addMarker(markerOptions);
        }

        // Ustaw kamerę aby pokazać wszystkie markery
        if (!returnPoints.isEmpty()) {
            LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
            for (ReturnPoint point : returnPoints) {
                boundsBuilder.include(point.getLocation());
            }
            LatLngBounds bounds = boundsBuilder.build();
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
        }
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        // Znajdź punkt zwrotu odpowiadający markerowi
        for (ReturnPoint point : returnPoints) {
            if (point.getLocation().latitude == marker.getPosition().latitude &&
                point.getLocation().longitude == marker.getPosition().longitude) {
                showReturnPointInfo(point);
                return true;
            }
        }
        return false;
    }

    private void showReturnPointInfo(ReturnPoint point) {
        binding.infoCard.setVisibility(View.VISIBLE);
        binding.automatName.setText(point.getName());
        binding.automatAddress.setText(point.getAddress());
        binding.automatDistance.setText("Godziny: " + point.getHours() + " | Ocena: " + point.getRating() + "★");
    }

    private void initReturnPoints() {
        returnPoints = new ArrayList<>();

        // WARSZAWA - Żabka
        returnPoints.add(new ReturnPoint(
                "1",
                "Żabka - Marszałkowska",
                "ul. Marszałkowska 78/80, 00-545 Warszawa",
                new LatLng(52.226564, 21.014067),
                "6:00 - 23:00",
                4.8
        ));

        returnPoints.add(new ReturnPoint(
                "2",
                "Żabka - Praga",
                "ul. Miłosna 37, 03-808 Warszawa",
                new LatLng(52.2256, 21.0695),
                "6:30 - 22:00",
                4.6
        ));

        // WARSZAWA - Lidl
        returnPoints.add(new ReturnPoint(
                "3",
                "Lidl - Warszawa Polska",
                "ul. Polna 23, 00-644 Warszawa",
                new LatLng(52.2286, 21.0404),
                "7:00 - 22:00",
                4.7
        ));

        // KRAKÓW - Żabka
        returnPoints.add(new ReturnPoint(
                "4",
                "Żabka - Stare Miasto",
                "ul. Floriańska 53, 31-019 Kraków",
                new LatLng(50.064167, 19.941389),
                "6:00 - 23:00",
                4.9
        ));

        returnPoints.add(new ReturnPoint(
                "5",
                "Żabka - Nowa Huta",
                "al. Solidarności 152, 31-998 Kraków",
                new LatLng(50.0425, 19.9758),
                "7:00 - 21:00",
                4.5
        ));

        // KRAKÓW - Lidl
        returnPoints.add(new ReturnPoint(
                "6",
                "Lidl - Kraków Tarnów",
                "ul. Bosacka 10, 31-231 Kraków",
                new LatLng(50.0743, 19.9268),
                "7:00 - 22:00",
                4.7
        ));

        // GDAŃSK - Żabka
        returnPoints.add(new ReturnPoint(
                "7",
                "Żabka - Centrum",
                "ul. Długa 59, 80-831 Gdańsk",
                new LatLng(54.3525, 18.6466),
                "6:00 - 22:00",
                4.8
        ));

        returnPoints.add(new ReturnPoint(
                "8",
                "Żabka - Oliwa",
                "ul. Grunwaldzka 472, 80-267 Gdańsk",
                new LatLng(54.4883, 18.5881),
                "7:00 - 21:00",
                4.4
        ));

        // GDAŃSK - Lidl
        returnPoints.add(new ReturnPoint(
                "9",
                "Lidl - Gdańsk",
                "ul. Dąbrowskiego 79, 80-335 Gdańsk",
                new LatLng(54.3798, 18.6398),
                "7:00 - 22:00",
                4.6
        ));

        // WROCŁAW - Żabka
        returnPoints.add(new ReturnPoint(
                "10",
                "Żabka - Śródmieście",
                "ul. Oławska 12, 50-123 Wrocław",
                new LatLng(51.1087, 17.0368),
                "6:00 - 23:00",
                4.8
        ));

        // WROCŁAW - Lidl
        returnPoints.add(new ReturnPoint(
                "11",
                "Lidl - Wrocław",
                "ul. Pilotów 40, 54-132 Wrocław",
                new LatLng(51.1234, 17.0789),
                "7:00 - 22:00",
                4.7
        ));

        // POZNAŃ - Żabka
        returnPoints.add(new ReturnPoint(
                "12",
                "Żabka - Centrum",
                "ul. Paderewskiego 1, 61-643 Poznań",
                new LatLng(52.4089, 16.9245),
                "6:00 - 23:00",
                4.7
        ));

        // POZNAŃ - Lidl
        returnPoints.add(new ReturnPoint(
                "13",
                "Lidl - Poznań",
                "al. Niepodległości 2, 61-713 Poznań",
                new LatLng(52.4123, 16.9456),
                "7:00 - 22:00",
                4.5
        ));

        // ŁÓDŹ - Żabka
        returnPoints.add(new ReturnPoint(
                "14",
                "Żabka - Centrum",
                "ul. Piotrkowska 85, 90-446 Łódź",
                new LatLng(51.7720, 19.4565),
                "6:00 - 23:00",
                4.6
        ));

        // ŁÓDŹ - Lidl
        returnPoints.add(new ReturnPoint(
                "15",
                "Lidl - Łódź",
                "ul. Rewolucji 1905 r. 25, 90-503 Łódź",
                new LatLng(51.7789, 19.4534),
                "7:00 - 22:00",
                4.6
        ));

        // SZCZECIN - Żabka
        returnPoints.add(new ReturnPoint(
                "16",
                "Żabka - Szczecin",
                "ul. Avgustyna Logwina 43, 70-001 Szczecin",
                new LatLng(53.4289, 14.5527),
                "6:00 - 23:00",
                4.7
        ));

        // ZIELONA GÓRA - Lidl
        returnPoints.add(new ReturnPoint(
                "17",
                "Lidl - Zacisze",
                "ul. Zacisze 1A, 65-001 Zielona Góra",
                new LatLng(51.9421, 15.4812),
                "7:00 - 22:00",
                4.7
        ));

        returnPoints.add(new ReturnPoint(
                "18",
                "Lidl - Staszica",
                "ul. Stanisława Staszica 5, 65-069 Zielona Góra",
                new LatLng(51.94677868843086, 15.519795525244461),
                "7:00 - 22:00",
                4.6
        ));

        // ZIELONA GÓRA - Żabka
        returnPoints.add(new ReturnPoint(
                "19",
                "Żabka | Prosto z pieca,",
                "ul. Bohaterów Westerplatte 52, 65-078 Zielona Góra",
                new LatLng(51.94493644170302, 15.512367561460259),
                "6:00 - 23:00",
                4.8
        ));

        returnPoints.add(new ReturnPoint(
                "20",
                "Żabka - Północ",
                "al. Niepodległości 34, 65-001 Zielona Góra",
                new LatLng(51.9512, 15.4956),
                "6:00 - 23:00",
                4.7
        ));

        returnPoints.add(new ReturnPoint(
                "21",
                "Żabka - Zachód",
                "ul. Wyszyńskiego 18, 65-072 Zielona Góra",
                new LatLng(51.9287, 15.4734),
                "6:30 - 22:00",
                4.6
        ));
    }


    @Override
    public void onResume() {
        super.onResume();
        binding.mapView.onResume();
    }

    @Override
    public void onPause() {
        binding.mapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        binding.mapView.onDestroy();
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        binding.mapView.onLowMemory();
    }
}