package serendipiddy.com.androidnetworktraffic;

import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public final static int GET_NEW_APP_UID = 1923; // putting here keeps the intent references locally consistent
    public final String TAG = "networkUsageMain";
    private final String DATE_FORMAT = "dd/MM/yy hh:mm:ss.SSS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getNewAppUID();
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
            }
        });


    }

    /**
     * Get a new app
     */
    private void getNewAppUID() {
        Intent chooseUID = new Intent(this, InstalledAppList.class);
        startActivityForResult(chooseUID, GET_NEW_APP_UID);
    }

    /**
     * Get the result of a invocation of InstalledAppList or other
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GET_NEW_APP_UID) {
            if(resultCode == AppCompatActivity.RESULT_OK){
                String appName = data.getStringExtra(InstalledAppList.EXTRA_NAME);
                String appLabel = data.getStringExtra(InstalledAppList.EXTRA_LABEL);
                long installTime = data.getLongExtra(InstalledAppList.EXTRA_INSTALL_TIME, -1);
                int appUID = data.getIntExtra(InstalledAppList.EXTRA_UID, -1);
                Log.d(TAG, appName +" "+ appUID);
                // TODO add it to the current list of apps displayed
                addApplicationLabelToMainView(appName, appLabel, appUID, installTime);
                // TODO add it to the current list of apps, in memory
            }
            if (resultCode == AppCompatActivity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }

    /**
     * Adds an item to the list of selected apps to monitor
     * @param name
     * @param label
     * @param uid
     */
    private void addApplicationLabelToMainView(String name, String label, int uid, long installTime) {
        TextView mainText = (TextView) findViewById(R.id.selectedApplicationsView);
        mainText.setText(label +" "+ uid);
        testingUsageMon(name, uid, installTime);
    }

    /**
     * Temporary method for figuring out how the NetworkStats system works.
     * Prints some results for the selected uid.
     * @param uid
     */
    private void testingUsageMon(String appName, int uid, long installTime) {
        // TODO remember the end time of the last read, then only collect updated stats, with option to refresh/collect from scratch

        // String builders to capture output
        StringBuilder sb_main = new StringBuilder();
        StringBuilder sb_wifi = new StringBuilder();
        StringBuilder sb_mobile = new StringBuilder();

        sb_main.append(appName+"\n");

        Calendar cal_from = Calendar.getInstance();
        Calendar cal_to = Calendar.getInstance();
        // cal_from.add(Calendar.HOUR_OF_DAY, -2);
        cal_from.setTimeInMillis(installTime);
        cal_to.setTimeInMillis(System.currentTimeMillis());
        // TODO try adjusting granularity to within a few seconds, and see how accurate it is

        long start = cal_from.getTimeInMillis();
        long end = cal_to.getTimeInMillis();
        sb_main.append("FROM: " + cal_from.getTime() + "\nTO:   " + cal_to.getTime() +"\n");
        sb_main.append("FROM: " + start + "\nTO:   " + end +"\n");

        NetworkStats queryNetworkStatsWifi = getNetworkStats(start, end, uid, ConnectivityManager.TYPE_WIFI);
        NetworkStats queryNetworkStatsData = getNetworkStats(start, end, uid, ConnectivityManager.TYPE_MOBILE);
        NetworkStats.Bucket bucket = new NetworkStats.Bucket(); // temporary, reusable bucket

        // TODO handle case where usage permission isn't granted

        String type_wifi = "wifi";
        String type_mobile = "mobile";

        // variables to hold usage summary
        long totalRxPackets = 0;
        long totalTxPackets = 0;
        long totalRxBytes = 0;
        long totalTxBytes = 0;

        long totalWifiRxPackets = 0;
        long totalWifiTxPackets = 0;
        long totalWifiRxBytes = 0;
        long totalWifiTxBytes = 0;
        long totalMobileRxPackets = 0;
        long totalMobileTxPackets = 0;
        long totalMobileRxBytes = 0;
        long totalMobileTxBytes = 0;

        // Iterate through the Wifi and Mobile buckets, collecting bucket and summary values
        while (queryNetworkStatsWifi.hasNextBucket()) {
            queryNetworkStatsWifi.getNextBucket(bucket);
            String startTime = getDate(bucket.getStartTimeStamp(),DATE_FORMAT);
            String endTime = getDate(bucket.getEndTimeStamp(),DATE_FORMAT);
            sb_wifi.append("\""+startTime + "\" \"" + endTime +"\" "
                    + bucket.getStartTimeStamp() + " " + bucket.getEndTimeStamp() + " "
                    + bucket.getRxPackets() + " " + bucket.getTxPackets() + " "
                    + bucket.getRxBytes() + " " + bucket.getTxBytes() + " " + type_wifi + "\n");
            totalRxPackets += bucket.getRxPackets();
            totalRxBytes += bucket.getRxBytes();
            totalTxPackets += bucket.getTxPackets();
            totalTxBytes += bucket.getTxBytes();
        }
        totalWifiRxPackets = totalRxPackets;
        totalWifiTxPackets = totalTxPackets;
        totalWifiRxBytes = totalRxBytes;
        totalWifiTxBytes = totalTxBytes;

        // Iterate through the Wifi buckets
        while (queryNetworkStatsWifi.hasNextBucket()) {
            queryNetworkStatsWifi.getNextBucket(bucket);
            String startTime = getDate(bucket.getStartTimeStamp(),DATE_FORMAT);
            String endTime = getDate(bucket.getEndTimeStamp(),DATE_FORMAT);
            sb_mobile.append("\""+startTime + "\" \"" + endTime +"\" "
                    + bucket.getStartTimeStamp() + " " + bucket.getEndTimeStamp() + " "
                    + bucket.getRxPackets() + " " + bucket.getTxPackets() + " "
                    + bucket.getRxBytes() + " " + bucket.getTxBytes() + " " + type_mobile + "\n");
            totalRxPackets += bucket.getRxPackets();
            totalRxBytes += bucket.getRxBytes();
            totalTxPackets += bucket.getTxPackets();
            totalTxBytes += bucket.getTxBytes();
        }
        totalMobileRxPackets = totalRxPackets - totalWifiRxPackets;
        totalMobileTxPackets = totalTxPackets - totalWifiTxPackets;
        totalMobileRxBytes = totalRxBytes - totalWifiRxBytes;
        totalMobileTxBytes = totalTxBytes - totalWifiTxBytes;

        sb_main.append("\n### Total Usage ###\n");
        appendSummary(sb_main, totalRxPackets, totalTxPackets, totalRxPackets, totalRxBytes);
        sb_main.append("\n### Mobile Usage ###\n");
        appendSummary(sb_main, totalMobileRxPackets, totalMobileTxPackets, totalMobileRxBytes, totalMobileTxBytes);
        sb_main.append("\n### Wifi Usage ###\n");
        appendSummary(sb_main, totalWifiRxPackets, totalWifiTxPackets, totalWifiRxBytes, totalWifiTxBytes);

        TextView mainText = (TextView) findViewById(R.id.selectedApplicationsView);
        mainText.setText(sb_main.toString());
    }

    /**
     * Outputs the given summary to given string builder
     */
    private void appendSummary(StringBuilder sb, long rxPkt, long txPkt, long rxByte, long txByte) {
        sb.append("Packets: \n");
        sb.append("    "+rxPkt+"Rx\n");
        sb.append("    "+txPkt+"Tx\n");
        sb.append("Bytes: \n");
        sb.append("    "+rxByte+"Rx\n");
        sb.append("    "+txByte+"Tx\n");
        sb.append("Average Rate:\n");
        sb.append("    "+rxByte/rxPkt+"Rx\n");
        sb.append("    "+txByte/rxPkt+"Tx\n");
    }

    /**
     * Get the network stats object to extract usage data from
     * @param type
     * @return
     */
    private NetworkStats getNetworkStats(long start, long end, int uid, int type) {
        NetworkStatsManager networkStatsMan = (NetworkStatsManager) getSystemService(Context.NETWORK_STATS_SERVICE);
        try {
            return networkStatsMan.queryDetailsForUid(type, "", start, end, uid);
        }
        catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Retrieves the list of monitored UID/apps, checks if they're valid, then displays them
     */
    private void populateMonitoredAppList() {

    }

    /**
     * Return date in specified format.
     * @param milliSeconds Date in milliseconds
     * @param dateFormat Date format
     * @return String representing date in specified format
     */
    public static String getDate(long milliSeconds, String dateFormat)
    {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }
}
