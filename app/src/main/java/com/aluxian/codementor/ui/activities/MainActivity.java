package com.aluxian.codementor.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.aluxian.codementor.R;
import com.aluxian.codementor.data.cookies.PersistentCookieStore;
import com.aluxian.codementor.data.models.Chatroom;
import com.aluxian.codementor.presentation.listeners.ChatroomSelectedListener;
import com.aluxian.codementor.presentation.presenters.MainActivityPresenter;
import com.aluxian.codementor.presentation.views.MainActivityView;
import com.aluxian.codementor.services.CoreServices;
import com.aluxian.codementor.services.UserManager;
import com.aluxian.codementor.ui.fragments.ChatroomsFragment;
import com.aluxian.codementor.ui.fragments.ConversationFragment;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity<MainActivityPresenter>
        implements ChatroomSelectedListener, MainActivityView {

    private static final String STATE_HAS_CHATROOM_SELECTED = "has_chatroom_selected";

    @Bind(R.id.tv_empty_state) TextView emptyStateView;
    @Bind(R.id.drawer_layout) DrawerLayout drawerLayout;
    @Bind(R.id.drawer_view) View drawerView;

    private UserManager userManager;
    private PersistentCookieStore cookieStore;
    private ChatroomsFragment chatroomsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        CoreServices coreServices = getCoreServices();
        userManager = coreServices.getUserManager();
        cookieStore = coreServices.getCookieStore();

        if (!userManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        Fragment navDrawerFragment = getSupportFragmentManager().findFragmentById(R.id.drawer_view);
        chatroomsFragment = (ChatroomsFragment) navDrawerFragment;
        chatroomsFragment.init(drawerLayout, drawerView);
        chatroomsFragment.setChatroomSelectedListener(this);

        if (savedInstanceState != null) {
            if (savedInstanceState.getBoolean(STATE_HAS_CHATROOM_SELECTED)) {
                emptyStateView.setVisibility(View.GONE);
            }
        }

        setPresenter(new MainActivityPresenter(this, coreServices));
    }

    @Override
    public void onChatroomSelected(Chatroom chatroom) {
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle(chatroom.getOtherUser().getName());
            actionBar.setSubtitle(chatroom.getOtherUser().getPresenceType().statusResId);
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, ConversationFragment.newInstance(chatroom))
                .commit();

        emptyStateView.setVisibility(View.GONE);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_HAS_CHATROOM_SELECTED, emptyStateView.getVisibility() == View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                chatroomsFragment.toggleDrawer();
                return true;

            case R.id.action_log_out:
                getPresenter().userLoggedOut();
                userManager.setLoggedOut();
                cookieStore.removeAll();

                startActivity(new Intent(this, LoginActivity.class));
                finish();

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (chatroomsFragment.isDrawerOpen()) {
            chatroomsFragment.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }

}
