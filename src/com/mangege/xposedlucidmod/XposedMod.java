package com.mangege.xposedlucidmod;

import java.util.List;

import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.preference.PreferenceActivity;
import android.preference.PreferenceActivity.Header;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class XposedMod implements IXposedHookLoadPackage {

	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
		SettingsMod(lpparam);
		SystemuiMod(lpparam);
	}

	private void SettingsMod(LoadPackageParam lpparam) {
		if (!lpparam.packageName.equals("com.android.settings"))
			return;

		// XposedBridge.log("LucidMod app: " + lpparam.packageName);

		final Class<?> SystemSelectClazz = findClass(
				"com.android.settings.SystemSelect", lpparam.classLoader);

		Class<?> SettingsClazz = findClass("com.android.settings.Settings",
				lpparam.classLoader);

		XposedBridge.hookAllMethods(SettingsClazz, "updateHeaderList",
				new XC_MethodHook() {

					@SuppressLint("NewApi")
					@Override
					protected void afterHookedMethod(MethodHookParam param)
							throws Throwable {
						PreferenceActivity activity = (PreferenceActivity) param.thisObject;
						List<Header> list = (List<Header>) param.args[0];

						for (int i = 0; i < list.size(); i++) {
							Header aHeader = list.get(i);
							if (aHeader.fragment != null
									&& aHeader.fragment
											.equals("com.android.settings.WirelessSettings")) {
								// 添加系统选择
								Header systemSelect = new Header();
								systemSelect.title = "系统选择";
								systemSelect.iconRes = aHeader.iconRes;
								Intent intent = new Intent(activity,
										SystemSelectClazz);
								systemSelect.intent = intent;
								list.add(i, systemSelect);
								break;
							}
						}

						for (int i = 0; i < list.size(); i++) {
							Header aHeader = list.get(i);
							// 删除 Backup Assistant Plus
							if (aHeader.fragment != null
									&& aHeader.fragment
											.equals("com.android.settings.BaLauncherFragment")) {
								list.remove(aHeader);
								break;
							}
						}

					}
				});

		// 删除帐号与同步里的VERIZON帐户选项
		findAndHookMethod(
				"com.android.settings.accounts.ManageAccountsSettings",
				lpparam.classLoader, "removeEmptyCategories",
				new XC_MethodHook() {
					@SuppressLint("NewApi")
					@Override
					protected void afterHookedMethod(MethodHookParam param)
							throws Throwable {
						PreferenceFragment fragment = (PreferenceFragment) param.thisObject;
						PreferenceGroup accountsGroup = (PreferenceGroup) fragment
								.findPreference("accountGroup");
						PreferenceCategory vzwAccountsCategory = ((PreferenceCategory) accountsGroup
								.findPreference("backupAssistantCategory"));
						accountsGroup.removePreference(vzwAccountsCategory);
					}
				});
	}

	private void SystemuiMod(LoadPackageParam lpparam) {
		if (!lpparam.packageName.equals("com.android.systemui"))
			return;

		XposedBridge.log("LucidMod app: " + lpparam.packageName);

		// 删除漫游三角形图标
		findAndHookMethod(
				"com.android.systemui.statusbar.phone.PhoneStatusBarPolicy",
				lpparam.classLoader, "updateCdmaRoamingIcon", Intent.class,
				XC_MethodReplacement.returnConstant(null));

		// 删除定位十字架图标
		findAndHookMethod(
				"com.android.systemui.statusbar.phone.PhoneStatusBarPolicy",
				lpparam.classLoader, "updateGPSPrivacySetting",
				XC_MethodReplacement.returnConstant(null));
	}
}
