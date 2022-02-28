package pdfreader.pdfviewer.pdfscanner.documentreader.officetool.data;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.Ads.Preference;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.R;
import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.GetSet.Tool;

import java.util.ArrayList;
import java.util.List;

public class ToolsData {

    public static void FirebaseData(Activity activity) {
        FirebaseDatabase.getInstance().getReference().child("app_data").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Preference.setGoogle_banner(dataSnapshot.child("banner_id").getValue(String.class));
                Preference.setGoogle_full(dataSnapshot.child("full_id").getValue(String.class));
                Preference.setGoogle_native(dataSnapshot.child("native_id").getValue(String.class));
                Preference.setGoogle_open(dataSnapshot.child("app_open_id").getValue(String.class));
                Preference.setAds_time(dataSnapshot.child("ads_time").getValue(String.class));
                Preference.setAds_name(dataSnapshot.child("ads_name").getValue(String.class));

//                Preference.setAppLovin_banner(dataSnapshot.child("al_banner").getValue(String.class));
//                Preference.setAppLovin_native(dataSnapshot.child("al_native").getValue(String.class));
//                Preference.setAppLovin_full(dataSnapshot.child("al_full").getValue(String.class));

                Preference.setAppLovin_banner("3b997405cd6433ed");
                Preference.setAppLovin_full("f2f732f71d0a6c9e");
                Preference.setAppLovin_native("67ba5142ec7b3d52");

                Preference.setActive_AdsWeek(dataSnapshot.child("weekly_key").getValue(String.class));
                Preference.setActive_AdsMonth(dataSnapshot.child("monthly_key").getValue(String.class));
                Preference.setActive_AdsYear(dataSnapshot.child("yearly_key").getValue(String.class));
                Preference.setBase_key(dataSnapshot.child("base_key").getValue(String.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(activity, "Something went wrong please try again.!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static List<Tool> getTools(Context context) {
        ArrayList<Tool> arrayList = new ArrayList<>();
        arrayList.add(new Tool(1, context.getString(R.string.merge),  R.drawable.ic_tool_merge));
        arrayList.add(new Tool(2, context.getString(R.string.split), R.drawable.ic_tool_split));
        arrayList.add(new Tool(3, context.getString(R.string.extract_images),  R.drawable.ic_tool_library));
        arrayList.add(new Tool(4, context.getString(R.string.save_as_pictures),  R.drawable.ic_tool_photo));
        arrayList.add(new Tool(5, context.getString(R.string.organize_pages),  R.drawable.ic_tool_headline));
        arrayList.add(new Tool(6, context.getString(R.string.edit_metadata),  R.drawable.ic_tool_metadata));
        arrayList.add(new Tool(7, context.getString(R.string.compress),  R.drawable.ic_tool_compress));
        arrayList.add(new Tool(8, context.getString(R.string.extract_text),  R.drawable.ic_tool_text));
        arrayList.add(new Tool(9, context.getString(R.string.images_to_pdf),  R.drawable.ic_tool_pdf));
        context.getString(R.string.protect);
        context.getString(R.string.unprotect);
        context.getString(R.string.stamp);
        return arrayList;
    }
}
