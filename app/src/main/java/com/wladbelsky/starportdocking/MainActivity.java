package com.wladbelsky.starportdocking;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private RecyclerView recyclerView;
    private OrderAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private SwipeRefreshLayout swipeRefreshLayout;

    private List<Order> orders = new ArrayList<>();
    private Bitmap userAvatar;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        recyclerView = (RecyclerView) findViewById(R.id.order_recycler);
        swipeRefreshLayout = findViewById(R.id.swipeContainer);
        setSupportActionBar(toolbar);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new OrderAdapter(getApplicationContext(),orders);
        recyclerView.setAdapter(mAdapter);

        Update();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Update();
            }
        });
        // Configure the refreshing colors
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_orange_dark,
                android.R.color.holo_orange_light,
                android.R.color.holo_orange_dark,
                android.R.color.holo_orange_light);


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                startActivityForResult(new Intent(view.getContext(), RequestLP.class),RequestLP.REQUEST_PAD_CODE);
            }
        });


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

    }

    private void Update()
    {
        UpdateOrders updateOrders = new UpdateOrders();
        updateOrders.execute();


        //test
//        JsonToServer j = new JsonToServer();
//        j.execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == RequestLP.REQUEST_PAD_CODE && resultCode == RESULT_OK)
        {
            Snackbar.make(findViewById(R.id.swipeContainer), getString(R.string.pad_reserve_success,
                    data.getIntExtra("pad",-1)), Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            Update();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_logout) {
            SplashActivity.resetToken();

            startActivity(new Intent(this, SplashActivity.class));
            finish();
        } else if (id == R.id.nav_about) {
            startActivity(new Intent(this, AboutActivity.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    class UpdateOrders extends AsyncTask<Void, Void, Boolean>
    {

        @Override
        protected void onPreExecute()
        {
            swipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                JSONArray jObject = new JSONArray(SplashActivity.getJsonFromServer(SplashActivity.serverIP + "/get_orders.php?token=" + SplashActivity.getToken()));
                orders.clear();
                //mAdapter.clear();
                Log.i("oracle", jObject.toString());//jsonArray.toString());
                for(int i = 0; i < jObject.length(); i++)
                {
                    Calendar start = Calendar.getInstance();
                    Calendar end = Calendar.getInstance();
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy", Locale.ENGLISH);
                    start.setTime(sdf.parse(jObject.getJSONObject(i).getString("start")));
                    end.setTime(sdf.parse(jObject.getJSONObject(i).getString("end")));
                    orders.add(new Order(jObject.getJSONObject(i).getInt("id"),
                            jObject.getJSONObject(i).getInt("pad"),
                            jObject.getJSONObject(i).getString("location"),
                            //jObject.getJSONObject(i).getString("start"),
                            start,
                            end));
                }
                if(userAvatar == null)
                {
                    Log.i("oracle", "loading user data");
                    JSONObject userData = new JSONObject(SplashActivity.postJsonOnServer(SplashActivity.serverIP + "/get_user.php","token="+SplashActivity.getToken()));

                    String base64prep = userData.getString("avatar").replace(" ", "+");
                    byte[] decodedString = Base64.decode(base64prep, Base64.DEFAULT);
                    userAvatar = getCroppedBitmap(BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length));
                    userEmail = userData.getString("email");
                }
            }
            catch (Exception e)
            {
                Log.e("oracle", e.toString());
                return false;
            }
            return true;
        }

        private Bitmap getCroppedBitmap(Bitmap bitmap) {
            Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                    bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(output);

            final int color = 0xff424242;
            final Paint paint = new Paint();
            final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(color);
            // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
            canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                    bitmap.getWidth() / 2, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(bitmap, rect, rect, paint);
            //Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false);
            //return _bmp;
            return output;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAdapter.notifyDataSetChanged();
            swipeRefreshLayout.setRefreshing(false);
            TextView t = findViewById(R.id.nav_name);
            ImageView i = findViewById(R.id.nav_avatar);
            i.setImageBitmap(userAvatar);
            t.setText(userEmail);

        }
    }

    class Order
    {
        private int id;
        private int padNumber;
        private String location;
        private Calendar startDate;
        private Calendar endDate;
        public Order(int id, int pad,String loc, Calendar date_start, Calendar date_end)
        {
            this.id = id;
            padNumber = pad;
            location = loc;
            startDate = date_start;
            endDate = date_end;
        }

        public int getId() {return id;}
        public void setId(int id) {this.id = id;}

        public int getPadNumber() {
            return padNumber;
        }


        public Calendar getEndDate() {
            return endDate;
        }

        public Calendar getStartDate() {
            return startDate;
        }

        public void setEndDate(Calendar endDate) {
            this.endDate = endDate;
        }

        public void setPadNumber(int padNumber) {
            this.padNumber = padNumber;
        }

        public void setStartDate(Calendar startDate) {
            this.startDate = startDate;
        }

        public String getLocation() { return location; }

        public void setLocation(String location) { this.location = location; }
    }


    public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder>
    {
        private LayoutInflater inflater;
        private List<Order> orders;

        OrderAdapter(Context context, List<Order> orders)
        {
            this.orders = orders;
            inflater = LayoutInflater.from(context);
        }

        @Override
        public OrderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_layout, parent, false);//inflater.inflate(R.layout.order_layout, parent, false);
            return new OrderViewHolder(view);
        }

        private String convertTextDate(Calendar calendar)
        {
            return String.format("%1$tb %1$td %1$tY", calendar);
        }

        @Override
        public void onBindViewHolder(OrderAdapter.OrderViewHolder holder, int position) {
            Order order = orders.get(position);

            holder.padNum.setText(String.format("%02d" , order.getPadNumber()));
            holder.startDate.setText(convertTextDate(order.getStartDate()));
            holder.endDate.setText(convertTextDate(order.getEndDate()));
            holder.locationName.setText(order.getLocation());
        }

        @Override
        public int getItemCount() {
            return orders.size();
        }

        public Order getItem(int id) {
            return orders.get(id);
        }

        private class OrderViewHolder extends RecyclerView.ViewHolder {
            private final TextView padNum, startDate, endDate, locationName;
            OrderViewHolder(View view){
                super(view);
                padNum = view.findViewById(R.id.pad_number);
                startDate = view.findViewById(R.id.date_start);
                endDate = view.findViewById(R.id.date_end);
                locationName = view.findViewById(R.id.location_name);
            }
        }
    }

//Additional task. Can be ignored.
    class JsonToServer extends AsyncTask<Void, Void, Boolean>
    {
        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("email", "trigger@spare.os");
                jsonObject.put("pass","this_is_not_gonna_work");
                jsonObject.put("avatar", "this_still_not_gonna_work_but_its_an_example");

                Log.i("oracle", postJsonOnServer(SplashActivity.serverIP + "/post_json.php", "json="+jsonObject.toString()));
                return true;
            }
            catch (Exception e)
            {
                e.printStackTrace();
                return false;
            }
        }

        private String postJsonOnServer(String urlString, String params)
        {
            OutputStream out = null;

            try {
                byte[] data = null;
                InputStream is = null;

                try {
                    URL url = new URL(urlString);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);

                    conn.setRequestProperty("Content-Length", "" + Integer.toString(params.getBytes().length));
                    OutputStream os = conn.getOutputStream();
                    data = params.getBytes("UTF-8");
                    os.write(data);
                    data = null;

                    conn.connect();
                    int responseCode= conn.getResponseCode();
                    Log.i("oracle", responseCode+"");

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    is = conn.getInputStream();

                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        baos.write(buffer, 0, bytesRead);
                    }
                    data = baos.toByteArray();
                } catch (Exception e) {
                    Log.e("oracle", e.getMessage());
                } finally {
                    try {
                        if (is != null)
                            is.close();
                    } catch (Exception ex) {}
                }

                return new String(data, "UTF-8");
            } catch (Exception e) {
                Log.e("oracle",e.getMessage());
            }
            return null;
        }

    }

}

