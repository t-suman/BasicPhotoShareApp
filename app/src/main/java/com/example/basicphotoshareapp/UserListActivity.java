package com.example.basicphotoshareapp;

import android.Manifest;
import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UserListActivity extends AppCompatActivity {

    ListView userListView;
    ArrayList<String> userNames=new ArrayList();
    ArrayAdapter arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        setTitle("User's List");

        userListView=findViewById(R.id.list);

        arrayAdapter=new ArrayAdapter(this, android.R.layout.simple_list_item_1,userNames);

        userListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent=new Intent(getApplicationContext(),UserFeedActivity.class);
                intent.putExtra("username",userNames.get(position));
                startActivity(intent);
            }
        });

        ParseQuery<ParseUser> query=ParseUser.getQuery();
        //query.whereNotEqualTo("username",ParseUser.getCurrentUser().getUsername());
        query.whereNotEqualTo("objectId",ParseUser.getCurrentUser().getObjectId());
        query.addAscendingOrder("username");

        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if(e==null){
                    if(objects!=null){
                        for(ParseUser object:objects) {
                            userNames.add(object.getUsername());
                        }
                        userListView.setAdapter(arrayAdapter);
                    }
                }
                else {
                    e.printStackTrace();
                }
            }
        });

    }
    public void getPhoto(){
        Intent intent=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent,1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1&&resultCode==RESULT_OK&&data!=null){
            Uri selectedImage=data.getData();
            try {
                Bitmap bitmap=MediaStore.Images.Media.getBitmap(this.getContentResolver(),selectedImage);
                ByteArrayOutputStream stream=new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG,100,stream);
                byte[] byteArray=stream.toByteArray();

                ParseFile file=new ParseFile("image.png",byteArray);

                ParseObject object=new ParseObject("Images");
                //object.put("objectId",ParseUser.getCurrentUser().getObjectId());
                object.put("username",ParseUser.getCurrentUser().getUsername());
                object.put("image",file);
                object.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if(e==null){
                            Toast.makeText(UserListActivity.this,"Image Recived",Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(UserListActivity.this,"Image can't Recived",Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                });
                //Toast.makeText(UserListActivity.this,"Image Recived",Toast.LENGTH_SHORT).show();

            } catch (IOException e) {
                Toast.makeText(UserListActivity.this,"ERROR",Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==1){
            if(grantResults!=null&&grantResults[0]== PackageManager.PERMISSION_GRANTED){
                getPhoto();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.share_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.share){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED)
                {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
                }
                else
                {
                    getPhoto();
                }
            }
            else{
                getPhoto();
            }
        }
        else if(item.getItemId()==R.id.logout){
            ParseUser.logOut();
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
