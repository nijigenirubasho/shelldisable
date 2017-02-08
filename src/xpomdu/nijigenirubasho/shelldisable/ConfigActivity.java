package xpomdu.nijigenirubasho.shelldisable;
import android.app.*;
import android.content.*;
import android.content.SharedPreferences.*;
import android.content.pm.*;
import android.os.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import java.util.*;

public class ConfigActivity extends Activity
{
	Button save;
	Button pnview;
	EditText list;
	@Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		save = (Button) findViewById(R.id.mainButton1);
		pnview = (Button) findViewById(R.id.mainButton2);
		list = (EditText) findViewById(R.id.mainEditText1);
		final SharedPreferences sp=getSharedPreferences("noshellconfig", MODE_WORLD_READABLE);
		if (sp.getString("whitelist", "#").equals("#"))
		{
			Editor e=sp.edit();
			e.putString("whitelist", "com.pluscubed.matlog\neu.chainfire.supersu");
			e.commit();
			Toast.makeText(getApplicationContext(), "无白名单配置文件，载入默认配置...", Toast.LENGTH_SHORT).show();
		}
		list.setText(sp.getString("whitelist", null));
		save.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View p1)
				{
					Editor e=sp.edit();
					e.putString("whitelist", list.getText().toString());
					e.commit();
					Toast.makeText(getApplicationContext(), "已保存并生效", Toast.LENGTH_SHORT).show();
				}
			});
		pnview.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View p1)
				{
					Toast.makeText(getApplicationContext(), "正在生成列表，这可能需要一段时间，请稍候...", Toast.LENGTH_LONG).show();
					new Timer().schedule(new TimerTask()
						{
							public void run()
							{
								new Handler(Looper.getMainLooper()).post(new Runnable() 
									{
										@Override
										public void run()
										{
											LayoutInflater li=LayoutInflater.from(ConfigActivity.this);
											View v=li.inflate(R.layout.pndialog, null);
											EditText pnlist=(EditText) v.findViewById(R.id.pndialogEditText1);
											AlertDialog.Builder ab=new AlertDialog.Builder(ConfigActivity.this);
											ab.setTitle("包名获取界面");
											ab.setView(v);
											ab.create().show();
											pnlist.setText(getAllApp());
										}
									});
							}
						}, 500);
				}
			});

	}

	String getAllApp()
	{
		String ret = "";
		PackageManager pm = getPackageManager();
		List ls = pm.getInstalledPackages(0);
		for (PackageInfo pi : ls)
		{
			if ((pi.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0)	
			{
				ret += "一般应用\n";
			}
			else
			{
				ret += "系统应用\n";
			}
			ret += pi.applicationInfo.loadLabel(pm).toString() + "\n" + pi.packageName + "\n\n";
		}
		return ret;
	}
}
