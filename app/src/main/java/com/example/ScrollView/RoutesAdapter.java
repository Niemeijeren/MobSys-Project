package com.example.ScrollView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.RouteTracking.Utils;
import com.example.a02_exercise.R;
import com.example.routes.DatabaseHandler;
import com.example.routes.LocationPoint;
import com.example.routes.Route;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RoutesAdapter extends RecyclerView.Adapter<RoutesAdapter.ViewHolder> {

    private ArrayList<Route> routes;
    private ItemClickListener mClickListener;
    private Utils utils = new Utils();

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private View view;
        private final TextView itemId;
        private final TextView content;
        private final TextView length;
        private final TextView time;
        public Route route;


        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
            // Should st
            this.view = view;

            itemId = (TextView) view.findViewById(R.id.item_ID);
            content = (TextView) view.findViewById(R.id.content);
            length = (TextView) view.findViewById(R.id.length_of_route);
            time = (TextView) view.findViewById(R.id.time);
        }

        public View getTextView() {
            return view;
        }

    }

    public void removeItem(int position) {
        routes.remove(position);
        notifyItemRemoved(position);
    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param routes List<Route> containing the data to populate views to be used
     * by RecyclerView.
     */
    public RoutesAdapter(ArrayList<Route> routes) {
        this.routes = routes;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.route_item, viewGroup, false);
        return new ViewHolder(view);
    }


    // Replace the contents of a view (invoked by the layout manager)
    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        viewHolder.route = routes.get(position);
        viewHolder.itemId.setText((position + 1) + "");
        double length = 0;
        List<LocationPoint> locations = viewHolder.route.getLocationPoints();
        if (locations.size() > 10) {
            LocationPoint first = locations.get(0);
            locations.remove(0);
            length = 0;
            for (LocationPoint locationPoint: locations) {
                length += utils.calculateDistance(first, locationPoint);
                first = locationPoint;
            }
        }


        if (viewHolder.route.getLocationPoints().size() > 10) {
            Long firstTimeStamp = (viewHolder.route.getLocationPoints().get(0).getTimeStamp());
            Long lastTimeStamp = (viewHolder.route.getLocationPoints().get(viewHolder.route.getLocationPoints().size() -1).getTimeStamp());

            Long millis = lastTimeStamp - firstTimeStamp;

            viewHolder.time.setText(String.format("%02d min, %02d sec",
                    TimeUnit.MILLISECONDS.toMinutes(millis),
                    TimeUnit.MILLISECONDS.toSeconds(millis) -
                     TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
            ));
        } else {
            viewHolder.time.setText("N/A");
        }


        //to km
        length = length/1000;

        DecimalFormat df = new DecimalFormat("###.##");

        viewHolder.length.setText(df.format(length) + " km");

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");
        Date resultDate = new Date(viewHolder.route.timeStart());

        viewHolder.content.setText(sdf.format(resultDate) + "");

        viewHolder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mClickListener != null) mClickListener.onItemClick(view, position);
            }
        });

        viewHolder.view.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View view) {
                if (mClickListener != null) {
                    mClickListener.onLongPress(view, position);
                    return true;
                }
                return false;
            }
        });

    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // convenience method for getting data at click position
    public Route getRoute(int id) {
        return routes.get(id);
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
        void onLongPress(View view, int position);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return routes.size();
    }
}

