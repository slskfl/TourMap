package com.example.tourmap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
//import com.google.android.gms.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.pedro.library.AutoPermissions;
import com.pedro.library.AutoPermissionsListener;

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
public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, AutoPermissionsListener {
    // 전체 화면을 지도로 이용하는 것이 아님 >> AppCompatActivity
    LocationManager manager; //내 위치를 찾기 위함
    LocationListener listener; //내 위치 정보를 받아와서 처리
    private GoogleMap mMap;
    Spinner spTourName;
    static final int NORMAL = 1, HYBRID = 2, MYPOSITION = 3;
    String seoulTourName[] = {"국립중앙박물관", "남산골한옥마을", "예술의전당", "청계천", "63빌딩", "남산타워",
            "경복궁", "김치문화체험관", "서울올림픽기념관", "국립민속박물관", "서대문형무소역사관", "창덕궁"};
    //위도
    Double lat[] = {37.5240867, 37.5591447, 37.4785361, 37.5696512, 37.5198158, 37.5511147,
            37.5788408, 37.5629457, 37.5202976, 37.5815645, 37.5742887, 37.5826041};
    //경도
    Double lng[] = {126.9803881, 126.9936826, 127.0107423, 127.0056375, 126.9403139, 126.9878596,
            126.9770162, 126.9851652, 127.1159236, 126.9789313, 126.9562269, 126.9919376};
    Double latlng[] = new Double[2]; //관광지의 위도와 경도를 담는 배열
    Double myLatLng[] = new Double[2]; //내 위치의 위도와 경도를 담는 배열
    ArrayAdapter<String> adapter;
    int currentPosition; //현재 내가 찾은 위치의 값
    boolean check = false;// 찾는 것이 관광지인지 나의 위치인지를 구분하는 용도(나의 위치가 true)
    //boolean isSecurity=false; // 보안 설정에 대한 여부
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map); //지도는 자동으로 연결
        spTourName = findViewById(R.id.spTourName);
        AutoPermissions.Companion.loadAllPermissions(this, 100);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, seoulTourName);
        spTourName.setAdapter(adapter);
        spTourName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                latlng[0] = lat[position];
                latlng[1] = lng[position];
                currentPosition = position;
                check = false;
                tourMove(latlng);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mapFragment.getMapAsync(this);
            //보안 설정을 허용함
            Handler handler=new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    setMyLocation();
                }
            }, 500);
    }

    //내 위치 찾는 메서드
    void setMyLocation(){
        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        try{
            //GPS로 찾을 경우 >> GPS_PROVIDER
            //네트워크로 찾을 경우 >> NETWORK_PROVIDER
            // GPS 혹은 네트워크 중에서 위치를 빠르게 찾아줌 >> PASSIVE_PROVIDER
            Location location = manager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            if(location != null){
                //내 위치의 위도, 경도를 넘겨줄 때 나의 위치를 찾고 있을 때에는 넘겨줄 수 없음>>else로 이동
                //위치를 찾았을 경우 null값이 아님
                listener=new LocationListener() {
                    @Override
                    public void onLocationChanged(@NonNull Location location) {
                        //location>> 현재 내 위치의 정보가 담긴 변수
                        myLatLng[0]=location.getLatitude();
                        myLatLng[1]=location.getLongitude();
                    }
                };
            } else{
                showToast("내 위치 찾는 중..");
            }
            //위치가 변할 때마다 찾기 (10초마다 거기가 10km가 변하면 MyListener 클래스 다녀오기)
            MyListener myListener=new MyListener();
            manager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 10000, 1, (android.location.LocationListener) myListener);
        } catch (SecurityException e){
            showToast("내 위치를 찾을 수 없습니다.");
        }
    }

    // 퍼미션 결과 메서드
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        AutoPermissions.Companion.parsePermissions(this, 100, permissions,this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        /* 자동으로 시드니의 위도 경도
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/
    }

    //옵션 메뉴 메서드
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, NORMAL, 0, "일반지도");
        menu.add(0, HYBRID, 0, "위성지도");
        menu.add(0, MYPOSITION, 0, "현재위치");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case NORMAL:
                //일반지도
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case HYBRID:
                //위성지도
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
            case MYPOSITION:
                //현재위치
                check=true;
                setMyLocation();
                tourMove(myLatLng);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //토스트 메서드
    void showToast(String msg){
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    //관광지+내위치 이동 메서드
    void tourMove( Double latlngLocation[]){
        String tel[]={"02-2077-9000", "02-2264-4412", "02-580-1300", "02-2290-6114","02-789-5663", "02-3455-9277",
        "02-3700-3900", "02-318-7051", "02-410-1354", "02-3704-3114", "02-360-8590", "02-762-8261"};
        String homepage[]={"http://www.museum.go.kr",
                "http://hanokmaeul.seoul.go.kr",
                "http://www.sac.or.kr",
                "http://www.cheonggyecheon.or.kr",
                "http://www.63.co.kr",
                "http://www.nseoultower.com",
                "http://www.royalpalace.go.kr",
                "http://www.visitseoul.net/kr/article/article.do?_method=view&art_id=49160&lang=kr&m=0004003002009&p=03",
                "http://www.88olympic.or.kr",
                "http://www.nfm.go.kr",
                "http://www.sscmc.or.kr/culture2",
                "http://www.cdg.go.kr" };

        String address[]={"서울특별시 용산구 서빙고로 137 국립중앙박물관",
                "서울특별시 중구 퇴계로34길 28 남산한옥마을",
                "서울특별시 서초구 남부순환로 2364 국립국악원",
                "서울특별시 종로구 창신동",
                "서울특별시 영등포구 63로 50 한화금융센터_63",
                "서울특별시 용산구 남산공원길 105 N서울타워,",
                "서울특별시 종로구 삼청로 37 국립민속박물관",
                "서울특별시 중구 명동2가 32-2",
                "서울특별시 송파구 올림픽로 448 서울올림픽파크텔",
                "서울특별시 종로구 삼청로 37 국립민속박물관",
                "서울특별시 서대문구 통일로 251 독립공원",
                "서울특별시 종로구 율곡로 99"};
        // 배열 >> 0:위도, 1:경도
        LatLng seoul = new LatLng(latlngLocation[0], latlngLocation[1]);
        //mMap.addMarker(new MarkerOptions().position(seoul).title("관광지 위치"));
        //마커 만들기
        MarkerOptions markerOptions=new MarkerOptions();
        markerOptions.position(seoul);
        if(check==true){
            //나의 위치
            markerOptions.title("현재 나의 위치");
            markerOptions.snippet("위도 : "+latlngLocation[0]+ ", 경도 : "+latlngLocation[1]);
        } else if(check==false){
            markerOptions.title(seoulTourName[currentPosition]);
            markerOptions.snippet(address[currentPosition]);
        }

        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker));
        mMap.addMarker(markerOptions).showInfoWindow();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(seoul, 15));
        //마커 클릭 이벤트
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if(check==false) {
                    Uri uri = Uri.parse("tel:" + tel[currentPosition]);
                    Intent intent = new Intent(Intent.ACTION_DIAL, uri);
                    startActivity(intent);
                }
                return false;
            }
        });
        //마커 위에 위치가 적힌 창을 클릭
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                if(check==false) {
                    Uri uri = Uri.parse(homepage[currentPosition]);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    public void onDenied(int i, String[] strings) {
        //isSecurity=false;
    }

    @Override
    public void onGranted(int i, String[] strings) {
        //내 위치 검색 허용할 경우
        //isSecurity=true;
        setMyLocation();
    }

    //내 위치 변했을 때 값을 가져오는 클래스
    class MyListener implements LocationListener{

        @Override
        public void onLocationChanged(@NonNull Location location) {
            // 위치 변동 시 수행
            myLatLng[0]=location.getLongitude();
            myLatLng[1]=location.getLongitude();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            //status>> 상태값을 가져옴
            switch (status){
                case LocationProvider
                        .OUT_OF_SERVICE:
                    //서비스 지역을 벗어남
                    showToast("서비스 지역을 벗어났습니다.");
                    break;
                case LocationProvider
                        .TEMPORARILY_UNAVAILABLE:
                    //서비스 불안정
                    showToast("일시적으로 사용할 수 없습니다.");
                    break;
                case LocationProvider
                        .AVAILABLE:
                    //서비스 지역으로 돌아옴
                    showToast("서비스 사용이 가능합니다.");
                    break;

            }
        }

        @Override
        public void onProviderEnabled(@NonNull String provider) {
            //위치 정보를 켜 놓음
            showToast("현재 서비스 사용 상태");
        }

        @Override
        public void onProviderDisabled(@NonNull String provider) {
            //위치 정보 꺼 놓음
            showToast("현재 서비스 사용 불가 상태");
        }
    }
}