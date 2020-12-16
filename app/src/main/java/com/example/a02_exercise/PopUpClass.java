package com.example.a02_exercise;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.routes.DatabaseHandler;
import com.example.routes.Route;

/**
 * Most code comes from this article:
 * https://medium.com/@evanbishop/popupwindow-in-android-tutorial-6e5a18f49cc7
 */
public class PopUpClass {

    //PopupWindow display method

    public void showPopupWindow(final View view, Route route) {


        //Create a View object yourself through inflater
        LayoutInflater inflater = (LayoutInflater) view.getContext().getSystemService(view.getContext().LAYOUT_INFLATER_SERVICE);

        View popupView = inflater.inflate(R.layout.popup, null, false);



        //Specify the length and width through constants
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;

        //Make Inactive Items Outside Of PopupWindow
        boolean focusable = false;

        //Create a window with our parameters
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        //Set the location of the window on the screen
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        //Initialize the elements of our window, install the handler

        if (route != null) {

            if (route.getPoints() > 0) {
                TextView test2 = popupView.findViewById(R.id.popup_description);
                test2.setText("Congratulations you completed your trip!");
                TextView points = popupView.findViewById(R.id.popup_points);
                points.setText(route.getPoints() + " points");
            } else {
                TextView test2 = popupView.findViewById(R.id.popup_description);
                test2.setText("Trip was invalid!");
                TextView points = popupView.findViewById(R.id.popup_points);
                points.setVisibility(View.INVISIBLE);
            }
        }

        Button buttonEdit = popupView.findViewById(R.id.popup_button);
        buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });

        //Handler for clicking on the inactive zone of the window

        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //Close the window when clicked
                popupWindow.dismiss();
                return true;
            }
        });
    }

}