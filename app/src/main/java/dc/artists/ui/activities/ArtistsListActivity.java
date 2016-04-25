package dc.artists.ui.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import dc.artists.ui.fragments.ArtistsListFragment;
import dc.artists.R;

public class ArtistsListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);

        ActionBar toolbar = getSupportActionBar();
        if (null != toolbar) {
            toolbar.setTitle(R.string.artists);
        }

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);

        if (null == fragment) {
            fragment = ArtistsListFragment.newInstance();
            fm.beginTransaction().replace(R.id.fragment_container, fragment).commit();
        }
    }
}