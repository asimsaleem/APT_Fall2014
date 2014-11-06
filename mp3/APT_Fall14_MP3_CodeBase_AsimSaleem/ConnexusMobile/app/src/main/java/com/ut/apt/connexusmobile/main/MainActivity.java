package com.ut.apt.connexusmobile.main;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.util.Log;

import android.app.FragmentTransaction;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
//import android.support.v4.app.Fragment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.ut.apt.connexusmobile.R;
import com.ut.apt.connexusmobile.fragments.CameraUploadFragment;
import com.ut.apt.connexusmobile.fragments.CustomCameraFragment;
import com.ut.apt.connexusmobile.fragments.LoginFragment;
import com.ut.apt.connexusmobile.fragments.MySubscribedStreamsFragment;
import com.ut.apt.connexusmobile.fragments.SearchResultsStreamFragment;
import com.ut.apt.connexusmobile.fragments.ViewAllStreamsFragment;
import com.ut.apt.connexusmobile.fragments.ViewMyStreamsFragment;
import com.ut.apt.connexusmobile.fragments.ViewNearbyStreamsFragment;
import com.ut.apt.connexusmobile.fragments.ViewSingleStreamFragment;
import org.apache.http.Header;


import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.provider.MediaStore.Images.Media;

public class MainActivity extends Activity
                          implements LoginFragment.OnViewStreamButtonClickListener,
                                     LoginFragment.OnLoginButtonClickListener,
                                     ViewAllStreamsFragment.OnImageStreamClickListener,
                                     ViewAllStreamsFragment.OnSearchButtonClickListener,
                                     ViewAllStreamsFragment.OnNearbyButtonClickListener,
                                     ViewAllStreamsFragment.OnMySubscribedStreamButtonClickListener,
                                     ViewSingleStreamFragment.OnMorePicturesButtonClickListener,
                                     ViewSingleStreamFragment.OnUploadImageButtonClickListener,
                                     ViewSingleStreamFragment.OnStreamsButtonClickListener,
                                     SearchResultsStreamFragment.OnMoreSearchResultsClickListener,
                                     SearchResultsStreamFragment.OnSearchButtonClickListener,
                                     SearchResultsStreamFragment.OnImageStreamClickListener,
                                     CameraUploadFragment.OnChooseFromLibraryClickListener,
                                     CameraUploadFragment.OnUseCameraButtonClickListener,
                                     CameraUploadFragment.OnUploadButtonClickListener,
                                     CustomCameraFragment.OnStreamsButtonClickListener,
                                     CustomCameraFragment.OnTakePictureClickListener,
                                     CustomCameraFragment.OnUsePictureButtonClickListener,
                                     ViewNearbyStreamsFragment.OnMoreButtonClickListener,
                                     ViewNearbyStreamsFragment.OnViewAllStreamsClickListener,
                                     ViewMyStreamsFragment.OnViewMySubscribedStreamButtonClickListener,
                                     ViewMyStreamsFragment.OnViewMyImageStreamClickListener,
                                     ViewMyStreamsFragment.OnNearbyButtonClickListener,
                                     ViewMyStreamsFragment.OnSearchButtonClickListener,
                                     GooglePlayServicesClient.ConnectionCallbacks,
                                     GooglePlayServicesClient.OnConnectionFailedListener{

    //Set the Application URL
    public static String APP_URL = "http://connexussaleem.appspot.com";

    public static String TAG = "MainActivity";

    public static String USER_NAME;

    // Global variable to hold the current location
    Location mCurrentLocation;
    LocationClient mLocationClient;

    @Override
    protected void onStart() {
        super.onStart();
        // Connect the client.
        mLocationClient.connect();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Location
        mLocationClient = new LocationClient(this, this, this);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction().add(R.id.container, new LoginFragment()).commit();
        }
    }

    @Override
    protected void onStop() {

        // Disconnecting the client invalidates it.
        mLocationClient.disconnect();
        super.onStop();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private AsyncHttpClient httpClient = new AsyncHttpClient();


    /********* LoginFragment Methods - Start *************/
    @Override
    public void onLoginButtonClick(final String loginUser, String loginPassword) {

        Log.e(TAG, "onLoginButtonClick: LOGIN USER ID: " + loginUser + "/LOGIN PASSWORD: "  + loginPassword);

        //Validate if the Login User Id and Password is correct by making the HTTP Call
        String loginUrl = APP_URL + "/login" +
                                    "?userId=" + loginUser +
                                    "&userPwd=" + loginPassword;

        Log.e(TAG, "onLoginButtonClick: LOGIN REQUEST URL Is: " + loginUrl);
        httpClient.get(loginUrl, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                String responseData = new String(response);

                Log.e(TAG, "onLoginButtonClick: Response is: " + responseData);
                if(responseData != null){

                    if(responseData.contains("UIDM")){
                        Toast.makeText(getApplicationContext(),"Please enter a Valid User Id", Toast.LENGTH_SHORT).show();

                    }else if(responseData.contains("PWDM")){
                        Toast.makeText(getApplicationContext(),"Please enter a Valid Password", Toast.LENGTH_SHORT).show();

                    }else if (responseData.contains("SUCCESS")){

                        Log.e(TAG, "onLoginButtonClick: Replacing USER Name with the value: "  + loginUser);
                        USER_NAME = loginUser;

                        Toast.makeText(getApplicationContext(), "Logging into the App now....Please wait", Toast.LENGTH_LONG).show();

                        //Invoke the myStreamsHandler() method to retreive all the streams belonging to the logged in User
                        myStreamsHandler();
                    }else {

                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(  MainActivity.this );

                        // set title
                        alertDialogBuilder.setTitle("Invalid Credentials!!!");

                        // set dialog message
                        alertDialogBuilder
                                .setMessage("Please enter valid User Id/Password and try again")
                                .setCancelable(false)
                                .setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });

                        // create alert dialog
                        AlertDialog alertDialog = alertDialogBuilder.create();

                        // show it
                        alertDialog.show();
                    }
                }
             }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                Log.e(TAG, "There was a problem in retrieving the url : " + e.toString());
            }
        });
    }

    @Override
    public void onViewAllStreamButtonClick() {

        Log.e(TAG, "Inside the Click Handler for View All Streams Button inside MAIN ACTIVITY");
        String requestURL = APP_URL + "/viewAllStream";

        Log.e(TAG, "onViewAllStreamButtonClick: REQUEST URL Is: " + requestURL);
        httpClient.get(requestURL, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {

                String responseData = new String(response);
                Log.e(TAG, "onViewAllStreamButtonClick: Response is: " + responseData);

                // Create new fragment and transaction
                Fragment viewAllStreamsFragment = new ViewAllStreamsFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();

                Log.e(TAG, "onViewAllStreamButtonClick: Set the Response data into the Bundle to pass it to the View All Stream Fragment Handler");

                Bundle bundle = new Bundle();
                bundle.putString("viewAllStreamResponse", responseData);
                viewAllStreamsFragment.setArguments(bundle);

                // Replace whatever is in the fragment_container view with this fragment,
                // and add the transaction to the back stack
                transaction.replace(R.id.container, viewAllStreamsFragment);
                transaction.addToBackStack(null);

                // Commit the transaction
                transaction.commit();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                Log.e(TAG, "onViewAllStreamButtonClick: There was a problem in retrieving the url : " + e.toString());
            }
        });
    }
    /********** Login Fragment Methods - END ************/


    /********** View All Streams Page - START ***********/
    @Override
    public void onImageStreamButtonClick(String streamId) {

        Log.e(TAG, "Inside the onImageStreamButtonClick CallBack Handle. Handle the Image that is Clicked");

        String requestURL = APP_URL + "/viewStream?streamId=" + streamId;
        Log.e(TAG, "onImageStreamButtonClick REQUEST URL Is: " + requestURL);

        httpClient.get(requestURL, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {

                String responseData = new String(response);
                Log.e(TAG, "onImageStreamButtonClick: Response is: " + responseData);

                // Create new fragment and transaction
                Fragment viewSingleStreamFragment = new ViewSingleStreamFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();

                Log.e(TAG, "onImageStreamButtonClick: Set the Response data into the Bundle to pass it to the View All Stream Fragment Handler");
                Bundle bundle = new Bundle();
                bundle.putString("ViewSingleStreamResponse", responseData);
                viewSingleStreamFragment.setArguments(bundle);

                // Replace whatever is in the fragment_container view with this fragment,
                // and add the transaction to the back stack
                Log.e(TAG, "onImageStreamButtonClick: About to replace the current Fragment with the viewSingleStreamFragment");
                transaction.replace(R.id.container, viewSingleStreamFragment);
                transaction.addToBackStack(null);

                // Commit the transaction
                transaction.commit();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                Log.e(TAG, "onImageStreamButtonClick: There was a problem in retrieving the url : " + e.toString());
            }
        });
    }

    @Override
    public void onMySubscribedStreamButtonClick() {

        Log.e(TAG, "Inside the Click Handler for My Subscribed Streams Button inside MAIN ACTIVITY");
        Log.e(TAG, "User Name to be Used is: " + USER_NAME);

        String requestURL = APP_URL + "/mysubscribedstreams?userEmail=" + USER_NAME;

        Log.e(TAG, "onMySubscribedStreamButtonClick REQUEST URL Is: " + requestURL);

        httpClient.get(requestURL, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {

                String responseData = new String(response);
                Log.e(TAG, "onMySubscribedStreamButtonClick: Response is: " + responseData);

                // Create new fragment and transaction
                Fragment mySubscribedStreamsFragment = new MySubscribedStreamsFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();

                Log.e(TAG, "onMySubscribedStreamButtonClick: Set the Response data into the Bundle to pass it to the My Subscribed Streams Fragment Handler");

                Bundle bundle = new Bundle();
                bundle.putString("MySubscribedStreamsResponse", responseData);
                mySubscribedStreamsFragment.setArguments(bundle);

                // Replace whatever is in the fragment_container view with this fragment,
                // and add the transaction to the back stack
                transaction.replace(R.id.container, mySubscribedStreamsFragment);
                transaction.addToBackStack(null);

                // Commit the transaction
                transaction.commit();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                Log.e(TAG, "onMySubscribedStreamButtonClick: There was a problem in retrieving the url : " + e.toString());
            }
        });
    }

    @Override
    public void onNearbyStreamButtonClick() {

        Log.e(TAG, "onNearbyStreamButtonClick: NEARBY STREAM BUTTON WAS CLICKED IN THE FRAGMENT");

        int resultCode =  GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        mCurrentLocation = mLocationClient.getLastLocation();
        Log.e(TAG, "onNearbyStreamButtonClick: Current Location is: " + mCurrentLocation.getLatitude() + "/" + mCurrentLocation.getLongitude());

        String requestURL = APP_URL + "/nearbySearch?cLat=" + mCurrentLocation.getLatitude() + "&cLon=" + mCurrentLocation.getLongitude();

        Log.e(TAG, "onNearbyStreamButtonClick: REQUEST URL Is: " + requestURL);
        httpClient.post(requestURL, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {

                String responseData = new String(response);
                Log.e(TAG, "onNearbyStreamButtonClick: Response is: " + responseData);

                // Create new fragment and transaction
                Fragment nearbyStreamsFragment = new ViewNearbyStreamsFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();

                Log.e(TAG, "onNearbyStreamButtonClick: Set the Response data into the Bundle to pass it to the Nearby Streams Fragment Handler");
                Bundle bundle = new Bundle();
                bundle.putString("nearbyStreamsResponse", responseData);
                nearbyStreamsFragment.setArguments(bundle);

                // Replace whatever is in the fragment_container view with this fragment,
                // and add the transaction to the back stack
                transaction.replace(R.id.container, nearbyStreamsFragment);
                transaction.addToBackStack(null);

                // Commit the transaction
                transaction.commit();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                Log.e(TAG, "onNearbyStreamButtonClick: There was a problem in retrieving the url : " + e.toString());
            }
        });
    }

    @Override
    public void onSearchStreamButtonClick(final String searchTerm) {

        Log.e(TAG, "onSearchStreamButtonClick: SEARCH BUTTON WAS CLICKED IN THE FRAGMENT");
        String requestURL = APP_URL + "/mobileSearch?searchTerm=" + searchTerm;

        Log.e(TAG, "onSearchStreamButtonClick: REQUEST URL Is: " + requestURL);
        httpClient.post(requestURL, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {

                String responseData = new String(response);
                Log.e(TAG, "onSearchStreamButtonClick: Response is: " + responseData);

                // Create new fragment and transaction
                Fragment searchResultsStreamFragment = new SearchResultsStreamFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();

                Log.e(TAG, "onSearchStreamButtonClick: Set the Response data into the Bundle to pass it to the View All Stream Fragment Handler");
                Bundle bundle = new Bundle();
                bundle.putString("searchResultsStreamResponse", responseData);
                bundle.putString("searchTerm", searchTerm);
                searchResultsStreamFragment.setArguments(bundle);

                // Replace whatever is in the fragment_container view with this fragment,
                // and add the transaction to the back stack
                transaction.replace(R.id.container, searchResultsStreamFragment);
                transaction.addToBackStack(null);

                // Commit the transaction
                transaction.commit();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                Log.e(TAG, "onSearchStreamButtonClick: There was a problem in retrieving the url : " + e.toString());
            }
        });
    }
    /********** View All Streams Page - END ************/

    /********** View My Streams Page - START ***********/
    public void myStreamsHandler() {

        Log.e(TAG, "myStreamsHandler: MY STREAMS LOGIN USER ID: " + USER_NAME);

        //Validate if the Login User Id and Password is correct by making the HTTP Call
        String requestURL = APP_URL + "/mystreams" +
                                      "?userEmail=" + USER_NAME ;

        Log.e(TAG, "myStreamsHandler: MY STREAMS REQUEST URL Is: " + requestURL);
        httpClient.get(requestURL, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                String responseData = new String(response);
                Log.e(TAG, "myStreamsHandler: Response is: " + responseData);

                Log.e(TAG, "myStreamsHandler: Parsing the Response that has been received");

                // Create new fragment and transaction
                Fragment viewMyStreamsFragment = new ViewMyStreamsFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();

                Log.e(TAG, "myStreamsHandler: Set the Response data into the Bundle to pass it to the View All Stream Fragment Handler");
                Bundle bundle = new Bundle();
                bundle.putString("myStreamsResponse", responseData);
                viewMyStreamsFragment.setArguments(bundle);

                // Replace whatever is in the fragment_container view with this fragment,
                // and add the transaction to the back stack
                transaction.replace(R.id.container, viewMyStreamsFragment);
                transaction.addToBackStack(null);

                // Commit the transaction
                transaction.commit();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                Log.e(TAG, "There was a problem in retrieving the url : " + e.toString());
            }
        });
    }

    @Override
    public void onViewMySubscribedStreamButtonClick() {
        Log.e(TAG, "onViewMySubscribedStreamButtonClick: MySubscribedStreams BUTTON WAS CLICKED IN THE FRAGMENT. Invoking the method for View All Streams Click here");
        onMySubscribedStreamButtonClick();
    }


    @Override
    public void onViewMyImageStreamButtonClick(String streamId) {
        Log.e(TAG, "onViewMyImageStreamButtonClick: STREAMS BUTTON WAS CLICKED IN THE FRAGMENT. Invoking the method for View All Streams Click here");
        onImageStreamButtonClick(streamId);
    }
    /********* View My Streams Page - END *************/

    /******* View Single Stream Page - START **********/
    @Override
    public void onStreamsButtonClick() {
        Log.e(TAG, "onStreamsButtonClick: STREAMS BUTTON WAS CLICKED IN THE FRAGMENT. Invoking the method for View All Streams Click here");
        onViewAllStreamButtonClick();
    }

    @Override
    public void onMorePicturesButtonClick(String streamId, String cursorValue) {

        Log.e(TAG, "onMorePicturesButtonClick: MORE PICTURES BUTTON WAS CLICKED IN THE FRAGMENT");
        Log.e(TAG, "onMorePicturesButtonClick: StreamName:" + streamId + " and CursorValue is:" + cursorValue);
        Log.e(TAG, "onMorePicturesButtonClick: Inside the onMorePicturesButtonClick CallBack Handle. Handle the Image that is Clicked..");

        String requestURL = APP_URL + "/viewStream?streamId=" + streamId + "&cursor=" + cursorValue;
        Log.e(TAG, "onMorePicturesButtonClick: REQUEST URL Is: " + requestURL);

        httpClient.get(requestURL, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {

                String responseData = new String(response);
                Log.e(TAG, "onMorePicturesButtonClick: Response is: " + responseData);

                // Create new fragment and transaction
                Fragment viewSingleStreamFragment = new ViewSingleStreamFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();

                Log.e(TAG, "onMorePicturesButtonClick: Set the Response data into the Bundle to pass it to the View All Stream Fragment Handler");
                Bundle bundle = new Bundle();
                bundle.putString("ViewSingleStreamResponse", responseData);
                viewSingleStreamFragment.setArguments(bundle);

                // Replace whatever is in the fragment_container view with this fragment,
                // and add the transaction to the back stack
                Log.e(TAG, "onMorePicturesButtonClick: About to replace the current Fragment with the viewSingleStreamFragment");
                transaction.replace(R.id.container, viewSingleStreamFragment);
                transaction.addToBackStack(null);

                // Commit the transaction
                transaction.commit();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                Log.e(TAG, "onMorePicturesButtonClick: There was a problem in retrieving the url : " + e.toString());
            }
        });
    }

    @Override
    public void onUploadImageButtonClick(String streamName) {
        Log.e(TAG, "onUploadImageButtonClick: UPLOAD IMAGE BUTTON CLICKED IN THE FRAGMENT for View Single Stream with StreamName: " + streamName);

        // Create new fragment and transaction
        Fragment cameraUploadFragment = new CameraUploadFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        Log.e(TAG, "onUploadImageButtonClick: Set the Response data into the Bundle to pass it to the View All Stream Fragment Handler");
        Bundle bundle = new Bundle();
        bundle.putString("streamName", streamName);
        cameraUploadFragment.setArguments(bundle);

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack
        transaction.replace(R.id.container, cameraUploadFragment);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
    }

    @Override
    public void onMoreSearchResultsButtonClick() {
        Log.e(TAG, "onMoreSearchResultsButtonClick: MORE SEARCH RESULTS BUTTON CLICKED IN THE FRAGMENT");
    }
    /******* View Single Stream Page - END **********/


    /******** Camera Page - START ******************/
    private void useCustomCamera(){


        // Create new fragment and transaction
        Fragment customCameraFragment = new CustomCameraFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        Log.e(TAG, "onSearchStreamButtonClick: Set the Response data into the Bundle to pass it to the View All Stream Fragment Handler");
        //Bundle bundle = new Bundle();
        //bundle.putString("searchResultsStreamResponse", responseData);
        //bundle.putString("searchTerm", searchTerm);
        //customCameraFragment.setArguments(bundle);

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack
        transaction.replace(R.id.container, customCameraFragment);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
    }

    @Override
    public void onTakePictureButtonClick() {
        Log.e(TAG, "onTakePictureButtonClick: TAKE PICTURE BUTTON CLICKED IN THE FRAGMENT");
    }

    @Override
    public void onUsePictureButtonClick() {
        Log.e(TAG, "onUsePictureButtonClick: USE PICTURE BUTTON CLICKED IN THE FRAGMENT");
    }

    @Override
    public void onMoreButtonClick() {
        Log.e(TAG, "onMoreButtonClick: MORE BUTTON CLICKED IN THE FRAGMENT");
   }

    @Override
    public void onViewAllStreamsButtonClick() {
        Log.e(TAG, "onViewAllStreamsButtonClick: VIEW ALL STREAMS BUTTON CLICKED IN THE FRAGMENT");
        onViewAllStreamButtonClick();
    }
    /******** Camera Page - END ******************/


    /********* Camera Upload - START ************/
    static final int RESULT_LOAD_IMAGE = 100;
    static final int REQUEST_IMAGE_CAPTURE = 200;
    public static final int MEDIA_TYPE_IMAGE = 1;

    private Uri fileUri;
    private Uri selectedFileUri;

    @Override
    public void onChooseFromLibraryButtonClick(String streamName) {
        Log.e(TAG, "onChooseFromLibraryButtonClick: CHOOSE FROM LIBRARY BUTTON CLICKED IN THE FRAGMENT");
        Intent choosePictureIntent = new Intent(Intent.ACTION_PICK, Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(choosePictureIntent, RESULT_LOAD_IMAGE);
    }

    @Override
    public void onUseCameraStreamButtonClick(String streamName) {
        Log.e(TAG, "onUseCameraStreamButtonClick: ON USE CAMERA BUTTON CLICK");
        useDefaultCamera();
    }


    private void useDefaultCamera(){

        Log.e(TAG, "useDefaultCamera: ON USE CAMERA BUTTON CLICK");
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE_SECURE);

        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE); // create a file to save the image
        Log.e(TAG, "useDefaultCamera: FILE URI TO BE uSED Is: " + fileUri);

        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name
        Log.e(TAG, "useDefaultCamera: Triggering the Activity Result code with Data: "+ takePictureIntent.getData());
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);

    }

    private static Uri getOutputMediaFileUri(int type){
        Log.e(TAG, "getOutputMediaFileUri: Inside the getOutputMediaFileUri Method - START");
        return Uri.fromFile(getOutputMediaFile(type));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.e(TAG, "Inside the onActivityResult Method - START");
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            Log.e(TAG, "onActivityResult: In the CAPTURE IMAGE MODE....");
            Cursor cursor = getContentResolver().query(Media.EXTERNAL_CONTENT_URI, new String[]{Media.DATA, Media.DATE_ADDED, MediaStore.Images.ImageColumns.ORIENTATION}, Media.DATE_ADDED, null, "date_added ASC");
            String photoPath = "";
            if(cursor != null && cursor.moveToFirst())
            {
                do {
                    Uri uri = Uri.parse(cursor.getString(cursor.getColumnIndex(Media.DATA)));
                    photoPath = uri.toString();
                    Log.e(TAG, "onActivityResult: PHOTO PATH IS "  + photoPath);
                    Log.e(TAG, "onActivityResult: Camera Case: Cursor Position: " + cursor.getPosition());
                    Log.e(TAG, "onActivityResult: Camera Case: Selected FILE URI is: " + uri);
                    selectedFileUri = uri;

                }while(cursor.moveToNext());
                    cursor.close();
            }

            Log.e(TAG, "onActivityResult: Camera Mode:  Selected Image is being set into the Image View");
            ImageView imageView = (ImageView) findViewById(R.id.imgView);
            imageView.setImageBitmap(BitmapFactory.decodeFile(photoPath));

        }else  if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {

            Log.e(TAG, "onActivityResult: In the SELECT FROM GALLERY MODE....");
            Uri selectedImage = data.getData();
            String[] filePathColumn = {Media.DATA};
            selectedFileUri = selectedImage;
            Log.e(TAG, "onActivityResult: Gallery Mode: Selected File URI: " + selectedFileUri);

            Cursor cursor = getContentResolver().query(selectedImage,filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            Log.e(TAG, "onActivityResult: Gallery Mode:  Selected Image is being set into the Image View");
            ImageView imageView = (ImageView) findViewById(R.id.imgView);
            imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));

            // String picturePath contains the path of selected Image
        }   else if (resultCode == RESULT_CANCELED) {
        // User cancelled the image capture
        } else {
        // Image capture failed, advise user
        }
    }

    @Override
    public void onUploadButtonClick(final String streamName) {

        Log.e(TAG, "onUploadButtonClick: UPLOAD BUTTON CLICKED IN THE FRAGMENT with Stream Name: " + streamName);
        Log.e(TAG, "onUploadButtonClick: Trying NEW Cursor Logic with Selected file URI: " + selectedFileUri);

        if(selectedFileUri == null){
            Toast.makeText(getApplicationContext(),"Please click/select a Picture to Upload first!!!!", Toast.LENGTH_LONG).show();
            return;
        }

        String[] filePathColumn = {Media.DATA};
        String picturePath = "";
        Cursor cursor = getContentResolver().query(selectedFileUri,filePathColumn, null, null, null);
        if(cursor != null) {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            picturePath = cursor.getString(columnIndex);
            Log.e(TAG, "onUploadButtonClick: Gallery Picture Path is: " + picturePath);
            cursor.close();
        }else{
            Log.e(TAG, "onUploadButtonClick: Camera Picture Path is: " + picturePath);
            picturePath = selectedFileUri.toString();
        }

        Log.e(TAG, "onUploadButtonClick: Gallery Mode:  Selected Image is being set into the Image View");
        ImageView imageView = (ImageView) findViewById(R.id.imgView);
        imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));
        try{
        Log.e(TAG, "onUploadButtonClick: Picture Path is:" + picturePath);
        File file = new File(picturePath);

        //Upload the Image into the Blobstore Here.....
        String requestURL = APP_URL + "/fileStream";
        Log.e(TAG, "onUploadButtonClick: Upload URL Created is: " + requestURL);

        final RequestParams urlparams = new RequestParams();
        urlparams.put("streamName", streamName);

        Log.e(TAG, "onUploadButtonClick: File Is being set into the Upload Params now....");
        final RequestParams uploadParams = new RequestParams();
        uploadParams.put("file", file, "image/jpeg");

        Log.e(TAG, "onUploadButtonClick: Invoking the GET method to get the Blobstore URL..");
        httpClient.get(requestURL, urlparams, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String blobstoreURL = new String(responseBody);
                Log.e(TAG, "onUploadButtonClick: Response is: " + blobstoreURL);
                Log.e(TAG, "onUploadButtonClick: About to invoke the POST command with the BLOBSTORE URL: " + blobstoreURL);

                 httpClient.post(blobstoreURL, uploadParams, new AsyncHttpResponseHandler() {

                    @Override
                    public void onStart() {

                        Log.e(TAG, "Starting FILE UPLOAD PROCESS ATTEMPT");
                        Toast.makeText(getApplicationContext(),"Uploading the Image to the Stream: " +streamName, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                        Log.e(TAG, "onUploadButtonClick: SUCCESS");
                        Log.e(TAG, "onUploadButtonClick: Redirecting to the Single Stream Page with Stream Name: " + streamName);
                        onImageStreamButtonClick(streamName);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                        String errorResponseStr = new String(errorResponse);
                        Log.e(TAG, "onUploadButtonClick: Error Message Response is: " + errorResponseStr);
                        Log.e(TAG, "There was a problem in retrieving the url : " + e.toString());
                    }
                });
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
        }catch(Exception e){

        }
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){

        Log.e(TAG, "Inside the getOutputMediaFile Method - START");
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d(TAG, "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else {
            return null;
        }

        Log.e(TAG, "Inside the getOutputMediaFile Method - END");
        return mediaFile;
    }

    /**** Location Service ****/
    @Override
    public void onConnected(Bundle bundle) {
        // Display the connection status
        //Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDisconnected() {
        //Toast.makeText(this, "Disconnected. Please re-connect.",Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            //try {
                // Start an Activity that tries to resolve the error
               // connectionResult.startResolutionForResult(
                 //       this,
                 //       CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
           // } catch (IntentSender.SendIntentException e) {
                // Log the error
             //   e.printStackTrace();
           // }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
           // showErrorDialog(connectionResult.getErrorCode());
        }
    }
    /********** Camera Upload - END ************/
}