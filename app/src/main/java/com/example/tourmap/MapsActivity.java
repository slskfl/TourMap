package com.example.tourmap;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
/*  1) SHA1 인증코드
        [cmd]
        입력1>> cd C:\Program Files\Android\Android Studio\jre\bin
        입력2>> keytool.exe -list -v -keystore C:\Users\USER\.android\debug.keystore (틀렸을 경우 오른쪽 방향키만 연속으로 누르기)
        입력3>> 키 저장소 비밀번호 입력 : android
        결과>> SHA1: A4:05:D8:3E:F5:FA:BC:CA:4F:50:0E:61:82:D9:14:A8:F9:15:58:DA
    2) API 발급(지도기능 설정)
        https://console.developers.google.com/apis/dashboard?project=poetic-planet-307805&supportedpurview=project
        설정) Android 앱, 패키지 이름, SHA1 입력 추가, 키 제한(키 제한이 있을 경우 다른 패키지에서 사용할 때 또 만들어야 함)
    3) 개발하는 앱에서 활용
*/
public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {
    // 전체 화면을 지도로 이용하는 것이 아님 >> AppCompatActivity
    private GoogleMap mMap;
    Spinner spTourName;
    String seoulTourName[]={"국립중앙박물관", "남산골한옥마을", "예술의전당", "청계천", "63빌딩", "남산타워",
            "경복궁", "김치문화체험관", "서울올림픽기념관", "국립민속박물관", "서대문형무소역사관", "창덕궁"};
    //위도
    Double lat[]={37.5240867, 37.5591447, 37.4785361,37.5696512, 37.5198158, 37.5511147,
            37.5788408, 37.5629457, 37.5202976, 37.5815645, 37.5742887, 37.5826041};
    //경도
    Double lng[]={126.9803881, 126.9936826, 127.0107423, 127.0056375, 126.9403139, 126.9878596,
            126.9770162, 126.9851652, 127.1159236, 126.9789313, 126.9562269, 126.9919376};
    Double latlng[]=new Double[2]; //위도와 경도를 담는 배열
    ArrayAdapter<String> adapter;
    int currentPosition; //현재 내가 찾은 위치
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map); //지도는 자동으로 연결
        spTourName=findViewById(R.id.spTourName);
        adapter=new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, seoulTourName);
        spTourName.setAdapter(adapter);
        spTourName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                latlng[0]=lat[position];
                latlng[1]=lng[position];
                currentPosition=position;
                tourMove(latlng);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mapFragment.getMapAsync(this);
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        /* 자동으로 시드니의 위도 경도
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/
    }

    //토스트 메서드
    void showToast(String msg){
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }
    //관광지로 이동 메서드
    void tourMove( Double latlngLocation[]){
        // 배열 >> 0:위도, 1:경도
        LatLng seoul = new LatLng(latlngLocation[0], latlngLocation[1]);
        //mMap.addMarker(new MarkerOptions().position(seoul).title("관광지 위치"));
        //마커 만들기
        String address[]={"서울특별시 용산구 서빙고로 137 국립중앙박물관,02-2077-9000",
                "서울특별시 중구 퇴계로34길 28 남산한옥마을,02-2264-4412",
                "서울특별시 서초구 남부순환로 2364 국립국악원,02-580-1300",
                "서울특별시 종로구 창신동,02-2290-6114",
                "서울특별시 영등포구 63로 50 한화금융센터_63,02-789-5663",
                "서울특별시 용산구 남산공원길 105 N서울타워,02-3455-9277",
                "서울특별시 종로구 삼청로 37 국립민속박물관,02-3700-3900",
                "서울특별시 중구 명동2가 32-2,02-318-7051",
                "서울특별시 송파구 올림픽로 448 서울올림픽파크텔,02-410-1354",
                "서울특별시 종로구 삼청로 37 국립민속박물관,02-3704-3114",
                "서울특별시 서대문구 통일로 251 독립공원,02-360-8590",
                "서울특별시 종로구 율곡로 99,02-762-8261"
        };
        MarkerOptions markerOptions=new MarkerOptions();
        markerOptions.position(seoul);
        markerOptions.title(seoulTourName[currentPosition]);
        markerOptions.snippet(address[currentPosition]);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker));
        mMap.addMarker(markerOptions).showInfoWindow();

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(seoul, 15));
    }
}