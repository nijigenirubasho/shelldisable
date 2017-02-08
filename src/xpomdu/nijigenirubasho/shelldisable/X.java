package xpomdu.nijigenirubasho.shelldisable;
import android.util.*;
import de.robv.android.xposed.*;
import de.robv.android.xposed.callbacks.*;
import java.io.*;

public class X implements IXposedHookLoadPackage
{
	String tag="no_shell";
	String hookedSignal="シェルしないでください！---";
	@Override
	public void handleLoadPackage(XC_LoadPackage.LoadPackageParam p1) throws Throwable
	{
		final String packageName;
		Log.d(tag, "フック開始----PN:" + p1.packageName);
		XSharedPreferences xsp=new XSharedPreferences(this.getClass().getPackage().getName(), "noshellconfig");
		String whitelist= xsp.getString("whitelist", null);
		if (p1.packageName.equals("de.robv.android.xposed.installer"))
		{
			Log.d(tag, "エクスポスド発見した、フックしたくない。");
			return;
		}
		if (whitelist.contains(p1.packageName))
		{
			Log.d(tag, "このアプリ、ホワイトリストに...\nPN:" + p1.packageName);
			return;
		}
		else
		{
			packageName = p1.packageName;
			Class<?> RuntimeClazz=XposedHelpers.findClass("java.lang.Runtime", p1.classLoader);
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
							if (param.args[i]instanceof File)
							{
								execParameter += "(F)" + ((File)param.args[i]).getPath();
								param.args[i] = new File("/no_dir/");
							}
							if (i != param.args.length - 1)
							{
								execParameter += " ";
							}
						}
						Log.d(tag, hookedSignal + "RE\nPN:" + packageName + "\nEP:" + execParameter);
					}
				});
			XposedHelpers.findAndHookMethod("java.lang.ProcessBuilder", p1.classLoader, "start", new XC_MethodReplacement()
				{
					@Override
					protected Object replaceHookedMethod(MethodHookParam param) throws Throwable
					{
						Log.d(tag, hookedSignal + "PBS\nPN:" + packageName);
						return new ProcessBuilder("exit").start();
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

