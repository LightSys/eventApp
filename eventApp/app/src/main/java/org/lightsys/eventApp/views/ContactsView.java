package org.lightsys.eventApp.views;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.core.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.lightsys.eventApp.R;
import org.lightsys.eventApp.data.ContactInfo;
import org.lightsys.eventApp.data.Info;
import org.lightsys.eventApp.tools.LocalDB;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by otter57 on 3/29/17.
 *
 * displays contact information and emergency contact info
 */

public class ContactsView extends Fragment {

    private LocalDB db;
    private ArrayList<Info> contactsInfo;
    private LinearLayout mainLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.listview_no_line, container, false);
        getActivity().setTitle("Contact Information");

        db = new LocalDB(getContext());
        contactsInfo = db.getContactPage();
        mainLayout = v.findViewById(R.id.layout);

        // display TextView Items
        ListView infoListView = v.findViewById(R.id.infoList);
        ArrayList<HashMap<String, String>> itemList = generateListItems();
        String[] from = {"header", "text"};
        int[] to = {R.id.headerText, R.id.text};
        SimpleAdapter adapter = new SimpleAdapter(getActivity(), itemList, R.layout.info_list_item, from, to ){
            @Override
            public View getView(int position, View v, ViewGroup parent) {
                View mView = super.getView(position, v, parent);

                TextView groupNum = mView.findViewById(R.id.headerText);
                groupNum.setTextColor(Color.parseColor(db.getThemeColor("themeMedium")));
                return mView;
            }
        };

        infoListView.setAdapter(adapter);
        infoListView.setSelector(android.R.color.transparent);

        //display ListView Items
        for (Info c : contactsInfo) {
            if (c.getId() == 1) { //checks if item is for listView
                generateContactList(c);
            }
        }

        return v;
    }

    private void generateContactList(Info c){

        // initialize list for contact info
        ListView list = new ListView(this.getContext(), null, R.style.list_view_no_divider);

        //get address info
        ArrayList<ContactInfo> contact = new ArrayList<>();
        String addresses [] = c.getBody().split(":");
        for (String a : addresses) {
            contact.add(db.getContactByName(a));
        }

        //display header
        TextView txt = (TextView) View.inflate(this.getContext(), R.layout.header, null);
        txt.setText(c.getHeader());
        txt.setTextColor(Color.parseColor(db.getThemeColor("themeMedium")));
        mainLayout.addView(txt);

        //display ListView items
        ArrayList<HashMap<String, String>> hmContact = generateAddressItems(contact);
        String[] from = {"info"};
        int[] to = {R.id.infoText};

        SimpleAdapter adapter = new SimpleAdapter(getActivity(), hmContact, R.layout.contact_list_item, from, to);

        list.setAdapter(adapter);
        list.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        list.setOnItemClickListener(new OnAddressClicked (contact));
        list.setSelector(ResourcesCompat.getDrawable(getResources(), R.drawable.button_pressed, null));

        mainLayout.addView(list);
    }

    private ArrayList<HashMap<String, String>> generateAddressItems(ArrayList<ContactInfo> contact) {
        ArrayList<HashMap<String, String>> aList = new ArrayList<>();
        for (ContactInfo c : contact) {
            HashMap<String, String> hm = new HashMap<>();
            String info = c.getName();
            if (info == null || info.equals("null")) info = "";
            info += (c.getAddress()==null)? "":"\n"+ c.getAddress();
            info += (c.getPhone()==null)? "":"\n"+ c.getPhone();

            hm.put("info", info);

            aList.add(hm);
        }
        return aList;
    }

    private ArrayList<HashMap<String, String>> generateListItems() {
        ArrayList<HashMap<String, String>> aList = new ArrayList<>();

        for (Info c : contactsInfo) {
                HashMap<String, String> hm = new HashMap<>();

            if (c.getId() == 0) { //checks if item is for textView
                hm.put("header", c.getHeader());
                hm.put("text", c.getBody());
                hm.put("type", Integer.toString(c.getId()));

                aList.add(hm);
            }
        }
        return aList;
    }


    //Asks user if they want to go to maps or phone
    //performs desired action
    private class OnAddressClicked implements AdapterView.OnItemClickListener {

        final ArrayList<ContactInfo> contact;
        private OnAddressClicked (ArrayList<ContactInfo> contact) {
            this.contact = contact;

        }
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id){
            final String address = contact.get(position).getAddress();
            final String phone = contact.get(position).getPhone();
            String title = "Go to maps or phone?";
            title = (address == null)?"Go to phone?":title;
            title = (phone == null)?"Go to maps?":title;

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder
                    .setCancelable(false)
                    .setMessage(title);
                    if (address!=null) {
                        builder.setPositiveButton(R.string.map_button, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                                        Uri.parse("google.navigation:q=" + address));
                                try {
                                    startActivity(intent);
                                } catch (android.content.ActivityNotFoundException ex) {
                                    Toast.makeText(getActivity(), "No Map app Found", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                    if (phone !=null) {
                        builder.setNegativeButton(R.string.phone_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String phoneCall = "+" + phone.replaceAll("[^0-9.]", "");
                                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phoneCall, null));
                                try {
                                    startActivity(intent);
                                } catch (android.content.ActivityNotFoundException ex) {
                                    Toast.makeText(getActivity(), "no Phone app found", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                    builder.setNeutralButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }

    }
}



