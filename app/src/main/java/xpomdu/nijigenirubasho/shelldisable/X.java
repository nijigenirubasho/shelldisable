package xpomdu.nijigenirubasho.shelldisable;
import android.util.Log;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import java.io.File;

public class X implements IXposedHookLoadPackage
{
	String tag="no_shell";
	String hookedSignal="シェルしないでください---";
	@Override
	public void handleLoadPackage(XC_LoadPackage.LoadPackageParam p1) throws Throwable
	{
		final String packageName=p1.packageName;
		Log.i(tag, "フック開始---PN:" + packageName);
		XSharedPreferences xsp = new XSharedPreferences(this.getClass().getPackage().getName(), "noshellconfig");
		String whitelist= xsp.getString("whitelist", null);
		if (packageName.equals("de.robv.android.xposed.installer"))
		{
			Log.i(tag, "エクスポスド発見した、フックしたくない");
			return;
		}
		boolean b;
		try
		{
			b = whitelist.contains(packageName);
		}
		catch (NullPointerException e)
		{
			Log.e(tag, "配置ファイル読み取りエラー");
			return;
		}
		if (b)
		{
			Log.i(tag, "このアプリ、ホワイトリストに...\nPN:" + p1.packageName);
			return;
		}
		else
		{
			Class<?> RuntimeClazz=XposedHelpers.findClass("java.lang.Runtime", p1.classLoader);
			Class<?> ProcessBuilderClazz=XposedHelpers.findClass("java.lang.ProcessBuilder", p1.classLoader);
			XposedBridge.hookAllMethods(RuntimeClazz, "exec", new XC_MethodHook(){
					@Override
					protected void beforeHookedMethod(MethodHookParam param) throws Throwable
					{
						String execParameter = "";
						for (int i=0;i < param.args.length;i++)
						{
							execParameter += "P" + String.valueOf(i) + ":";
							if (param.args[i] instanceof String[])
							{
								execParameter += "(S[])" + stringArrayToString((String[])param.args[i], ",");
								param.args[i] = new String[]{"exit"};
							}
							if (param.args[i] instanceof String)
							{
								execParameter += "(S)" + param.args[i];
								param.args[i] = "exit";
							}
							if (param.args[i] instanceof File)
							{
								execParameter += "(F)" + ((File)param.args[i]).getPath();
								param.args[i] = new File("/no_dir/");
							}
							if (i != param.args.length - 1)
							{
								execParameter += " ";
							}
						}
						Log.i(tag, hookedSignal + "RE\nPN:" + packageName + "\nEP(" + String.valueOf(param.args.length) + "):" + execParameter);
					}
				});
			XposedBridge.hookAllConstructors(ProcessBuilderClazz, new XC_MethodHook(){
					@Override
					protected void beforeHookedMethod(MethodHookParam param) throws Throwable
					{
						String processBuilderParameter = "";
						for (int i=0;i < param.args.length;i++)
						{
							processBuilderParameter += "P" + String.valueOf(i) + ":";
							if (param.args[i] instanceof String[])
							{
								processBuilderParameter += "(S[])" + stringArrayToString((String[])param.args[i], ",");
								param.args[i] = new String[]{"exit"};
							}
							if (param.args[i] instanceof String)
							{
								processBuilderParameter += "(S)" + param.args[i];
								param.args[i] = "exit";
							}
							if (i != param.args.length - 1)
							{
								processBuilderParameter += " ";
							}
						}
						Log.i(tag, hookedSignal + "PB\nPN:" + packageName + "\nPBP(" + String.valueOf(param.args.length) + "):" + processBuilderParameter);
					}
				});
		}
	}
	String stringArrayToString(String[] in, String dot)
	{
		String out = "";
		for (int i=0;i < in.length;i++)
		{
			if (i == in.length - 1)
			{
				out += in[i];
			}
			else
			{
				out += in[i] + dot;
			}
		}
		return out;
	}
}

