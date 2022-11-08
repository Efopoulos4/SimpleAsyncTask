package com.example.simpleasynctask;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity implements DefaultLifecycleObserver {

    private static final String TAG = "paok";
    private TextView mTextView;
    private ProgressBar mProgressBar;
    private static final String TEXT_STATE = "currentText";
    private static final String PROGRESSBAR_STATE = "currentBarState";
    private static final String MAX_STATE = "currentMaxState";
    private static AsyncTask<Void, Integer, String> task = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView = findViewById(R.id.textView1);
        mProgressBar = findViewById(R.id.progressBar);
        
        if (savedInstanceState != null) {
            mTextView.setText(savedInstanceState.getString(TEXT_STATE));
            mProgressBar.setMax(savedInstanceState.getInt(MAX_STATE));
            mProgressBar.setProgress(savedInstanceState.getInt(PROGRESSBAR_STATE));
            //While rotating the device or closing the app we want to create new task from where the last task ended
            //But only if the progress bar is running otherwise is going to start without our permission
            if (!(mProgressBar.getProgress() == 0 || mProgressBar.getProgress() == mProgressBar.getMax())) {
                task = new SimpleAsyncTask(mTextView, mProgressBar).execute();
            }
        }
        
        Lifecycle lifecycle = ProcessLifecycleOwner.get().getLifecycle();
        lifecycle.removeObserver(this);
        lifecycle.addObserver(this);
    }

    public void startTask(View view) {
        if (task != null) {
            task.cancel(true);
        }
        mTextView.setText(R.string.napping);
        mProgressBar.setProgress(0);
        task = new SimpleAsyncTask(mTextView, mProgressBar).execute();
    }

    /**
     * We save the state before the activity is being destroyed and force the background running to stop
     * @param outState
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        Log.d(TAG, "onSaveInstanceState: ");
        super.onSaveInstanceState(outState);
        outState.putString(TEXT_STATE, mTextView.getText().toString());
        outState.putInt(MAX_STATE, mProgressBar.getMax());
        outState.putInt(PROGRESSBAR_STATE, mProgressBar.getProgress());
        if (task != null) {
            task.cancel(true);
        }
    }

    /**
     * OnResume of the Activity
     * Create new task after screen rotation
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (task != null) {
            task = new SimpleAsyncTask(mTextView, mProgressBar).execute();
        }
    }

    /**
     * OnResume of the APP
     * Create new task after closing and restore the app
     */
    @Override
    public void onResume(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onResume(owner);
        if (task != null) {
            task = new SimpleAsyncTask(mTextView, mProgressBar).execute();
        }
    }
}