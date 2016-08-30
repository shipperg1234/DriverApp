package in.co.theshipper.www.shipper_driver;


import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AlertDialog;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.provider.ContactsContract.CommonDataKinds.Phone;
import static android.provider.ContactsContract.Contacts;

public class EmergencyContact extends Fragment {

    private DBController controller;
    private static final int REQUEST_SELECT_CONTACT = 100 ;
    private static final int RESULT_OK = -1;
    private View view;
    private ListView listView;
    private SimpleCursorAdapter listAdapter;
    private String selectedNum="";
    private String phoneName = "";
    private Button addButton,deleteButton;
    private Button v;
    public EmergencyContact() {
        // Required empty public constructor
    }

    @Override
    public void onCreate( Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        if(getActivity() != null) {
            controller = new DBController(getActivity());
        }

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (container == null) {
            return null;
        } else {
            view = inflater.inflate(R.layout.fragment_emergency_contact, container, false);
            Fn.SystemPrintLn("Emergency entered onCreateViewElse");
            addButton = (Button) view.findViewById(R.id.addButton);
            //deleteButton = (Button) view.findViewById(R.id.deleteButton);
            listView = (ListView) view.findViewById(R.id.contact_list);

            return view;
    }
}
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
       // uiupdate();

    }
    protected void uiupdate()
    {
        if(getActivity() != null) {
            SQLiteDatabase sqlDB = controller.getWritableDatabase();
            Cursor cursor = sqlDB.rawQuery("SELECT * FROM contactsdb", null);
            listAdapter = new SimpleCursorAdapter(
                    getActivity(),
                    R.layout.phone_name_view,
                    cursor,
                    new String[]{"name", "number"},
                    new int[]{R.id.name, R.id.number},
                    0
            );
            //ListView=(ListView)this.getActivity().findViewById(R.id.list);
            listView.setAdapter(listAdapter);
        }
    }
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        uiupdate();
        Fn.logW("EMERGENCY_CONTACT_FRAGMENT_LIFECYCLE","onActivityCreated called");
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Fn.logD("onItemClick","list clicked at position: "+position);
                View child_view = listView.getChildAt(position);
                TextView number = (TextView) child_view.findViewById(R.id.number);
//                String PhoneNum = number.getText().toString();
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + number.getText().toString()));
                callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(callIntent);
            }
        });

        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

            @Override
            public void onItemCheckedStateChanged(ActionMode mode,
                                                  int position, long id, boolean checked) {

                // Prints the count of selected Items in title
                mode.setTitle(listView.getCheckedItemCount() + " Selected");
            }

            /**
             * Called to report a user click on an action button.
             * @return true if this callback handled the event,
             *          false if the standard MenuItem invocation should continue.
             */
            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                Fn.logD("Entered","onActionItemClicked of EmergencyContact Deletion multiselect");
                if (item.getItemId() == R.id.delete){

                    SparseBooleanArray selected = listView.getCheckedItemPositions();
                    int count = listView.getCount();
                    for (int i=0;i < count;i++){
                        if(selected.get(i))
                        {View delete_view = listView.getChildAt(i);
                            TextView number = (TextView) delete_view.findViewById(R.id.number);
                            Fn.logD("Multi select numbers selected",number.getText().toString());
                            String num = number.getText().toString();
                            num = num.replace("-", "");
                            controller.deleteContacts(num);

                        }
                    }
                    uiupdate();
                    // Close CAB (Contextual Action Bar)
                    mode.finish();
                    return true;
                }
                // return false;

                //return true;
                return false;
            }

            /**
             * Called when action mode is first created.
             * The menu supplied will be used to generate action buttons for the action mode.
             * @param mode ActionMode being created
             * @param menu Menu used to populate action buttons
             * @return true if the action mode should be created,
             *          false if entering this mode should be aborted.
             */
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.menu_emergency_contact_delete, menu);
                return true;
            }

            /**
             * Called when an action mode is about to be exited and destroyed.
             */
            @Override
            public void onDestroyActionMode(ActionMode mode) {
                //  mAdapter.removeSelection();
            }

            /**
             * Called to refresh an action mode's action menu whenever it is invalidated.
             * @return true if the menu or action mode was updated, false otherwise.
             */
            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Contact();
            }
        });

       // listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
         //   @Override
           // public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {}});

                // Capture ListView item click



    }
    protected void Contact() {
        if(getActivity() != null) {
            Intent intent = new Intent(Intent.ACTION_PICK, Uri.parse(Phone.NUMBER));
            intent.setType(Contacts.CONTENT_TYPE);
            if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivityForResult(intent, REQUEST_SELECT_CONTACT);
            }
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(getActivity() != null) {
            if (resultCode == RESULT_OK) {
                switch (requestCode) {
                    case REQUEST_SELECT_CONTACT:
                        Cursor cursor = null;
                        String phoneNumber = "";
                        List<String> allNumbers = new ArrayList<String>();
                        int phoneIdx = 0;
                        int phhoneNx = 0;
                        //String selectedNum = "";
                        try {
                            Uri result = data.getData();
                            String id = result.getLastPathSegment();
                            cursor = getActivity().getContentResolver().query(Phone.CONTENT_URI, null, Phone.CONTACT_ID + "=?", new String[]{id}, null);
                            phoneIdx = cursor.getColumnIndex(Phone.NUMBER);
                            phhoneNx = cursor.getColumnIndex(Phone.DISPLAY_NAME);
                            if (cursor.moveToFirst()) {
                                while (cursor.isAfterLast() == false) {
                                    phoneNumber = cursor.getString(phoneIdx);
                                    phoneName = cursor.getString(phhoneNx);
                                    Fn.logD("phoneNumber", phoneNumber);
                                    Fn.logD("phoneName", phoneName);
                                    allNumbers.add(phoneNumber);
                                    cursor.moveToNext();
                                }
                            } else {
                                //no results actions
                            }
                        } catch (Exception e) {
                            //error actions
                        } finally {
                            if (cursor != null) {
                                cursor.close();
                            }
                            final CharSequence[] items = (allNumbers.toArray(new String[allNumbers.size()]));
                            if (allNumbers.size() > 1) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setTitle("Choose a number");
                                builder.setItems(items, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int item) {
                                        String selectedNumber = items[item].toString();
                                        selectedNumber = selectedNumber.replace("-", "");
                                        selectedNum = selectedNumber;
                                        Fn.logD("number ", selectedNum);
                                        Fn.logD("name ", phoneName);
                                        if (!selectedNum.equals("")) {
                                            HashMap<String, String> queryValues = new HashMap<String, String>();
                                            queryValues.put("name", phoneName);
                                            queryValues.put("number", selectedNum);
                                            controller.insert(queryValues, 4);
                                            uiupdate();
                                        } else {
                                            Toast.makeText(getContext(), "Enter a valid number", Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                });
                                AlertDialog alert = builder.create();
                                alert.show();
                            } else {
                                selectedNum = phoneNumber.toString();
                                selectedNum = selectedNum.replace("-", "");
                                Fn.logD("number ", selectedNum);
                                Fn.logD("name ", phoneName);
                                if (!selectedNum.equals("")) {
                                    HashMap<String, String> queryValues = new HashMap<String, String>();
                                    queryValues.put("name", phoneName);
                                    queryValues.put("number", selectedNum);
                                    controller.insert(queryValues, 4);
                                    uiupdate();
                                } else {
                                    Toast.makeText(getContext(), "Enter a valid number", Toast.LENGTH_SHORT).show();
                                }
                            }

                            try {
                                controller.getAll();
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            if (phoneNumber.length() == 0) {
                                //no numbers found actions
                            }
                        }
                        break;
                }
            } else {

            }
        }
    }
    private void onCallButtonClick(View v) {

        View call = (View) v.getParent();
        TextView number = (TextView) call.findViewById(R.id.number);
        String PhoneNum = number.getText().toString();
        Intent callIntent = new Intent(Intent.ACTION_DIAL);
        callIntent.setData(Uri.parse("tel:"+Uri.encode(PhoneNum.trim())));
        callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(callIntent);
    }
}



