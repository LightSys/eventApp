package org.lightsys.eventApp.tools;

import android.app.Activity;

import org.lightsys.eventApp.views.MainActivity;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

/** created by Littlesnowman88 to help Auto Updater know when the main refresh button is pressed.
 * Created on 13 July 2018
 */
public class RefreshPressedHelper extends Observable{

    private static RefreshPressedHelper uniqueInstance;

    private ArrayList<Observer> observers;

    private RefreshPressedHelper() {observers = new ArrayList<>();}

    public static synchronized RefreshPressedHelper getInstance() {
        if (uniqueInstance == null) {
            uniqueInstance = new RefreshPressedHelper();
        }
        return uniqueInstance;
    }

    public synchronized void setRefreshPressed(Activity caller_activity) {
        if (caller_activity.getClass() == MainActivity.class) {
            for (Observer observer : observers) {
                observer.update(null, null);
            }
        }
    }

    @Override
    public synchronized void addObserver(Observer o) {
        if (o.getClass() ==  AutoUpdater.class && ! observers.contains(o)) {
            observers.add(o);
        }
    }

    @Override
    public void notifyObservers() {}//DO NOTHING

    @Override
    public void notifyObservers(Object arg) { }//DO NOTHING


}
