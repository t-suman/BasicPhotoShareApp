package com.example.basicphotoshareapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

public class UserFeedActivity extends AppCompatActivity {

    LinearLayout linearLayout;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_feed);

        Intent intent=getIntent();
        String activeUserName=intent.getStringExtra("username");
        setTitle(activeUserName+"'s feed");

        linearLayout=findViewById(R.id.linear_layout);

        ParseQuery<ParseObject> query=ParseQuery.getQuery("Images");
        query.whereEqualTo("username",activeUserName);
        query.addDescendingOrder("createdAt");

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e==null){
                    if(objects!=null){
                        for(ParseObject object:objects){
                            ParseFile file=object.getParseFile("image");
                            file.getDataInBackground(new GetDataCallback() {
                                @Override
                                public void done(byte[] data, ParseException e) {
                                    if(e==null){
                                        if(data!=null){
                                            Bitmap bitmap= BitmapFactory.decodeByteArray(data,0,data.length);

                                            imageView=new ImageView(getApplicationContext());
                                            imageView.setLayoutParams(new ViewGroup.LayoutParams(
                                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                                    ViewGroup.LayoutParams.WRAP_CONTENT
                                            ));
                                            imageView.setImageBitmap(bitmap);
                                            linearLayout.addView(imageView);
                                        }
                                    }
                                }
                            });
                        }
                    }
                }
            }
        });
    }


}
