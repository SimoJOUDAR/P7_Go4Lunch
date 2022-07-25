package fr.joudar.go4lunch.domain.core;

import android.Manifest;
import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import fr.joudar.go4lunch.R;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;


public class LocationPermissionHandler implements EasyPermissions.PermissionCallbacks {

  private static final int PERMISSION_REQUEST_CODE = 666;
  private final Activity activity;

  private static final List<Runnable> onPermissionGrantedListeners =
      new ArrayList<>();

  @Inject public LocationPermissionHandler(Activity activity) {
    this.activity = activity;
  }

  public boolean hasPermission() {
    return EasyPermissions.hasPermissions(activity, Manifest.permission.ACCESS_FINE_LOCATION);
  }

  // Asks the user for Location Permission and then runs the arg runnable
  public void requestPermission(Runnable onPermissionGranted) {
    Log.d("LocationPermHandler", "requestPermission");
    onPermissionGrantedListeners.add(onPermissionGranted);
    requestPermissionBuilder();
  }

  // Builds the Dialog
  public void requestPermissionBuilder() {
    EasyPermissions.requestPermissions(
        new PermissionRequest.Builder(
                activity, PERMISSION_REQUEST_CODE, Manifest.permission.ACCESS_FINE_LOCATION)
            .setRationale(R.string.location_permission_request_txt)
            .setNegativeButtonText(R.string.location_permission_request_negative_btn_txt)
            .setPositiveButtonText(R.string.location_permission_request_positive_btn_txt)
            .build());
  }

  /***********************************************************************************************
   ** Overridden intrinsic EasyPermission callbacks
   **********************************************************************************************/
  @Override
  public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
    for (Runnable listener : onPermissionGrantedListeners) {
      Log.d("LocationPermHandler", "onPermissionsGranted : " + listener);
      if (listener != null) listener.run();
    }
  }

  @Override
  public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
    if (requestCode == PERMISSION_REQUEST_CODE) {
      requestPermissionBuilder();
    }
  }

  @Override
  public void onRequestPermissionsResult(
      int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
  }

}
