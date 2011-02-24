package cri.sanity;

import java.util.HashMap;
import java.util.Map;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import cri.sanity.screen.*;


public class ScreenActivity extends PrefActivity implements SharedPreferences.OnSharedPreferenceChangeListener
{
	private static final Click clickLogo = new Click(){ public boolean on(){ return A.gotoMarketPub(); }};
	private static final Map<Class<?>,Integer> mapScreenPref   = new HashMap<Class<?>,Integer>();
	private static final Map<Class<?>,Integer> mapScreenWidget = new HashMap<Class<?>,Integer>();
	private static final Map<Class<?>,Integer> mapScreenMenu   = new HashMap<Class<?>,Integer>();
	private static final Map<Integer,Class<?>> mapMenuScreen   = new HashMap<Integer,Class<?>>();
	private static final Map<String,Object>    mapSkipKeys     = P.skipKeysMap();

	protected boolean nag         = true;
	protected boolean skipAllKeys = false;
	protected boolean shortcut;
	
	//---- Activity override

	@Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    shortcut = getIntent().getIntExtra(ShortcutActivity.EXTRA_KEY, 0) > 0;		// called through shortcut?
    if(mapMenuScreen.isEmpty() && !isMainActivity()) screenerAll();
    final Integer i = mapScreenPref.get(getClass());
    if(i == null) return;
    addPreferencesFromResource(i);
		final Preference p = pref(K.LOGO);
		if(p == null) return;
		p.setTitle(A.fullName());
		p.setSummary(A.s(R.string.app_desc)+"\n"+A.s(R.string.app_copy));
		p.setPersistent(false);
		Integer w = mapScreenWidget.get(getClass());
		if(w != null) p.setWidgetLayoutResource(w);
		on(p, clickLogo);
  }

	@Override
	public void onResume()
	{
		super.onResume();
		nag();
		A.prefs().registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onPause()
	{
		super.onPause();
		A.prefs().unregisterOnSharedPreferenceChangeListener(this);
		nag = true;
		if(shortcut) finish();		// finish to allow other screens to be called through shortcut (otherwise this screen will be shown again)
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.main, menu);
    final Integer i = mapScreenMenu.get(getClass());
    if(i!=null && menu.findItem(i)!=null)
    	menu.removeItem(i);
    MenuItem m = menu.add(R.string.help);
    m.setIcon(android.R.drawable.ic_menu_help);
    m.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				A.alert(A.s(R.string.help), getGroupText(getPreferenceScreen(), false));
				return true;
			}
			private String getGroupText(PreferenceGroup pg, boolean all) {
				String msg = "";
				final int n = pg.getPreferenceCount();
				for(int i=0; i<n; i++) {
					final Preference p = pg.getPreference(i);
					if(p instanceof PreferenceGroup)
						msg += "\n** "+p.getTitle().toString().toUpperCase()+"\n\n"+getGroupText((PreferenceGroup)p, true);
					else if(all || !p.getKey().equals(K.LOGO)) {
						msg += "- "+p.getTitle()+".\n"+p.getSummary()+'\n';
						try { msg += A.s(A.rstring("help_"+p.getKey()))+'\n'; }
						catch(Exception e) {}
						msg += '\n';
					}
				}
				return msg;
			}
		});
    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		final int      id  = item.getItemId();
		final Class<?> cls = mapMenuScreen.get(id);
		if(cls == null) return super.onOptionsItemSelected(item);
		if(!isMainActivity()) finish();		// to avoid big activity stack, terminate current activity if it isn't the main one
		startActivity(new Intent(A.app(), cls));
		return true;
	}

	//---- public api

	public static final boolean alertChangeLog() {
		A.alert(A.s(R.string.changelog_title), A.rawstr(R.raw.changelog));
		return true;
	}

	public final void screener(String key, final Class<?> cls, int idPref, int idMenu, int widget) {
		screener(pref(key), cls, idPref, idMenu, widget);
	}
	public final void screener(Preference p, final Class<?> cls, int idPref, int idMenu, int widget) {
		mapScreenWidget.put(cls, widget);
		screener(p, cls, idPref, idMenu);
	}
	public final void screener(String key, final Class<?> cls, int idPref, int idMenu) {
		screener(pref(key), cls, idPref, idMenu);
	}
	public final void screener(Preference p, final Class<?> cls, int idPref, int idMenu) {
		screener(cls, idPref);
		if(idMenu > 0) {
			mapScreenMenu.put(cls, idMenu);
			mapMenuScreen.put(idMenu, cls);
		}
		if(p == null) return;
		on(p, new Click(){ public boolean on(){
			Intent i = new Intent(A.app(), cls);
			startActivity(i);
			return true;
		}});
	}
	public static final void screener(final Class<?> cls, int idPref) {
		mapScreenPref.put(cls, idPref);
	}
	public static final void screener(final Class<?> cls, int idPref, int widget) {
		mapScreenWidget.put(cls, widget);
		mapScreenPref  .put(cls, idPref);
	}

	//---- protected api

	protected final void screenerAll()
	{
		// all preferences screens
  	screener(K.SCREEN_GENERAL  , GeneralActivity.class  , R.xml.prefs_general  , R.id.menu_general  , R.layout.img_general);
  	screener(K.SCREEN_DEVICES  , DevicesActivity.class  , R.xml.prefs_devices  , R.id.menu_devices  , R.layout.img_devices);
  	screener(K.SCREEN_PROXIMITY, ProximityActivity.class, R.xml.prefs_proximity, R.id.menu_proximity, R.layout.img_proximity);
  	screener(K.SCREEN_SPEAKER  , SpeakerActivity.class  , R.xml.prefs_speaker  , R.id.menu_speaker  , R.layout.img_speaker);
  	screener(K.SCREEN_VOLUME   , VolumeActivity.class   , R.xml.prefs_volume   , R.id.menu_vol      , R.layout.img_vol);
  	screener(K.SCREEN_RECORD   , RecordActivity.class   , R.xml.prefs_record   , R.id.menu_rec      , R.layout.img_rec);
  	screener(K.SCREEN_NOTIFY   , NotifyActivity.class   , R.xml.prefs_notify   , R.id.menu_notify   , R.layout.img_notify);
  	screener(K.SCREEN_ABOUT    , AboutActivity.class    , R.xml.prefs_about    , R.id.menu_about    , R.layout.img_about);
	}

	protected void nag()
	{
		if(!nag || A.isFull()) return;
		final long now = A.now();
		if(now-A.getl(K.NAG) < Conf.NAG_TIMEOUT) return;
		A.putc(K.NAG, now);
		A.alert(
			A.rawstr(R.raw.nag),
			new A.Click(){ public void on(){ nag = true; A.gotoMarketDetails(Conf.DONATE_PKG); }},
			new A.Click(){ public void on(){ nag = true; }}
		);
		nag = false;
	}

	//---- OnSharedPreferenceChangeListener implementation

	public void onSharedPreferenceChanged(SharedPreferences prefs, String key)
	{
		if(skipAllKeys || mapSkipKeys.containsKey(key)) return;
		if(A.has(K.PRF_NAME)) A.delc(K.PRF_NAME);
	}

}
