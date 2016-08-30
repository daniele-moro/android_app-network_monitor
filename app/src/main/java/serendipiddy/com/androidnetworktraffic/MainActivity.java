package serendipiddy.com.androidnetworktraffic;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PackageManager pm = getPackageManager();
        List<ApplicationInfo> pinfo = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        ArrayList<ApplicationItem> applications = new ArrayList<>();

        for (ApplicationInfo ai : pinfo) {
            ApplicationItem app = new ApplicationItem(ai.packageName, ai.uid);
            applications.add(app);
        }

        Collections.sort(applications);

        PackageArrayAdapter adapter = new PackageArrayAdapter(this,  R.layout.application_text, applications);
        ListView applicationListView = (ListView) findViewById(R.id.applicationListView);
        applicationListView.setAdapter(adapter);
    }

    /**
     * Class holding the package information of an App, for displaying in a list
     */
    private class ApplicationItem implements Comparable {
        public final String packageName;
        public final int uid;

        public ApplicationItem(String packageName, int uid) {
            this.packageName = packageName;
            this.uid = uid;
        }

        @Override
        public int compareTo(Object o) {
            if (o instanceof ApplicationItem) {
                ApplicationItem other = (ApplicationItem) o;
                return this.packageName.compareTo(other.packageName);
            }
            else
                return 0;
        }
    }

    /**
     * Adapter for displaying package information
     */
    private class PackageArrayAdapter extends ArrayAdapter<ApplicationItem> {

        public PackageArrayAdapter(Context context, int layout, ArrayList<ApplicationItem> pkgs){
            super(context, layout, pkgs);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ApplicationItem ai = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.application_item, parent, false);
            }
            // Lookup view for data population
            TextView pkgName = (TextView) convertView.findViewById(R.id.package_list_name);
            // TextView pkgUID = (TextView) convertView.findViewById(R.id.package_list_uid);
            // Populate the data into the template view using the data object
            pkgName.setText(ai.packageName);
            // pkgUID.setText(new Integer(ai.uid).toString());
            // Return the completed view to render on screen
            return convertView;
        }
    }
}
