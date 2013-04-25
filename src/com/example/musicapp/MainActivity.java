package com.example.musicapp;

import com.facebook.Session;

import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.parse.Parse;

import android.os.Bundle;
import android.os.StrictMode;
import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends FragmentActivity {
	private static final int SPLASH = 0;
	private static final int MAPVIEW = 1;
	private static final int TABLEVIEW = 2;
	private static final int SETTINGS = 3;

	private static final int FRAGMENT_COUNT = SETTINGS + 1;
	private Fragment[] fragments = new Fragment[FRAGMENT_COUNT];

	private MenuItem settings;
	private MenuItem tableView;
	private boolean isResumed = false;
	private UiLifecycleHelper uiHelper;

	private Session.StatusCallback callback = new Session.StatusCallback() {

		@Override
		public void call(Session session, SessionState state,
				Exception exception) {
			onSessionStateChange(session,state,exception);

		}
	};
	static{
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Parse.initialize(this, "qD1qcwY59mUMTZrQYku0pBxb6li20Wim90epaGZ7",
				"73fM54mRV8ICnPQkm9gbBFzLHLNNCNnQMv7Wfcnw");
		uiHelper = new UiLifecycleHelper(this,callback);
		uiHelper.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		
		FragmentManager manager = getSupportFragmentManager();
		fragments[SPLASH]=manager.findFragmentById(R.id.splashFragment);
		fragments[MAPVIEW]=manager.findFragmentById(R.id.mapViewFragment);
		fragments[TABLEVIEW]=manager.findFragmentById(R.id.tableViewFragment);
		fragments[SETTINGS]=manager.findFragmentById(R.id.userSettingsFragment);
		FragmentTransaction transaction = manager.beginTransaction();

		for (int i = 0; i < fragments.length; i++) {
			transaction.hide(fragments[i]);
		}
		transaction.commit();

		
	}

	@Override
	public void onResume() {
		super.onResume();
		uiHelper.onResume();
		isResumed = true;
	}

	@Override
	public void onPause() {
		super.onPause();
		uiHelper.onPause();
		isResumed = false;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		uiHelper.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		uiHelper.onDestroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		uiHelper.onSaveInstanceState(outState);
	}

	@Override
	protected void onResumeFragments() {
		super.onResumeFragments();
		Session session = Session.getActiveSession();

		if (session != null && session.isOpened()) {
			// if the session is already open, try to show the selection
			// fragment
			showFragment(MAPVIEW, false);

		} else {
			// otherwise present the splash screen and ask the user to login.
			showFragment(SPLASH, false);
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// only add the menu when the selection fragment is showing
		if (fragments[MAPVIEW].isVisible() ) {
			if (menu.size() <1) {
				settings = menu.add(R.string.settings);
				tableView = menu.add("Table");
			}
			return true;
		} else {
			menu.clear();
			settings = null;
		}
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.equals(settings)) {
			showFragment(SETTINGS, true);
			return true;
		}
		if(item.equals(tableView)){
			showFragment(TABLEVIEW, false);
			return true;
		}

		return false;
	}
	private void onSessionStateChange(Session session, SessionState state,
			Exception exception) {
		if (isResumed) {
			FragmentManager manager = getSupportFragmentManager();
			int backStackSize = manager.getBackStackEntryCount();
			for (int i = 0; i < backStackSize; i++) {
				manager.popBackStack();
			}
			// check for the OPENED state instead of session.isOpened() since
			// for the
			// OPENED_TOKEN_UPDATED state, the selection fragment should already
			// be showing.
			if (state.equals(SessionState.OPENED)) {
				showFragment(MAPVIEW, false);
			} else if (state.isClosed()) {
				showFragment(SPLASH, false);
			}
		}
	}

	private void showFragment(int fragmentIndex, boolean addToBackStack) {
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction transaction = fm.beginTransaction();
		for (int i = 0; i < fragments.length; i++) {
			if (i == fragmentIndex) {
				transaction.show(fragments[i]);
			} else {
				transaction.hide(fragments[i]);
			}
		}
		if (addToBackStack) {
			transaction.addToBackStack(null);
		}
		transaction.commit();
	}





}
