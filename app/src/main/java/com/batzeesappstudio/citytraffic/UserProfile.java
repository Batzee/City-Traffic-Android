package com.batzeesappstudio.citytraffic;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.UUID;

import pub.devrel.easypermissions.EasyPermissions;

public class UserProfile extends AppCompatActivity {

    private EditText newUserName;
    private EditText newUserEmail;
    private EditText newUserPassword;
    private EditText newUserPasswordConfirm;

    private Button buttonNewName;
    private Button buttonNewEmail;
    private Button buttonNewPassword;
    private Button buttonDeleteUser;
    private Button buttonUploadimage;

    private ImageButton profileImage;
    private ProgressBar profileStatus;

    private FirebaseUser user;
    private FirebaseAuth auth;

    private BroadcastReceiver mDownloadReceiver;
    private Uri mDownloadUri;
    private Uri mFileUri;
    private int RC_TAKE_PICTURE = 101;
    private int RC_STORAGE_PERMS = 102;

    private StorageReference mStrogaeRef;
    private Bitmap finalBitmap;

    String TAG = "User Profile";
    String imageFileName;
    String newUrl;
    SharedPreferences sharedpreferences;
    SharedPreferences.Editor editor;
    public static final String CITYTRAFFICPREFERENCES = "CityTrafficPreference";
    private View profileView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        sharedpreferences = getSharedPreferences(CITYTRAFFICPREFERENCES, Context.MODE_PRIVATE);
        editor = sharedpreferences.edit();

        newUserName = (EditText) findViewById(R.id.editTextNewName);
        newUserEmail = (EditText) findViewById(R.id.editTextNewEmail);
        newUserPassword = (EditText) findViewById(R.id.editTextNewPassword);
        newUserPasswordConfirm = (EditText) findViewById(R.id.editTextConfirmPassword);

        buttonNewName = (Button) findViewById(R.id.buttonChangeName);
        buttonNewEmail = (Button) findViewById(R.id.buttonChangeEmail);
        buttonNewPassword = (Button) findViewById(R.id.buttonChangePassword);
        buttonDeleteUser = (Button) findViewById(R.id.buttonDeleteAccount);
        buttonUploadimage = (Button) findViewById(R.id.buttonChangeImage);

        profileImage = (ImageButton) findViewById(R.id.imageButtonUserImage);
        profileStatus = (ProgressBar) findViewById(R.id.progressBarProfile);
        profileView = findViewById(R.id.userprofileid);

        profileStatus.setVisibility(View.GONE);

        user = FirebaseAuth.getInstance().getCurrentUser();
        auth = FirebaseAuth.getInstance();


        buttonUploadimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profileStatus.setVisibility(View.VISIBLE);
                uploadImageToServer();
            }
        });
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraPermission();
            }
        });

        buttonNewName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConnectingToInternet(UserProfile.this)) {
                    new AlertDialogWrapper.Builder(UserProfile.this)
                            .setTitle("Please Confirm")
                            .setMessage("Are you sure you want to change the user Name?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    chageUserName();
                                    dialog.dismiss();
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).show();
                }
                else {
                    openSnackBar();
                }
            }
        });

        buttonNewEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConnectingToInternet(UserProfile.this)) {
                    new AlertDialogWrapper.Builder(UserProfile.this)
                            .setTitle("Please Confirm")
                            .setMessage("Are you sure you want to change the user Email address?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    changeEmailAddress();
                                    dialog.dismiss();
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).show();
                }

                else {
                    openSnackBar();
                }

            }
        });

        buttonNewPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConnectingToInternet(UserProfile.this)) {
                    new AlertDialogWrapper.Builder(UserProfile.this)
                            .setTitle("Please Confirm")
                            .setMessage("Are you sure you want to change your Password?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    changePassword();
                                    dialog.dismiss();
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).show();
                }

                else {
                    openSnackBar();
                }
            }
        });

        buttonDeleteUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConnectingToInternet(UserProfile.this)) {
                    new AlertDialogWrapper.Builder(UserProfile.this)
                            .setTitle("Please Confirm")
                            .setMessage("Are you sure you want to delete your Account?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    deleteUser();
                                    dialog.dismiss();
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).show();
                }
                else {
                    openSnackBar();
                }
            }
        });
    }


    private void deleteUser() {
        user.delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            goToLoginActivityAfterAccountClose();
                        }
                    }
                });
    }

    private void changePassword() {
        if (newUserPassword.getText().toString().equals(newUserPasswordConfirm.getText().toString())) {
            user.updatePassword(newUserPassword.getText().toString())
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                goToLoginActivity();
                            }
                        }
                    });
        } else {
            Toast.makeText(UserProfile.this, "Password does not match the Confirmation Password", Toast.LENGTH_LONG).show();
        }
    }

    private void changeEmailAddress() {
        user.updateEmail(newUserEmail.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            goToMainActivity();
                        }
                    }
                });
    }

    private void chageUserName() {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(newUserName.getText().toString())
                //.setPhotoUri(Uri.parse("https://example.com/jane-q-user/profile.jpg"))
                .build();
        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            goToMainActivity();
                        }
                    }
                });
    }

    private void chageUserImage(String url) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setPhotoUri(Uri.parse(url))
                .build();
        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            profileStatus.setVisibility(View.GONE);
                            goToMainActivity();
                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        goToMainActivity();
    }

    private void goToMainActivity() {
        Intent intent = new Intent(UserProfile.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void goToLoginActivity() {
        Toast.makeText(UserProfile.this, "Password Successfully Changed, please Login again with the new Password", Toast.LENGTH_LONG).show();
        editor.putBoolean("REMEMBERME", false);
        editor.commit();

        Intent intent = new Intent(UserProfile.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void goToLoginActivityAfterAccountClose() {
        Toast.makeText(UserProfile.this, "Account Successfully Deleted", Toast.LENGTH_LONG).show();
        editor.putBoolean("REMEMBERME", false);
        editor.commit();

        Intent intent = new Intent(UserProfile.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }


    private void cameraPermission() {

        String perm = Manifest.permission.READ_EXTERNAL_STORAGE;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !EasyPermissions.hasPermissions(this, perm)) {
            EasyPermissions.requestPermissions(this, "Adding Photo needs this Permission", RC_STORAGE_PERMS, perm);
            return;
        }

        File folder = new File(Environment.getExternalStorageDirectory() + File.separator + "CityTraffic");
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdir();
        }

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        imageFileName = UUID.randomUUID().toString() + ".jpg";
        File file = new File(Environment.getExternalStorageDirectory() + "/CityTraffic", imageFileName);
        mFileUri = Uri.fromFile(file);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mFileUri);

        startActivityForResult(takePictureIntent, RC_TAKE_PICTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_TAKE_PICTURE) {
            if (resultCode == RESULT_OK) {
                File saveFile = new File(Environment.getExternalStorageDirectory() + "/CityTraffic/" + imageFileName);
                CropImage.activity(Uri.fromFile(saveFile))
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(this);
            } else {
                Log.d("USERPROFILE", "FILE URI is NULL");
                deleteTempImage();
            }
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                finalBitmap = BitmapFactory.decodeFile(resultUri.getPath());
                try {
                    profileImage.setImageBitmap(finalBitmap);
                } catch (Exception ex) {
                    Log.d("Bitmap Error", ex.toString());
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                deleteTempImage();
            }
        } else {
            Toast.makeText(this, "Taking Picture Failed", Toast.LENGTH_SHORT).show();
            deleteTempImage();
        }
    }

    private void uploadImageToServer() {

        deleteTempImage();

        deleteImageFromServer();
    }

    private void deleteImageFromServer() {
        if (user.getPhotoUrl() == null || user.getPhotoUrl().equals("")) {
            uploadSequence();
        } else {
            String xc = sharedpreferences.getString("LASTPROFILEIMAGE","blank");
            mStrogaeRef = FirebaseStorage.getInstance().getReference().child("city_traffic/profile_images/" + xc);
            mStrogaeRef.delete().addOnSuccessListener(new OnSuccessListener() {
                @Override
                public void onSuccess(Object o) {
                    Log.d("Image Del from Server", "Success");
                    uploadSequence();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Log.d("Image Del from Server", "Fail");
                    uploadSequence();
                }
            });
        }
    }

    private void uploadSequence() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        finalBitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
        byte[] data = baos.toByteArray();
        mStrogaeRef = FirebaseStorage.getInstance().getReference().child("city_traffic/profile_images/" + imageFileName);

        UploadTask uploadTask = mStrogaeRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d("Image Upload", exception.toString());
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d("Image Upload", "Success");
                editor.putString("LASTPROFILEIMAGE",imageFileName);
                editor.commit();
                finalBitmap.recycle();
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                chageUserImage(downloadUrl.toString());
            }
        });
    }

    private void deleteTempImage() {
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/CityTraffic/" + imageFileName);
        if (file.exists()) {
            boolean deleted = file.delete();
            Log.d("File Deeleted Status", deleted + "");
        }
    }

    private void turnOnInternet(){
        Intent settingsIntent = new Intent(Settings.ACTION_SETTINGS);
        startActivityForResult(settingsIntent, 9003);
    }


    public boolean isConnectingToInternet(Context context) {

        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
            return true;
        } else {
            Toast.makeText(this, "Seems like No Active Internet, please enable data and try again", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    private void openSnackBar(){
        Snackbar.make(profileView, "No Internet Connectivity", Snackbar.LENGTH_SHORT).setAction("Settings", new View.OnClickListener() {
            @Override
            @TargetApi(Build.VERSION_CODES.M)
            public void onClick(View v) {
                turnOnInternet();
            }
        }).show();
    }

}
