package dc.artists.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import java.util.List;

import dc.artists.R;
import dc.artists.model.ArtistItem;
import dc.artists.model.ArtistsStore;
import dc.artists.ui.fragments.ArtistFragment;

public class ArtistPagerActivity extends AppCompatActivity
        implements ViewPager.OnPageChangeListener {
    private static final String EXTRA_ARTIST_ITEM = "dc.artists.artist_item";
    private ViewPager mViewPager;
    private List<ArtistItem> mArtistItems;
    private ArtistItem mArtistItem;

    public static Intent newIntent(Context context, ArtistItem artistItem) {
        Intent intent = new Intent(context, ArtistPagerActivity.class);
        intent.putExtra(EXTRA_ARTIST_ITEM, artistItem);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_pager);

        mArtistItem = (ArtistItem) getIntent().getSerializableExtra(EXTRA_ARTIST_ITEM);
        mViewPager = (ViewPager) findViewById(R.id.activity_artist_pager_view_pager);
        mArtistItems = ArtistsStore.get().getArtistItems();

        mViewPager.addOnPageChangeListener(this);

        mViewPager.setAdapter(new FragmentStatePagerAdapter(getSupportFragmentManager()) {

            @Override
            public Fragment getItem(int position) {
                ArtistItem artistItem = mArtistItems.get(position);
                return ArtistFragment.newInstance(artistItem);
            }

            @Override
            public int getCount() {
                return mArtistItems.size();
            }

        });

        for (int i = 0; i < mArtistItems.size(); i++) {
            if (mArtistItems.get(i).getId() == mArtistItem.getId()) {
                mViewPager.setCurrentItem(i);
                break;
            }
        }

    }

    private void updateTitle(String title) {
        ActionBar toolbar = getSupportActionBar();
        if (null != toolbar) {
            toolbar.setTitle(title);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem register = menu.findItem(R.id.action_link);
        String artistLink = mArtistItem.getLink();
        register.setVisible(null != artistLink && !artistLink.isEmpty());
        return super.onPrepareOptionsMenu(menu);
    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        mArtistItem = mArtistItems.get(position);
        updateTitle(mArtistItem.getName());
        supportInvalidateOptionsMenu();
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }
}